# TCC BES — Granularidade de serviços em um sistema de filas

Este repositório contém o projeto de TCC em Engenharia de Software que estudará
os efeitos de diferentes níveis de granularidade de microserviços.

O sistema de referência será uma API genérica de sala de espera: participantes
entram em uma fila FIFO para disputar quantidades limitadas de um recurso, e o
sistema cria reservas temporárias conforme a capacidade fica disponível.

O primeiro estágio do projeto é um monólito modular em Java e Spring Boot. Os
módulos serão definidos por contexto de negócio para que, em etapas futuras,
possam ser extraídos e comparados por métricas de execução e consumo de
recursos.

## Estado atual

O código partiu de um protótipo MVC por camadas e agora contém a base G0 do
monólito modular de sala de espera, com autenticação de gestor, filas,
reservas, PostgreSQL, Flyway, correlation ID, Actuator e métricas Prometheus.

## Documentação

Comece pelo [índice da documentação](docs/README.md).

- [Visão e escopo](docs/01-visao-e-escopo.md)
- [Domínio e regras](docs/02-dominio-e-regras.md)
- [Arquitetura](docs/03-arquitetura.md)
- [API conceitual](docs/04-api-conceitual.md)
- [Observabilidade e experimentos](docs/05-observabilidade-e-experimentos.md)
- [Roadmap](docs/06-roadmap.md)

Para agentes de desenvolvimento, o contexto operacional está em
[AGENTS.md](AGENTS.md).

## Tecnologias atuais

- Java 21
- Spring Boot 3.5
- Spring Web MVC e Jakarta Validation
- Spring JDBC, PostgreSQL, Flyway e Testcontainers
- Spring Boot Actuator, Micrometer e Prometheus
- Maven

## Execução local

Com um PostgreSQL disponível conforme
`api-monolito/src/main/resources/application.properties`:

```powershell
cd api-monolito
.\mvnw.cmd spring-boot:run
```

Testes:

```powershell
cd api-monolito
.\mvnw.cmd test
```

As credenciais e o usuário de exemplo existentes servem apenas ao protótipo e
não representam o desenho de segurança do MVP.

## Experimentos

Os artefatos iniciais para G0-G3, k6, Prometheus, cAdvisor, Grafana e cálculo
do ICCE ficam em [experiments/](experiments/README.md).
