# Protocolo de execução

O script `experiments/scripts/run-campaign.ps1` orquestra cada run de forma isolada:

1. Seleciona grupo, perfil de carga e número da repetição.
2. Sobe a topologia Docker Compose correspondente com um nome de projeto exclusivo.
3. Aguarda a API responder em `/actuator/health`.
4. Aguarda o Prometheus responder em `/-/ready`.
5. Executa o k6 em container Docker dentro da rede do Compose.
6. Coleta séries e resumo do Prometheus.
7. Exporta tabelas dos bancos via `psql` dentro dos containers.
8. Valida invariantes de domínio com base nos CSVs exportados.
9. Salva logs do Compose e metadados da execução.
10. Remove volumes e containers da topologia antes da próxima repetição.

Configuração registrada no manifesto:

```json
{
  "startedAt": "2026-06-30T02:27:19.1406346Z",
  "resultRoot": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3",
  "groups": [
    "g0",
    "g1",
    "g2",
    "g3"
  ],
  "profiles": [
    "steady",
    "contention",
    "spike"
  ],
  "runsPerProfile": 3,
  "ports": {
    "api": 18080,
    "prometheus": 19090,
    "grafana": 13000,
    "cadvisor": 18081,
    "postgres": 15433
  },
  "environment": {
    "gitCommit": "26b34a2",
    "gitStatusShort": "M README.md\n M api-monolito/mvnw.cmd\n M api-monolito/pom.xml\n M api-monolito/src/main/java/tcc/bes/api_monolito/ApiMonolitoApplication.java\n M api-monolito/src/main/java/tcc/bes/api_monolito/controller/LoginController.java\n M api-monolito/src/main/java/tcc/bes/api_monolito/exception/GlobalExceptionHandler.java\n M api-monolito/src/main/resources/application.properties\n D api-monolito/src/main/resources/schema.sql\n M api-monolito/src/test/java/tcc/bes/api_monolito/ApiMonolitoApplicationTests.java\n M api-monolito/src/test/java/tcc/bes/api_monolito/actuator/ActuatorEndpointTest.java\n M docs/01-visao-e-escopo.md\n M docs/03-arquitetura.md\n M docs/04-api-conceitual.md\n M docs/05-observabilidade-e-experimentos.md\n M docs/06-roadmap.md\n?? .vscode/\n?? AGENTS.md\n?? BES_TCC_melhorado.pdf\n?? api-monolito/src/main/java/tcc/bes/api_monolito/identity/\n?? api-monolito/src/main/java/tcc/bes/api_monolito/queuemanagement/\n?? api-monolito/src/main/java/tcc/bes/api_monolito/reservation/\n?? api-monolito/src/main/java/tcc/bes/api_monolito/shared/\n?? api-monolito/src/main/java/tcc/bes/api_monolito/waitingroom/\n?? api-monolito/src/main/resources/db/\n?? api-monolito/src/test/java/tcc/bes/api_monolito/architecture/\n?? api-monolito/src/test/java/tcc/bes/api_monolito/identity/\n?? api-monolito/src/test/java/tcc/bes/api_monolito/reservation/\n?? api-monolito/src/test/java/tcc/bes/api_monolito/shared/\n?? api-monolito/src/test/java/tcc/bes/api_monolito/support/\n?? api-monolito/src/test/java/tcc/bes/api_monolito/waitingroom/\n?? experiments/",
    "javaVersion": "java version \"26.0.1\" 2026-04-21\nJava(TM) SE Runtime Environment (build 26.0.1+8-34)\nJava HotSpot(TM) 64-Bit Server VM (build 26.0.1+8-34, mixed mode, sharing)",
    "pythonVersion": "Python 3.14.3",
    "dockerContext": "default",
    "dockerVersion": "Client:\n Version:           29.2.1\n API version:       1.53\n Go version:        go1.25.6\n Git commit:        a5c7197\n Built:             Mon Feb  2 17:20:16 2026\n OS/Arch:           windows/amd64\n Context:           default\n\nServer: Docker Desktop 4.62.0 (219486)\n Engine:\n  Version:          29.2.1\n  API version:      1.53 (minimum version 1.44)\n  Go version:       go1.25.6\n  Git commit:       6bc6209\n  Built:            Mon Feb  2 17:17:24 2026\n  OS/Arch:          linux/amd64\n  Experimental:     false\n containerd:\n  Version:          v2.2.1\n  GitCommit:        dea7da592f5d1d2b7755e3a161be07f43fad8f75\n runc:\n  Version:          1.3.4\n  GitCommit:        v1.3.4-0-gd6d73eb8\n docker-init:\n  Version:          0.19.0\n  GitCommit:        de40ad0",
    "k6Image": "grafana/k6:0.54.0"
  },
  "endedAt": "2026-06-30T04:49:32.8959053Z"
}
```

Cada run possui um diretório próprio com `k6-summary.json`, `k6.log`, `profile-config.json`, `run-metadata.json`, `prometheus/`, `domain/` e `compose-logs/`.
