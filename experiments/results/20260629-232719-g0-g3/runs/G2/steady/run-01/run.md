# Run G2 steady #1

## Metadados

```json
{
  "group": "G2",
  "profile": "steady",
  "run": 1,
  "project": "tcc-bes-g2-steady-run1",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01",
  "status": "success",
  "startedAt": "2026-06-30T03:34:50.7144293Z",
  "endedAt": "2026-06-30T03:39:02.7532290Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T03:35:47.0387017Z",
  "loadEndedAt": "2026-06-30T03:38:50.0221174Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 5969 | 32.925 | 0.00000 | 53.954 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.389 | 1.127 | 36454750 | 37849630 | 70.799 |

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
        "entries_checked": 1431
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1431,
        "reservations_checked": 1431
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1431,
    "reservations": 1431,
    "entry_states": {
      "CONFIRMED": 1431
    },
    "reservation_states": {
      "CONFIRMED": 1431
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\domain\\residual-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\domain\\residual-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
