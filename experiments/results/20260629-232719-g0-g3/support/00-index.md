# Pacote de suporte da campanha G0-G3

Gerado em UTC: `2026-06-30T04:54:02.492808+00:00`.

Este pacote organiza os dados brutos da campanha experimental em arquivos de apoio para análise. Ele não declara conclusão comparativa; o objetivo é preservar contexto, protocolo, matriz, medições e validações que podem sustentar uma seção experimental posterior.

## Arquivos

- `01-contexto-experimental.md`: objetivo, escopo e premissas da campanha.
- `02-protocolo-de-execucao.md`: sequência executada pelo orquestrador.
- `03-perfis-de-carga.md`: perfis steady, contention e spike.
- `04-topologias.md`: leitura operacional de G0, G1, G2 e G3.
- `05-execucoes.md`: matriz de execuções e caminhos dos artefatos.
- `06-k6.md`: métricas de carga e latência extraídas do k6.
- `07-prometheus-cadvisor.md`: métricas de recursos e aplicação.
- `08-validacoes-de-dominio.md`: invariantes verificadas após cada execução.
- `09-icce.md`: insumos e cálculo de ICCE.
- `10-limitacoes.md`: limitações conhecidas e cuidados de interpretação.
- `11-analise-wohlin.md`: síntese metodológica baseada em Wohlin et al.

## Agregados

- `../aggregates/runs.csv`
- `../aggregates/k6-summary.csv`
- `../aggregates/prometheus-summary.csv`
- `../aggregates/docker-stats-summary.csv`
- `../aggregates/domain-validation.csv`
- `../aggregates/icce-input.csv`
- `../aggregates/icce-output.csv`
- `../aggregates/group-profile-summary.csv`

## Visão rápida

| Grupo | Perfil | Runs | p95 médio ms | falha média | CPU média | Mem GB média | Domínio OK |
| --- | --- | --- | --- | --- | --- | --- | --- |
| G0 | contention | 3 | 20.035 | 0.00000 | 0.361 | 0.447 | 3 |
| G0 | spike | 3 | 13.645 | 0.00000 | 0.635 | 0.449 | 3 |
| G0 | steady | 3 | 15.466 | 0.00000 | 0.258 | 0.445 | 3 |
| G1 | contention | 3 | 19.771 | 0.00000 | 0.526 | 0.805 | 3 |
| G1 | spike | 3 | 12.244 | 0.00000 | 0.596 | 0.794 | 3 |
| G1 | steady | 3 | 25.878 | 0.00000 | 0.644 | 0.789 | 3 |
| G2 | contention | 3 | 34.660 | 0.00000 | 1.232 | 1.136 | 3 |
| G2 | spike | 3 | 33.280 | 0.00000 | 1.537 | 1.148 | 3 |
| G2 | steady | 3 | 44.285 | 0.00000 | 1.246 | 1.111 | 3 |
| G3 | contention | 3 | 32.942 | 0.00000 | 1.684 | 1.830 | 3 |
| G3 | spike | 3 | 28.531 | 0.00000 | 1.948 | 1.765 | 3 |
| G3 | steady | 3 | 42.527 | 0.00000 | 1.635 | 1.767 | 3 |

## Manifesto

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
  "runs": [
    {
      "group": "G0",
      "profile": "steady",
      "run": 1,
      "project": "tcc-bes-g0-steady-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T02:27:20.5628961Z",
      "endedAt": "2026-06-30T02:31:03.8135940Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T02:27:53.7248822Z",
      "loadEndedAt": "2026-06-30T02:30:57.7534245Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-01\\docker-stats"
    },
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
    },
    {
      "group": "G0",
      "profile": "steady",
      "run": 3,
      "project": "tcc-bes-g0-steady-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T02:34:42.2220251Z",
      "endedAt": "2026-06-30T02:38:19.7642317Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T02:35:13.1931624Z",
      "loadEndedAt": "2026-06-30T02:38:15.2510920Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\steady\\run-03\\docker-stats"
    },
    {
      "group": "G0",
      "profile": "contention",
      "run": 1,
      "project": "tcc-bes-g0-contention-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T02:38:19.7802810Z",
      "endedAt": "2026-06-30T02:42:05.6006430Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T02:38:57.8210906Z",
      "loadEndedAt": "2026-06-30T02:42:00.5175112Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-01\\docker-stats"
    },
    {
      "group": "G0",
      "profile": "contention",
      "run": 2,
      "project": "tcc-bes-g0-contention-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T02:42:05.6166217Z",
      "endedAt": "2026-06-30T02:45:45.7163114Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T02:42:36.6286263Z",
      "loadEndedAt": "2026-06-30T02:45:40.4149838Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\contention\\run-02\\docker-stats"
    },
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
    },
    {
      "group": "G0",
      "profile": "spike",
      "run": 1,
      "project": "tcc-bes-g0-spike-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g0.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T02:49:27.9984212Z",
      "endedAt": "2026-06-30T02:53:13.6405482Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T02:50:04.5268855Z",
      "loadEndedAt": "2026-06-30T02:53:06.6476922Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G0\\spike\\run-01\\docker-stats"
    },
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
    },
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
    },
    {
      "group": "G1",
      "profile": "steady",
      "run": 1,
      "project": "tcc-bes-g1-steady-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T03:00:45.3290341Z",
      "endedAt": "2026-06-30T03:04:40.2289016Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:01:28.7697123Z",
      "loadEndedAt": "2026-06-30T03:04:31.0603998Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-01\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "steady",
      "run": 2,
      "project": "tcc-bes-g1-steady-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T03:04:40.2421793Z",
      "endedAt": "2026-06-30T03:08:32.3018233Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:05:20.4114595Z",
      "loadEndedAt": "2026-06-30T03:08:22.4644929Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-02\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "steady",
      "run": 3,
      "project": "tcc-bes-g1-steady-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T03:08:32.3236756Z",
      "endedAt": "2026-06-30T03:12:21.8771422Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:09:13.3654916Z",
      "loadEndedAt": "2026-06-30T03:12:15.6675880Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\steady\\run-03\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "contention",
      "run": 1,
      "project": "tcc-bes-g1-contention-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\contention\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T03:12:21.8863199Z",
      "endedAt": "2026-06-30T03:16:06.7326040Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:12:58.1877736Z",
      "loadEndedAt": "2026-06-30T03:16:01.5395692Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\contention\\run-01\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "contention",
      "run": 2,
      "project": "tcc-bes-g1-contention-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\contention\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T03:16:06.7424961Z",
      "endedAt": "2026-06-30T03:19:52.7849048Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:16:41.6512114Z",
      "loadEndedAt": "2026-06-30T03:19:44.7348504Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\contention\\run-02\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "contention",
      "run": 3,
      "project": "tcc-bes-g1-contention-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\contention\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T03:19:52.7924185Z",
      "endedAt": "2026-06-30T03:23:36.7960217Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:20:26.7808670Z",
      "loadEndedAt": "2026-06-30T03:23:29.6010337Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\contention\\run-03\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "spike",
      "run": 1,
      "project": "tcc-bes-g1-spike-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\spike\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T03:23:36.8030640Z",
      "endedAt": "2026-06-30T03:27:21.9883951Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:24:11.2985761Z",
      "loadEndedAt": "2026-06-30T03:27:13.0154389Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\spike\\run-01\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "spike",
      "run": 2,
      "project": "tcc-bes-g1-spike-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\spike\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T03:27:21.9986765Z",
      "endedAt": "2026-06-30T03:31:06.3117023Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:27:55.9722737Z",
      "loadEndedAt": "2026-06-30T03:30:57.7196205Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\spike\\run-02\\docker-stats"
    },
    {
      "group": "G1",
      "profile": "spike",
      "run": 3,
      "project": "tcc-bes-g1-spike-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g1.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\spike\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T03:31:06.3193056Z",
      "endedAt": "2026-06-30T03:34:50.6911023Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:31:40.6831865Z",
      "loadEndedAt": "2026-06-30T03:34:42.9619112Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G1\\spike\\run-03\\docker-stats"
    },
    {
      "group": "G2",
      "profile": "steady",
      "run": 1,
      "project": "tcc-bes-g2-steady-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T03:34:50.7144293Z",
      "endedAt": "2026-06-30T03:39:02.7532290Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:35:47.0387017Z",
      "loadEndedAt": "2026-06-30T03:38:50.0221174Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-01\\docker-stats"
    },
    {
      "group": "G2",
      "profile": "steady",
      "run": 2,
      "project": "tcc-bes-g2-steady-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T03:39:02.7750927Z",
      "endedAt": "2026-06-30T03:43:24.7557304Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:40:09.1258235Z",
      "loadEndedAt": "2026-06-30T03:43:12.7451453Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-02\\docker-stats"
    },
    {
      "group": "G2",
      "profile": "steady",
      "run": 3,
      "project": "tcc-bes-g2-steady-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T03:43:24.7806769Z",
      "endedAt": "2026-06-30T03:47:33.0417597Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:44:20.8916126Z",
      "loadEndedAt": "2026-06-30T03:47:23.3017861Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\steady\\run-03\\docker-stats"
    },
    {
      "group": "G2",
      "profile": "contention",
      "run": 1,
      "project": "tcc-bes-g2-contention-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T03:47:33.0659858Z",
      "endedAt": "2026-06-30T03:51:43.9046153Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:48:30.4371580Z",
      "loadEndedAt": "2026-06-30T03:51:34.4040880Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-01\\docker-stats"
    },
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
    },
    {
      "group": "G2",
      "profile": "contention",
      "run": 3,
      "project": "tcc-bes-g2-contention-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T03:55:52.2349581Z",
      "endedAt": "2026-06-30T03:59:56.6048289Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T03:56:43.6348594Z",
      "loadEndedAt": "2026-06-30T03:59:47.1504698Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\contention\\run-03\\docker-stats"
    },
    {
      "group": "G2",
      "profile": "spike",
      "run": 1,
      "project": "tcc-bes-g2-spike-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T03:59:56.6277404Z",
      "endedAt": "2026-06-30T04:04:02.9170450Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:00:48.7932236Z",
      "loadEndedAt": "2026-06-30T04:03:51.1650459Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-01\\docker-stats"
    },
    {
      "group": "G2",
      "profile": "spike",
      "run": 2,
      "project": "tcc-bes-g2-spike-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g2.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T04:04:02.9346972Z",
      "endedAt": "2026-06-30T04:08:01.5006086Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:04:49.8556674Z",
      "loadEndedAt": "2026-06-30T04:07:52.5941969Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G2\\spike\\run-02\\docker-stats"
    },
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
    },
    {
      "group": "G3",
      "profile": "steady",
      "run": 1,
      "project": "tcc-bes-g3-steady-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T04:12:00.6457393Z",
      "endedAt": "2026-06-30T04:16:23.0070401Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:13:06.9447803Z",
      "loadEndedAt": "2026-06-30T04:16:10.6117522Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-01\\docker-stats"
    },
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
    },
    {
      "group": "G3",
      "profile": "steady",
      "run": 3,
      "project": "tcc-bes-g3-steady-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T04:20:40.7545481Z",
      "endedAt": "2026-06-30T04:24:55.5191010Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:21:42.0833045Z",
      "loadEndedAt": "2026-06-30T04:24:44.9464259Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\steady\\run-03\\docker-stats"
    },
    {
      "group": "G3",
      "profile": "contention",
      "run": 1,
      "project": "tcc-bes-g3-contention-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T04:24:55.5442650Z",
      "endedAt": "2026-06-30T04:29:02.9719889Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:25:48.6074232Z",
      "loadEndedAt": "2026-06-30T04:28:52.4800543Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-01\\docker-stats"
    },
    {
      "group": "G3",
      "profile": "contention",
      "run": 2,
      "project": "tcc-bes-g3-contention-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T04:29:02.9842384Z",
      "endedAt": "2026-06-30T04:33:14.1601087Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:30:00.2217026Z",
      "loadEndedAt": "2026-06-30T04:33:04.4709571Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-02\\docker-stats"
    },
    {
      "group": "G3",
      "profile": "contention",
      "run": 3,
      "project": "tcc-bes-g3-contention-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T04:33:14.1685056Z",
      "endedAt": "2026-06-30T04:37:20.7347646Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:34:07.2350895Z",
      "loadEndedAt": "2026-06-30T04:37:10.3184869Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\contention\\run-03\\docker-stats"
    },
    {
      "group": "G3",
      "profile": "spike",
      "run": 1,
      "project": "tcc-bes-g3-spike-run1",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\spike\\run-01",
      "status": "success",
      "startedAt": "2026-06-30T04:37:20.7473182Z",
      "endedAt": "2026-06-30T04:41:24.4648015Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:38:13.9436809Z",
      "loadEndedAt": "2026-06-30T04:41:15.7146185Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\spike\\run-01\\docker-stats"
    },
    {
      "group": "G3",
      "profile": "spike",
      "run": 2,
      "project": "tcc-bes-g3-spike-run2",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\spike\\run-02",
      "status": "success",
      "startedAt": "2026-06-30T04:41:24.4781630Z",
      "endedAt": "2026-06-30T04:45:28.8971197Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:42:19.4300927Z",
      "loadEndedAt": "2026-06-30T04:45:21.7199312Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\spike\\run-02\\docker-stats"
    },
    {
      "group": "G3",
      "profile": "spike",
      "run": 3,
      "project": "tcc-bes-g3-spike-run3",
      "composeFile": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\compose\\g3.yml",
      "runDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\spike\\run-03",
      "status": "success",
      "startedAt": "2026-06-30T04:45:28.9090623Z",
      "endedAt": "2026-06-30T04:49:32.8884693Z",
      "k6ExitCode": 0,
      "error": null,
      "loadStartedAt": "2026-06-30T04:46:23.3469239Z",
      "loadEndedAt": "2026-06-30T04:49:25.4476790Z",
      "dockerStatsDir": "C:\\Users\\Pardini\\Organico\\dev\\TCC_BES\\experiments\\results\\20260629-232719-g0-g3\\runs\\G3\\spike\\run-03\\docker-stats"
    }
  ],
  "endedAt": "2026-06-30T04:49:32.8959053Z"
}
```
