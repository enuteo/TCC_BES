import argparse
import csv
import json
import subprocess
from collections import Counter, defaultdict
from pathlib import Path


LAYOUT = {
    "G0": [
        ("postgres", "tcc_bes_db", ["manager_accounts", "resources", "queues", "entries", "reservations", "reservation_terminal_events"]),
    ],
    "G1": [
        ("residual-postgres", "tcc_bes_residual", ["manager_accounts", "queues", "entries"]),
        ("reservation-postgres", "tcc_bes_reservation", ["resources", "reservations", "reservation_terminal_events"]),
    ],
    "G2": [
        ("residual-postgres", "tcc_bes_residual", ["manager_accounts", "entries"]),
        ("queue-postgres", "tcc_bes_queue", ["queues"]),
        ("reservation-postgres", "tcc_bes_reservation", ["resources", "reservations", "reservation_terminal_events"]),
    ],
    "G3": [
        ("identity-postgres", "tcc_bes_identity", ["manager_accounts"]),
        ("waiting-room-postgres", "tcc_bes_waiting_room", ["entries"]),
        ("queue-postgres", "tcc_bes_queue", ["queues"]),
        ("reservation-postgres", "tcc_bes_reservation", ["resources", "reservations", "reservation_terminal_events"]),
    ],
}


def run_psql(docker_context, project, service, database, query):
    container = f"{project}-{service}-1"
    command = [
        "docker", "--context", docker_context,
        "exec", "-i", container,
        "psql", "-U", "tcc_bes", "-d", database,
        "-c", f"COPY ({query}) TO STDOUT WITH CSV HEADER",
    ]
    result = subprocess.run(command, text=True, capture_output=True, check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    return result.stdout


def export_table(docker_context, project, service, database, table, output):
    query = f"SELECT * FROM {table}"
    csv_text = run_psql(docker_context, project, service, database, query)
    path = output / f"{service}_{table}.csv"
    path.write_text(csv_text, encoding="utf-8")
    return path


def read_csv(path):
    if not path.exists() or path.stat().st_size == 0:
        return []
    with path.open(newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def write_csv(path, rows):
    path.parent.mkdir(parents=True, exist_ok=True)
    if not rows:
        path.write_text("", encoding="utf-8")
        return
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)


def table_rows(output, table):
    rows = []
    for path in output.glob(f"*_{table}.csv"):
        rows.extend(read_csv(path))
    return rows


def check_stock(resources):
    broken = []
    for row in resources:
        total = int(row.get("total_quantity") or 0)
        available = int(row.get("available_quantity") or 0)
        held = int(row.get("held_quantity") or 0)
        confirmed = int(row.get("confirmed_quantity") or 0)
        if total != available + held + confirmed:
            broken.append(row.get("id"))
    return not broken, {"broken_resource_ids": broken, "resources_checked": len(resources)}


def check_active_participant(entries):
    active = [row for row in entries if row.get("state") in {"WAITING", "HOLD_GRANTED"}]
    counts = Counter((row.get("queue_id"), row.get("participant_hash")) for row in active)
    duplicates = [f"{queue_id}:{participant_hash}" for (queue_id, participant_hash), count in counts.items() if count > 1]
    return not duplicates, {"duplicates": duplicates, "active_entries_checked": len(active)}


def check_queue_sequence(entries):
    counts = Counter((row.get("queue_id"), row.get("sequence")) for row in entries)
    duplicates = [f"{queue_id}:{sequence}" for (queue_id, sequence), count in counts.items() if count > 1]
    return not duplicates, {"duplicates": duplicates, "entries_checked": len(entries)}


def check_entry_reservation_links(entries, reservations):
    reservations_by_id = {row.get("id"): row for row in reservations}
    entries_by_id = {row.get("id"): row for row in entries}
    missing_reservations = []
    mismatched = []
    missing_entries = []

    for entry in entries:
        reservation_id = entry.get("reservation_id")
        if reservation_id:
            reservation = reservations_by_id.get(reservation_id)
            if reservation is None:
                missing_reservations.append(reservation_id)
            elif reservation.get("entry_id") != entry.get("id"):
                mismatched.append(reservation_id)

    for reservation in reservations:
        if reservation.get("entry_id") not in entries_by_id:
            missing_entries.append(reservation.get("entry_id"))

    ok = not missing_reservations and not mismatched and not missing_entries
    return ok, {
        "missing_reservations": missing_reservations,
        "mismatched_reservations": mismatched,
        "missing_entries": missing_entries,
        "entries_checked": len(entries),
        "reservations_checked": len(reservations),
    }


def state_counts(rows):
    return dict(Counter(row.get("state", "") for row in rows))


def validate(output):
    resources = table_rows(output, "resources")
    entries = table_rows(output, "entries")
    reservations = table_rows(output, "reservations")
    queues = table_rows(output, "queues")

    checks = []
    for name, func, data in [
        ("stock_conservation", check_stock, resources),
        ("duplicate_active_participant", check_active_participant, entries),
        ("duplicate_queue_sequence", check_queue_sequence, entries),
    ]:
        passed, details = func(data)
        checks.append({"check": name, "passed": passed, "details": details})

    passed, details = check_entry_reservation_links(entries, reservations)
    checks.append({"check": "entry_reservation_links", "passed": passed, "details": details})

    summary = {
        "passed": all(item["passed"] for item in checks),
        "checks": checks,
        "counts": {
            "resources": len(resources),
            "queues": len(queues),
            "entries": len(entries),
            "reservations": len(reservations),
            "entry_states": state_counts(entries),
            "reservation_states": state_counts(reservations),
        },
    }
    return summary


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--docker-context", default="default")
    parser.add_argument("--project", required=True)
    parser.add_argument("--group", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    group = args.group.upper()
    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)

    exports = []
    errors = []
    for service, database, tables in LAYOUT[group]:
        for table in tables:
            try:
                path = export_table(args.docker_context, args.project, service, database, table, output)
                exports.append(str(path))
            except Exception as exc:
                errors.append({
                    "service": service,
                    "database": database,
                    "table": table,
                    "error": str(exc),
                })

    summary = validate(output)
    summary["exports"] = exports
    summary["errors"] = errors

    (output / "domain-validation.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    rows = []
    for check in summary["checks"]:
        rows.append({
            "check": check["check"],
            "passed": check["passed"],
            "details": json.dumps(check["details"], ensure_ascii=False),
        })
    write_csv(output / "domain-validation.csv", rows)


if __name__ == "__main__":
    main()
