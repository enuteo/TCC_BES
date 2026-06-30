# Run G0 spike #2

## Metadados

```json
{
  "group": "G0",
  "profile": "spike",
  "run": 2,
  "project": "tcc-bes-g0-spike-run2",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02",
  "status": "success",
  "startedAt": "2026-06-30T02:53:13.6730190Z",
  "endedAt": "2026-06-30T02:56:59.2841465Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T02:53:48.1281765Z",
  "loadEndedAt": "2026-06-30T02:56:50.0545786Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 21756 | 120.449 | 0.00000 | 14.350 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 0.651 | 0.439 | 44373450 | 49054590 | 116.404 |

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
        "entries_checked": 4816
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 4816,
        "reservations_checked": 153
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 4816,
    "reservations": 153,
    "entry_states": {
      "UNFULFILLABLE": 4663,
      "CONFIRMED": 153
    },
    "reservation_states": {
      "CONFIRMED": 153
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\domain\\postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\domain\\postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\domain\\postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\domain\\postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\domain\\postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-02\\domain\\postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
