# Run G2 steady #3

## Metadados

```json
{
  "group": "G2",
  "profile": "steady",
  "run": 3,
  "project": "tcc-bes-g2-steady-run3",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03",
  "status": "success",
  "startedAt": "2026-06-30T03:43:24.7806769Z",
  "endedAt": "2026-06-30T03:47:33.0417597Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T03:44:20.8916126Z",
  "loadEndedAt": "2026-06-30T03:47:23.3017861Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6240 | 34.445 | 0.00000 | 36.811 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.158 | 1.113 | 38213780 | 39616430 | 71.733 |

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
        "entries_checked": 1512
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1512,
        "reservations_checked": 1512
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1512,
    "reservations": 1512,
    "entry_states": {
      "CONFIRMED": 1512
    },
    "reservation_states": {
      "CONFIRMED": 1512
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\domain\\residual-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\domain\\residual-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
