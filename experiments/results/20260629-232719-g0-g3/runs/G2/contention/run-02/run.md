# Run G2 contention #2

## Metadados

```json
{
  "group": "G2",
  "profile": "contention",
  "run": 2,
  "project": "tcc-bes-g2-contention-run2",
  "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
  "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02",
  "status": "success",
  "startedAt": "2026-06-30T03:51:43.9251363Z",
  "endedAt": "2026-06-30T03:55:52.2059159Z",
  "k6ExitCode": 0,
  "error": null,
  "loadStartedAt": "2026-06-30T03:52:35.0015705Z",
  "loadEndedAt": "2026-06-30T03:55:39.9601920Z",
  "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\docker-stats"
}
```

## k6

| Reqs | Req/s | Falha | p95 ms | Checks |
| --- | --- | --- | --- | --- |
| 12974 | 70.644 | 0.00000 | 34.642 | 1.00000 |

## Recursos

| Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- |
| docker-stats | 1.191 | 1.122 | 44538790 | 47402220 | 114.470 |

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
        "entries_checked": 2918
      }
    },
    {
      "check": "entry_reservation_links",
      "passed": true,
      "details": {
        "missing_reservations": [],
        "mismatched_reservations": [],
        "missing_entries": [],
        "entries_checked": 2918,
        "reservations_checked": 62
      }
    }
  ],
  "counts": {
    "resources": 1,
    "queues": 1,
    "entries": 2918,
    "reservations": 62,
    "entry_states": {
      "CONFIRMED": 62,
      "UNFULFILLABLE": 2856
    },
    "reservation_states": {
      "CONFIRMED": 62
    }
  },
  "exports": [
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\domain\\residual-postgres_manager_accounts.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\domain\\residual-postgres_entries.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\domain\\queue-postgres_queues.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\domain\\reservation-postgres_resources.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\domain\\reservation-postgres_reservations.csv",
    "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-02\\domain\\reservation-postgres_reservation_terminal_events.csv"
  ],
  "errors": []
}
```
