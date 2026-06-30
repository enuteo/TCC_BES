# Run G0 steady #1

## Metadados

```json
{
  "group": "G0",
  "profile": "steady",
  "run": 1,
  "project": "tcc-bes-g0-steady-run1",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01",
  "status": "success",
  "startedAt": "2026-06-30T02:27:20.5628961Z",
  "endedAt": "2026-06-30T02:31:03.8135940Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T02:27:53.7248822Z",
  "loadEndedAt": "2026-06-30T02:30:57.7534245Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6833 | 37.447 | 0.00000 | 17.309 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 0.297 | 0.442 | 25652580 | 27115740 | 34.633 |

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
        "entries_checked": 1688
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1688,
        "reservations_checked": 1688
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1688,
    "reservations": 1688,
    "entry_states": {
      "CONFIRMED": 1688
    },
    "reservation_states": {
      "CONFIRMED": 1688
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\domain\\postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\domain\\postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\domain\\postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\domain\\postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\domain\\postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\domain\\postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
