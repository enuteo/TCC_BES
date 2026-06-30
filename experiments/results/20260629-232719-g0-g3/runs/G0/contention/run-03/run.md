# Run G0 contention #3

## Metadados

```json
{
  "group": "G0",
  "profile": "contention",
  "run": 3,
  "project": "tcc-bes-g0-contention-run3",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03",
  "status": "success",
  "startedAt": "2026-06-30T02:45:45.7302000Z",
  "endedAt": "2026-06-30T02:49:27.9760660Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T02:46:17.4183657Z",
  "loadEndedAt": "2026-06-30T02:49:20.7934612Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 14064 | 77.246 | 0.00000 | 24.235 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 0.518 | 0.443 | 30452880 | 33435760 | 77.512 |

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
        "entries_checked": 3456
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 3456,
        "reservations_checked": 56
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 3456,
    "reservations": 56,
    "entry_states": {
      "UNFULFILLABLE": 3400,
      "CONFIRMED": 56
    },
    "reservation_states": {
      "CONFIRMED": 56
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\domain\\postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\domain\\postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\domain\\postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\domain\\postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\domain\\postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-03\\domain\\postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
