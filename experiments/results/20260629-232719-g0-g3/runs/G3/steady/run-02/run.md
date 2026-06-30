# Run G3 steady #2

## Metadados

```json
{
  "group": "G3",
  "profile": "steady",
  "run": 2,
  "project": "tcc-bes-g3-steady-run2",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02",
  "status": "success",
  "startedAt": "2026-06-30T04:16:23.0251396Z",
  "endedAt": "2026-06-30T04:20:40.7339982Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T04:17:25.3212689Z",
  "loadEndedAt": "2026-06-30T04:20:28.5663237Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6257 | 34.402 | 0.00000 | 41.231 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.719 | 1.772 | 52390050 | 53804800 | 121.004 |

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
        "entries_checked": 1530
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1530,
        "reservations_checked": 1530
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1530,
    "reservations": 1530,
    "entry_states": {
      "CONFIRMED": 1530
    },
    "reservation_states": {
      "CONFIRMED": 1530
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\domain\\identity-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\domain\\waiting-room-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-02\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
