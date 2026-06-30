# Pacote experimental G0-G3

Este diretório contém os artefatos iniciais do experimento descrito no TCC:

- `compose/g0.yml`: monólito modular executável.
- `compose/g1.yml`: topologia com monólito residual e serviço de reservas.
- `compose/g2.yml`: topologia com monólito residual, gestão de filas e reservas.
- `compose/g3.yml`: topologia com monólito residual, identidade, sala de
  espera, gestão de filas e reservas.
- `k6/waiting-room.js`: carga HTTP versionável para o fluxo principal.
- `prometheus/*.yml`: coleta da API residual, serviços internos da topologia
  e cAdvisor.
- `icce/calculate_icce.py`: cálculo do ICCE a partir de CSV exportado.

Antes de subir um cenário, gere o jar:

```powershell
cd api-monolito
.\mvnw.cmd package
```

Executar G0:

```powershell
docker compose -f experiments/compose/g0.yml up --build
```

Executar G1:

```powershell
docker compose -f experiments/compose/g1.yml up --build
```

Executar G2:

```powershell
docker compose -f experiments/compose/g2.yml up --build
```

Executar G3:

```powershell
docker compose -f experiments/compose/g3.yml up --build
```

Se alguma porta padrão já estiver em uso, sobrescreva as portas publicadas sem
alterar a rede interna dos serviços:

```powershell
$env:API_PORT = "18080"
$env:PROMETHEUS_PORT = "19090"
$env:GRAFANA_PORT = "13000"
$env:CADVISOR_PORT = "18081"
$env:POSTGRES_PORT = "15433" # usado apenas no G0
docker compose -f experiments/compose/g0.yml up --build
```

Executar carga:

```powershell
k6 run experiments/k6/waiting-room.js
```

G1, G2 e G3 usam o mesmo jar com papéis de execução. O residual mantém o contrato
público `/api/v1`; os serviços extraídos expõem apenas `/internal/v1` dentro da
rede do compose. Cada serviço possui seu próprio PostgreSQL e suas migrations
Flyway por papel. G3 é o maior nível de granularidade executável previsto para
esta rodada; granularidades mais finas ficam como trabalho futuro.

Antes de considerar um resultado experimental válido, execute o cenário com a
mesma carga, valide as invariantes funcionais e registre commit, configuração,
métricas e observações da execução.
