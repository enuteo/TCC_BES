# Run G0 spike #3

## Metadados

```json
{
  "group": "G0",
  "profile": "spike",
  "run": 3,
  "project": "tcc-bes-g0-spike-run3",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03",
  "status": "success",
  "startedAt": "2026-06-30T02:56:59.3008571Z",
  "endedAt": "2026-06-30T03:00:45.3039081Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T02:57:33.7451053Z",
  "loadEndedAt": "2026-06-30T03:00:36.3839532Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 21902 | 121.208 | 0.00000 | 12.221 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 0.616 | 0.450 | 44952880 | 49733940 | 115.417 |

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
        "entries_checked": 4884
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 4884,
        "reservations_checked": 150
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 4884,
    "reservations": 150,
    "entry_states": {
      "CONFIRMED": 150,
      "UNFULFILLABLE": 4734
    },
    "reservation_states": {
      "CONFIRMED": 150
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\domain\\postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\domain\\postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\domain\\postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\domain\\postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\domain\\postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-03\\domain\\postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
