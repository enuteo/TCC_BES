# Run G2 spike #3

## Metadados

```json
{
  "group": "G2",
  "profile": "spike",
  "run": 3,
  "project": "tcc-bes-g2-spike-run3",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03",
  "status": "success",
  "startedAt": "2026-06-30T04:08:01.5239112Z",
  "endedAt": "2026-06-30T04:12:00.6293002Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T04:08:49.3674199Z",
  "loadEndedAt": "2026-06-30T04:11:51.8057501Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 19933 | 109.999 | 0.00000 | 29.828 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.480 | 1.130 | 60593920 | 64739460 | 172.002 |

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
        "entries_checked": 3927
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 3927,
        "reservations_checked": 149
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 3927,
    "reservations": 149,
    "entry_states": {
      "UNFULFILLABLE": 3778,
      "CONFIRMED": 149
    },
    "reservation_states": {
      "CONFIRMED": 149
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\domain\\residual-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\domain\\residual-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-03\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
