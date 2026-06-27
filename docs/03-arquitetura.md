# Arquitetura

## Estado atual

O projeto existente é um protótipo Spring Boot organizado horizontalmente:

```text
controller -> service -> repository -> MySQL
```

Ele contém:

- endpoint de login em `/api/auth/login`;
- endpoint de status em `/api/status`;
- tabela `usuarios` acessada com Spring JDBC;
- tratamento global de erros e validation;
- filtro de correlation ID;
- Actuator e exportação para Prometheus;
- testes de controller, filtro e endpoints operacionais.

Esse código prova a base técnica, mas ainda não implementa o domínio de filas.
Também não possui limites de contexto: `controller`, `service`, `repository`,
`model` e `dto` são pacotes globais.

O login compara senha em texto simples e retorna dados do usuário sem uma
credencial de sessão. Ele é demonstrativo e não atende ao requisito de gestor
autenticado.

## Arquitetura alvo do MVP

O alvo é um monólito modular: uma aplicação implantável, um processo e um banco,
com dependências controladas entre módulos de negócio.

```text
HTTP / jobs
     |
     v
interfaces -> application -> domain
                    |
                    v
             ports <- infrastructure
```

- `interfaces`: controllers HTTP, DTOs e disparadores de jobs.
- `application`: casos de uso, autorização e coordenação de transações.
- `domain`: agregados, valores, políticas, eventos e portas necessárias.
- `infrastructure`: JDBC, relógio, geração de tokens e publicação técnica.

MVC continua presente no adaptador web, mas não determina a organização global
nem recebe regras de negócio.

## Módulos

| Módulo | Responsabilidade | Dados sob sua autoridade |
| --- | --- | --- |
| Identidade e Acesso | Gestores, credenciais e autorização | gestor, credencial ou sessão |
| Gestão de Filas | Configuração e ciclo de vida | fila e vínculo exclusivo com recurso |
| Sala de Espera | Entrada, FIFO, posição e worker | entrada e chave de idempotência |
| Inventário e Reservas | Recurso, saldo, hold e confirmação | estoque e reserva |

Observabilidade, correlation ID, tratamento HTTP e relógio são capacidades
transversais, não um contexto de domínio.

## Dependências permitidas

```text
interfaces HTTP --autoriza--> Identidade e Acesso
       |
       +--comanda--> Gestão de Filas
       |
       +--comanda--> Sala de Espera

Sala de Espera --consulta estado--> Gestão de Filas
Sala de Espera --solicita hold----> Inventário e Reservas
Gestão de Filas --valida recurso--> Inventário e Reservas
```

- Identidade e Acesso não depende dos demais módulos.
- Inventário e Reservas não conhece regras de ordenação ou usuários.
- Gestão de Filas não manipula entradas nem estoque.
- A camada de aplicação valida a existência do recurso por uma interface de
  Inventário e Reservas ao configurar a fila, sem alterar o estoque.
- Sala de Espera consulta se a fila está aberta e solicita a reserva por
  interfaces públicas dos módulos proprietários.
- Nenhum repositório acessa tabela pertencente a outro módulo.
- DTO HTTP não atravessa para o domínio nem vira contrato entre módulos.
- Código compartilhado deve se limitar a primitivas técnicas estáveis, como
  identificadores, relógio e metadados de correlação.

## Organização de pacotes

A implementação deve migrar gradualmente para a forma:

```text
tcc.bes.api_monolito
  identity
    domain
    application
    interfaces
    infrastructure
  queuemanagement
    domain
    application
    interfaces
    infrastructure
  waitingroom
    domain
    application
    interfaces
    infrastructure
  reservation
    domain
    application
    interfaces
    infrastructure
  shared
```

`shared` não pode se tornar um depósito de entidades ou serviços de negócio.

## Persistência

O MVP mantém MySQL e Spring JDBC. Uma única instância e schema são suficientes,
mas cada módulo possui suas tabelas e seus mapeadores.

As referências entre módulos usam identificadores, não objetos de persistência
compartilhados. Chaves estrangeiras podem proteger integridade durante a fase
monolítica, desde que não sejam usadas para consultas que contornem a interface
do módulo proprietário.

O ciclo do worker pode coordenar, em uma transação local, a entrada e a criação
do hold. Cada módulo continua acessando apenas suas próprias tabelas. Essa
transação entre módulos é uma característica conhecida da linha de base; uma
futura extração deverá substituí-la por protocolo distribuído sem alterar o
resultado funcional.

Migrações versionadas devem substituir a inicialização manual por `schema.sql`
antes de congelar a linha de base experimental.

## Comunicação e eventos

- Chamadas entre módulos são Java local e síncronas quando uma resposta é
  necessária.
- Eventos internos anunciam fatos já confirmados e são publicados após commit.
- Não haverá HTTP interno, service discovery ou broker apenas para simular
  microserviços.
- A entrega confiável de eventos distribuídos será decidida quando um módulo
  for efetivamente extraído.

## Concorrência e consistência

Estoque, reserva e estado correspondente da entrada exigem consistência forte
na linha de base. O caso de uso deve:

- garantir apenas um worker efetivo por entrada;
- impedir saldo negativo;
- serializar resultados concorrentes de confirmar, cancelar e expirar;
- confirmar a transação antes de publicar eventos e métricas de resultado;
- permitir nova tentativa segura em conflito transitório.

Relógio e gerador de identificadores devem ser injetáveis para testes
determinísticos.

## Segurança

- Operações administrativas exigem identidade de gestor autenticada e
  autorização sobre a fila.
- O protocolo de emissão da credencial do gestor será definido na
  implementação de Identidade e Acesso; o login atual não deve ser ampliado
  como está.
- O token da entrada concede acesso somente àquela entrada e reserva.
- Senhas, tokens, respostas idempotentes e chaves de idempotência não são
  persistidos em texto simples; eventual resposta que precise ser repetida é
  cifrada e possui retenção limitada.
- Logs, métricas e erros não expõem credenciais, token, chave do participante
  ou dados de autenticação.

## Preparação para extração futura

O monólito estará estruturalmente preparado quando:

- dependências de pacote coincidirem com o diagrama permitido;
- cada módulo possuir API de aplicação e dados próprios;
- eventos não carregarem entidades internas;
- testes de contrato separarem interface pública de implementação;
- nenhuma regra depender de chamada direta a repository de outro módulo;
- a carga experimental puder atingir as capacidades por HTTP.

Esses critérios criam opções de extração. Eles não definem ainda quantos
serviços existirão nem a ordem de migração.
