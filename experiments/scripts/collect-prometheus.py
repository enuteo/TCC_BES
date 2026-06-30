import argparse
import csv
import json
import math
import time
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path


QUERIES = {
    "cadvisor_cpu_cores": [
        'sum(rate(container_cpu_usage_seconds_total{{container_label_com_docker_compose_project="{project}"}}[30s]))',
        'sum(rate(container_cpu_usage_seconds_total{{name=~".*{project}.*"}}[30s]))',
        'sum(rate(container_cpu_usage_seconds_total{{container=~".*{project}.*"}}[30s]))',
    ],
    "cadvisor_memory_bytes": [
        'sum(container_memory_working_set_bytes{{container_label_com_docker_compose_project="{project}"}})',
        'sum(container_memory_working_set_bytes{{name=~".*{project}.*"}})',
        'sum(container_memory_working_set_bytes{{container=~".*{project}.*"}})',
    ],
    "cadvisor_rx_bytes": [
        'sum(container_network_receive_bytes_total{{container_label_com_docker_compose_project="{project}"}})',
        'sum(container_network_receive_bytes_total{{name=~".*{project}.*"}})',
        'sum(container_network_receive_bytes_total{{container=~".*{project}.*"}})',
    ],
    "cadvisor_tx_bytes": [
        'sum(container_network_transmit_bytes_total{{container_label_com_docker_compose_project="{project}"}})',
        'sum(container_network_transmit_bytes_total{{name=~".*{project}.*"}})',
        'sum(container_network_transmit_bytes_total{{container=~".*{project}.*"}})',
    ],
    "http_rps": "sum(rate(http_server_requests_seconds_count[30s]))",
    "http_error_rps": 'sum(rate(http_server_requests_seconds_count{{status=~"4..|5.."}}[30s]))',
    "waiting_entries_created": "sum(waitingroom_entries_created_total)",
    "waiting_entries_waiting": "sum(waitingroom_entries_waiting)",
    "waiting_entries_completed": "sum(waitingroom_entries_completed_total)",
    "worker_cycles": "sum(waitingroom_worker_cycles_total)",
    "reservation_holds_created": "sum(reservation_holds_created_total)",
    "reservation_completed": "sum(reservation_completed_total)",
    "reservation_holds_active": "sum(reservation_holds_active)",
    "reservation_stock_units": "sum(reservation_stock_units)",
}


def parse_time(value):
    if value.endswith("Z"):
        value = value[:-1] + "+00:00"
    return datetime.fromisoformat(value).astimezone(timezone.utc)


def query_range(base_url, query, start_ts, end_ts, step):
    params = urllib.parse.urlencode({
        "query": query,
        "start": f"{start_ts:.3f}",
        "end": f"{end_ts:.3f}",
        "step": str(step),
    })
    url = f"{base_url.rstrip('/')}/api/v1/query_range?{params}"
    with urllib.request.urlopen(url, timeout=20) as response:
        payload = json.loads(response.read().decode("utf-8"))
    if payload.get("status") != "success":
        raise RuntimeError(payload)
    return payload


def values_by_timestamp(payload):
    points = {}
    for series in payload.get("data", {}).get("result", []):
        for timestamp, value in series.get("values", []):
            try:
                numeric = float(value)
            except (TypeError, ValueError):
                numeric = 0.0
            if math.isnan(numeric) or math.isinf(numeric):
                numeric = 0.0
            points[float(timestamp)] = points.get(float(timestamp), 0.0) + numeric
    return dict(sorted(points.items()))


def avg(points):
    if not points:
        return 0.0
    return sum(points.values()) / len(points)


def delta(points):
    if len(points) < 2:
        return 0.0
    values = list(points.values())
    return max(0.0, values[-1] - values[0])


def write_csv(path, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    if not rows:
        path.write_text("", encoding="utf-8")
        return
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--group", required=True)
    parser.add_argument("--profile", required=True)
    parser.add_argument("--run", required=True, type=int)
    parser.add_argument("--project", required=True)
    parser.add_argument("--start", required=True)
    parser.add_argument("--end", required=True)
    parser.add_argument("--step", default="15")
    args = parser.parse_args()

    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    series_dir = output / "series"
    series_dir.mkdir(parents=True, exist_ok=True)

    start_dt = parse_time(args.start)
    end_dt = parse_time(args.end)
    start_ts = start_dt.timestamp()
    end_ts = end_dt.timestamp()
    duration_seconds = max(0.0, end_ts - start_ts)

    collected = {}
    errors = {}
    for name, template in QUERIES.items():
        templates = template if isinstance(template, list) else [template]
        attempted_queries = []
        last_error = None
        selected_query = None
        selected_payload = None
        selected_points = {}
        for index, candidate in enumerate(templates):
            query = candidate.format(project=args.project)
            attempted_queries.append(query)
            try:
                payload = query_range(args.base_url, query, start_ts, end_ts, args.step)
                points = values_by_timestamp(payload)
                if points or index == len(templates) - 1:
                    selected_query = query
                    selected_payload = payload
                    selected_points = points
                    break
            except Exception as exc:
                last_error = str(exc)
                time.sleep(0.1)

        if selected_query is not None:
            collected[name] = selected_points
            (series_dir / f"{name}.json").write_text(json.dumps({
                "query": selected_query,
                "attemptedQueries": attempted_queries,
                "payload": selected_payload,
            }, indent=2), encoding="utf-8")
        else:
            errors[name] = last_error or "no query produced data"
            collected[name] = {}
            (series_dir / f"{name}.json").write_text(json.dumps({
                "attemptedQueries": attempted_queries,
                "error": errors[name],
            }, indent=2), encoding="utf-8")

    summary = {
        "group": args.group,
        "profile": args.profile,
        "run": args.run,
        "project": args.project,
        "start": args.start,
        "end": args.end,
        "duration_seconds": duration_seconds,
        "cpu_avg": avg(collected["cadvisor_cpu_cores"]),
        "mem_avg_gb": avg(collected["cadvisor_memory_bytes"]) / 1_000_000_000,
        "rx_bytes": delta(collected["cadvisor_rx_bytes"]),
        "tx_bytes": delta(collected["cadvisor_tx_bytes"]),
        "http_rps_avg": avg(collected["http_rps"]),
        "http_error_rps_avg": avg(collected["http_error_rps"]),
        "waiting_entries_created_delta": delta(collected["waiting_entries_created"]),
        "waiting_entries_waiting_avg": avg(collected["waiting_entries_waiting"]),
        "waiting_entries_completed_delta": delta(collected["waiting_entries_completed"]),
        "worker_cycles_delta": delta(collected["worker_cycles"]),
        "reservation_holds_created_delta": delta(collected["reservation_holds_created"]),
        "reservation_completed_delta": delta(collected["reservation_completed"]),
        "reservation_holds_active_avg": avg(collected["reservation_holds_active"]),
        "reservation_stock_units_avg": avg(collected["reservation_stock_units"]),
        "errors": errors,
    }

    (output / "prometheus-summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    csv_summary = {k: v for k, v in summary.items() if k != "errors"}
    write_csv(output / "prometheus-summary.csv", [csv_summary])


if __name__ == "__main__":
    main()
