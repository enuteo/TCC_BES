# Run G1 steady #1

## Metadados

```json
{
  "group": "G1",
  "profile": "steady",
  "run": 1,
  "project": "tcc-bes-g1-steady-run1",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01",
  "status": "success",
  "startedAt": "2026-06-30T03:00:45.3290341Z",
  "endedAt": "2026-06-30T03:04:40.2289016Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T03:01:28.7697123Z",
  "loadEndedAt": "2026-06-30T03:04:31.0603998Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6429 | 35.503 | 0.00000 | 29.574 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 0.653 | 0.799 | 34907240 | 36309790 | 67.085 |

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
        "entries_checked": 1565
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1565,
        "reservations_checked": 1565
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1565,
    "reservations": 1565,
    "entry_states": {
      "CONFIRMED": 1565
    },
    "reservation_states": {
      "CONFIRMED": 1565
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\domain\\residual-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\domain\\residual-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\domain\\residual-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
