# Perfis de carga

Os perfis usam o mesmo script k6 (`experiments/k6/waiting-room.js`) e variam concorrência, duração e pressão sobre estoque.

## contention

```json
{
  "MANAGER_PASSWORD": "admin123",
  "HOLD_SECONDS": "45",
  "POLL_ATTEMPTS": "30",
  "RESOURCE_TOTAL": "120",
  "PROFILE": "contention",
  "HTTP_FAILED_RATE": "0.20",
  "POLL_SLEEP_SECONDS": "1",
  "DURATION": "3m",
  "MAX_QUANTITY": "3",
  "MANAGER_USERNAME": "admin",
  "HTTP_P95_MS": "5000",
  "VUS": "40",
  "BATCH_SIZE": "20"
}
```

## spike

```json
{
  "MANAGER_PASSWORD": "admin123",
  "HOLD_SECONDS": "45",
  "POLL_ATTEMPTS": "30",
  "RESOURCE_TOTAL": "300",
  "STAGE_RAMP_UP": "30s",
  "STAGE_HOLD": "2m",
  "POLL_SLEEP_SECONDS": "1",
  "SPIKE_VUS": "80",
  "PROFILE": "spike",
  "STAGE_RAMP_DOWN": "30s",
  "MANAGER_USERNAME": "admin",
  "HTTP_FAILED_RATE": "0.20",
  "HTTP_P95_MS": "5000",
  "BATCH_SIZE": "30",
  "MAX_QUANTITY": "3"
}
```

## steady

```json
{
  "MANAGER_PASSWORD": "admin123",
  "HOLD_SECONDS": "30",
  "POLL_ATTEMPTS": "20",
  "RESOURCE_TOTAL": "5000",
  "PROFILE": "steady",
  "HTTP_FAILED_RATE": "0.20",
  "POLL_SLEEP_SECONDS": "1",
  "DURATION": "3m",
  "MAX_QUANTITY": "3",
  "MANAGER_USERNAME": "admin",
  "HTTP_P95_MS": "5000",
  "VUS": "10",
  "BATCH_SIZE": "50"
}
```

Leitura operacional:

- `steady`: carga constante para observar custo médio e estabilidade.
- `contention`: estoque baixo em relação à concorrência para forçar disputa por holds e estados terminais.
- `spike`: subida rápida de usuários virtuais para observar elasticidade e filas sob rajada.
