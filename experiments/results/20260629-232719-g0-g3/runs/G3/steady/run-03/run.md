# Run G3 steady #3

## Metadados

```json
{
  "group": "G3",
  "profile": "steady",
  "run": 3,
  "project": "tcc-bes-g3-steady-run3",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03",
  "status": "success",
  "startedAt": "2026-06-30T04:20:40.7545481Z",
  "endedAt": "2026-06-30T04:24:55.5191010Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T04:21:42.0833045Z",
  "loadEndedAt": "2026-06-30T04:24:44.9464259Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6348 | 34.939 | 0.00000 | 44.888 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.453 | 1.784 | 52945870 | 54443620 | 124.956 |

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
        "entries_checked": 1556
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1556,
        "reservations_checked": 1556
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1556,
    "reservations": 1556,
    "entry_states": {
      "CONFIRMED": 1556
    },
    "reservation_states": {
      "CONFIRMED": 1556
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\domain\\identity-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\domain\\waiting-room-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
