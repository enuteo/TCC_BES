# Run G2 steady #2

## Metadados

```json
{
  "group": "G2",
  "profile": "steady",
  "run": 2,
  "project": "tcc-bes-g2-steady-run2",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02",
  "status": "success",
  "startedAt": "2026-06-30T03:39:02.7750927Z",
  "endedAt": "2026-06-30T03:43:24.7557304Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T03:40:09.1258235Z",
  "loadEndedAt": "2026-06-30T03:43:12.7451453Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6113 | 33.615 | 0.00000 | 42.089 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.191 | 1.094 | 37556890 | 38931870 | 71.168 |

## Domínio

```json
{
  "passed": true,
  "checks": [
    {
      "check": "stock_conservation",
      "passed": true,
      "details": {
        "broken_resource_ids": [],
        "resources_checked": 1
      }
    },
    {
      "check": "duplicate_active_participant",
      "passed": true,
      "details": {
        "duplicates": [],
        "active_entries_checked": 0
      }
    },
    {
      "check": "duplicate_queue_sequence",
      "passed": true,
      "details": {
        "duplicates": [],
        "entries_checked": 1473
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1473,
        "reservations_checked": 1473
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1473,
    "reservations": 1473,
    "entry_states": {
      "CONFIRMED": 1473
    },
    "reservation_states": {
      "CONFIRMED": 1473
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\domain\\residual-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\domain\\residual-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
