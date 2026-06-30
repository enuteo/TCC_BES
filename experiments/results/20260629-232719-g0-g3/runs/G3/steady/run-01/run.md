# Run G3 steady #1

## Metadados

```json
{
  "group": "G3",
  "profile": "steady",
  "run": 1,
  "project": "tcc-bes-g3-steady-run1",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01",
  "status": "success",
  "startedAt": "2026-06-30T04:12:00.6457393Z",
  "endedAt": "2026-06-30T04:16:23.0070401Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T04:13:06.9447803Z",
  "loadEndedAt": "2026-06-30T04:16:10.6117522Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6255 | 34.316 | 0.00000 | 41.462 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.733 | 1.745 | 52493160 | 53997790 | 119.474 |

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
        "entries_checked": 1529
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1529,
        "reservations_checked": 1529
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1529,
    "reservations": 1529,
    "entry_states": {
      "CONFIRMED": 1529
    },
    "reservation_states": {
      "CONFIRMED": 1529
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\domain\\identity-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\domain\\waiting-room-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
