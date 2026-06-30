# Run G0 steady #2

## Metadados

```json
{
  "group": "G0",
  "profile": "steady",
  "run": 2,
  "project": "tcc-bes-g0-steady-run2",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02",
  "status": "success",
  "startedAt": "2026-06-30T02:31:03.8327006Z",
  "endedAt": "2026-06-30T02:34:42.2140689Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T02:31:34.7927979Z",
  "loadEndedAt": "2026-06-30T02:34:37.6578081Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 6777 | 37.259 | 0.00000 | 11.646 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 0.231 | 0.444 | 25252080 | 26745240 | 34.367 |

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
        "entries_checked": 1667
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 1667,
        "reservations_checked": 1667
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 1667,
    "reservations": 1667,
    "entry_states": {
      "CONFIRMED": 1667
    },
    "reservation_states": {
      "CONFIRMED": 1667
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\domain\\postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\domain\\postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\domain\\postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\domain\\postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\domain\\postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-02\\domain\\postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
