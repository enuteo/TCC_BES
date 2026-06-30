# Run G3 contention #1

## Metadados

```json
{
  "group": "G3",
  "profile": "contention",
  "run": 1,
  "project": "tcc-bes-g3-contention-run1",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01",
  "status": "success",
  "startedAt": "2026-06-30T04:24:55.5442650Z",
  "endedAt": "2026-06-30T04:29:02.9719889Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T04:25:48.6074232Z",
  "loadEndedAt": "2026-06-30T04:28:52.4800543Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 13311 | 72.810 | 0.00000 | 32.953 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.784 | 1.853 | 78636610 | 81365600 | 232.134 |

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
        "entries_checked": 3114
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 3114,
        "reservations_checked": 54
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 3114,
    "reservations": 54,
    "entry_states": {
      "UNFULFILLABLE": 3060,
      "CONFIRMED": 54
    },
    "reservation_states": {
      "CONFIRMED": 54
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\domain\\identity-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\domain\\waiting-room-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
