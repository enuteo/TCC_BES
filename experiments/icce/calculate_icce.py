import argparse
import csv
from pathlib import Path


def read_rows(path):
    with Path(path).open(newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def metric(row, name):
    return float(row[name])


def cpu_time(row):
    return metric(row, "cpu_avg") * metric(row, "duration_seconds")


def mem_time(row):
    return metric(row, "mem_avg_gb") * metric(row, "duration_seconds")


def net_total(row):
    return metric(row, "rx_bytes") + metric(row, "tx_bytes")


def main():
    parser = argparse.ArgumentParser(description="Calculate ICCE normalized by G0 for each load profile.")
    parser.add_argument("input", help="CSV with group,load,run,cpu_avg,duration_seconds,mem_avg_gb,rx_bytes,tx_bytes")
    parser.add_argument("output", help="Output CSV with normalized metrics and ICCE")
    args = parser.parse_args()

    rows = read_rows(args.input)
    baseline = {}
    for row in rows:
        if row["group"] == "G0":
            baseline.setdefault(row["load"], []).append(row)

    baseline_avg = {}
    for load, load_rows in baseline.items():
        baseline_avg[load] = {
            "cpu": sum(cpu_time(row) for row in load_rows) / len(load_rows),
            "mem": sum(mem_time(row) for row in load_rows) / len(load_rows),
            "net": sum(net_total(row) for row in load_rows) / len(load_rows),
        }

    output_rows = []
    for row in rows:
        base = baseline_avg[row["load"]]
        cpu_norm = cpu_time(row) / base["cpu"] if base["cpu"] else 0
        mem_norm = mem_time(row) / base["mem"] if base["mem"] else 0
        net_norm = net_total(row) / base["net"] if base["net"] else 0
        output_rows.append({
            **row,
            "cpu_time": cpu_time(row),
            "mem_time": mem_time(row),
            "net_total": net_total(row),
            "cpu_norm": cpu_norm,
            "mem_norm": mem_norm,
            "net_norm": net_norm,
            "icce": (cpu_norm + mem_norm + net_norm) / 3,
        })

    with Path(args.output).open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(output_rows[0].keys()))
        writer.writeheader()
        writer.writerows(output_rows)


if __name__ == "__main__":
    main()
