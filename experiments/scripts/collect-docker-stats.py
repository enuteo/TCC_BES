import argparse
import csv
import json
import subprocess
import time
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path


UNIT_FACTORS = {
    "b": 1,
    "kb": 1_000,
    "mb": 1_000_000,
    "gb": 1_000_000_000,
    "tb": 1_000_000_000_000,
    "kib": 1024,
    "mib": 1024 ** 2,
    "gib": 1024 ** 3,
    "tib": 1024 ** 4,
}


def run_docker(context, args):
    command = ["docker", "--context", context] + args
    result = subprocess.run(command, text=True, capture_output=True, check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    return result.stdout


def parse_percent(value):
    if not value:
        return 0.0
    return float(value.strip().replace("%", "").replace(",", "."))


def parse_size(value):
    if not value:
        return 0.0
    text = value.strip().replace(",", ".")
    if text == "0B":
        return 0.0
    number = ""
    unit = ""
    for char in text:
        if char.isdigit() or char == ".":
            number += char
        else:
            unit += char
    if not number:
        return 0.0
    unit = unit.strip().lower()
    return float(number) * UNIT_FACTORS.get(unit, 1)


def parse_pair(value):
    if not value or "/" not in value:
        return 0.0, 0.0
    left, right = value.split("/", 1)
    return parse_size(left), parse_size(right)


def list_containers(context, project):
    output = run_docker(context, [
        "ps",
        "--filter", f"label=com.docker.compose.project={project}",
        "--format", "{{.ID}}",
    ])
    return [line.strip() for line in output.splitlines() if line.strip()]


def collect_sample(context, container_ids):
    if not container_ids:
        return []
    output = run_docker(context, ["stats", "--no-stream", "--format", "{{json .}}"] + container_ids)
    rows = []
    timestamp = datetime.now(timezone.utc).isoformat()
    for line in output.splitlines():
        if not line.strip():
            continue
        raw = json.loads(line)
        mem_usage, mem_limit = parse_pair(raw.get("MemUsage", ""))
        net_rx, net_tx = parse_pair(raw.get("NetIO", ""))
        block_read, block_write = parse_pair(raw.get("BlockIO", ""))
        rows.append({
            "timestamp": timestamp,
            "container_id": raw.get("Container", ""),
            "name": raw.get("Name", ""),
            "cpu_percent": parse_percent(raw.get("CPUPerc", "")),
            "cpu_cores": parse_percent(raw.get("CPUPerc", "")) / 100.0,
            "mem_usage_bytes": mem_usage,
            "mem_limit_bytes": mem_limit,
            "mem_percent": parse_percent(raw.get("MemPerc", "")),
            "net_rx_bytes": net_rx,
            "net_tx_bytes": net_tx,
            "block_read_bytes": block_read,
            "block_write_bytes": block_write,
            "pids": raw.get("PIDs", ""),
            "raw": json.dumps(raw, ensure_ascii=False),
        })
    return rows


def write_csv(path, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    rows = list(rows)
    if not rows:
        path.write_text("", encoding="utf-8")
        return
    fieldnames = list(rows[0].keys())
    with path.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def summarize(rows):
    if not rows:
        return {
            "sample_count": 0,
            "container_count": 0,
            "cpu_avg": 0.0,
            "mem_avg_gb": 0.0,
            "rx_bytes": 0.0,
            "tx_bytes": 0.0,
            "errors": [],
        }

    by_ts = defaultdict(list)
    by_container = defaultdict(list)
    for row in rows:
        by_ts[row["timestamp"]].append(row)
        by_container[row["container_id"]].append(row)

    cpu_points = [sum(item["cpu_cores"] for item in group) for group in by_ts.values()]
    mem_points = [sum(item["mem_usage_bytes"] for item in group) for group in by_ts.values()]

    rx_delta = 0.0
    tx_delta = 0.0
    for items in by_container.values():
        items = sorted(items, key=lambda row: row["timestamp"])
        rx_delta += max(0.0, items[-1]["net_rx_bytes"] - items[0]["net_rx_bytes"])
        tx_delta += max(0.0, items[-1]["net_tx_bytes"] - items[0]["net_tx_bytes"])

    return {
        "sample_count": len(by_ts),
        "container_count": len(by_container),
        "cpu_avg": sum(cpu_points) / len(cpu_points),
        "mem_avg_gb": (sum(mem_points) / len(mem_points)) / 1_000_000_000,
        "rx_bytes": rx_delta,
        "tx_bytes": tx_delta,
        "errors": [],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--docker-context", default="default")
    parser.add_argument("--project", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--stop-file", required=True)
    parser.add_argument("--interval-seconds", type=float, default=2.0)
    args = parser.parse_args()

    output = Path(args.output)
    stop_file = Path(args.stop_file)
    output.mkdir(parents=True, exist_ok=True)

    rows = []
    errors = []
    while not stop_file.exists():
        try:
            container_ids = list_containers(args.docker_context, args.project)
            rows.extend(collect_sample(args.docker_context, container_ids))
        except Exception as exc:
            errors.append({
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "error": str(exc),
            })
        time.sleep(args.interval_seconds)

    samples_path = output / "docker-stats-samples.csv"
    summary_path = output / "docker-stats-summary.json"
    csv_summary_path = output / "docker-stats-summary.csv"
    errors_path = output / "docker-stats-errors.json"

    write_csv(samples_path, rows)
    summary = summarize(rows)
    summary["errors"] = errors
    summary_path.write_text(json.dumps(summary, indent=2), encoding="utf-8")
    write_csv(csv_summary_path, [summary])
    errors_path.write_text(json.dumps(errors, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
