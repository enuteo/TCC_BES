# Observabilidade e experimentos

## Objetivo

A observabilidade deve servir simultaneamente à operação do MVP e à futura
comparação entre granularidades. A coleta precisa distinguir:

- carga recebida;
- tempo e resultado do processamento;
- estado do domínio;
- recursos computacionais consumidos.

Uma variante não será considerada melhor apenas por latência menor se violar
FIFO, perder reservas ou processar carga funcional diferente.

## Instrumentação atual

O protótipo já possui:

- Spring Boot Actuator;
- Micrometer com registro Prometheus;
- `/actuator/health`;
- `/actuator/prometheus`;
- `X-Correlation-ID` propagado por filtro e MDC;
- configuração Prometheus com coleta a cada 15 segundos.

Essa base deve ser preservada e ampliada. O compose atual ainda não representa
todo o ambiente experimental.

## Métricas HTTP e de processo

Utilizar métricas padrão do Spring Boot e JVM para obter:

- quantidade e duração de requisições HTTP por rota normalizada, método,
  status e resultado;
- latências p50, p95 e p99 calculadas no backend de métricas;
- throughput e taxa de erro;
- CPU do processo e do host;
- heap, non-heap, alocações e pausas de garbage collector;
- threads, conexões JDBC e saturação dos pools;
- tempo de inicialização e reinicializações.

Identificador de entrada, participante, fila, recurso, reserva e correlation ID
não podem ser labels de métrica, pois possuem cardinalidade não limitada.

## Métricas de domínio

Os nomes finais seguirão a convenção do Micrometer e sua tradução para
Prometheus. O conjunto lógico mínimo é:

| Métrica | Tipo | Dimensões limitadas |
| --- | --- | --- |
| entradas criadas | Counter | resultado |
| entradas aguardando | Gauge | estado agregado da aplicação |
| entradas concluídas | Counter | estado terminal |
| tempo de espera até hold | Timer | resultado |
| ciclos do worker | Counter | resultado |
| duração do ciclo do worker | Timer | resultado |
| entradas examinadas por ciclo | Distribution summary | resultado |
| conflitos e novas tentativas | Counter | operação |
| reservas criadas | Counter | resultado |
| reservas concluídas | Counter | `confirmed`, `cancelled`, `expired` |
| duração do hold | Timer | estado terminal |
| holds ativos | Gauge | estado agregado da aplicação |
| unidades do estoque | Gauge | `available`, `held`, `confirmed` |

Métricas agregadas não substituem a consulta transacional do domínio. Gauges
podem ficar temporariamente defasados, mas devem convergir sem reiniciar a
aplicação.

## Logs

Logs estruturados devem incluir:

- timestamp, nível, aplicação e versão;
- correlation ID;
- módulo e operação;
- resultado e duração quando aplicável;
- identificadores técnicos somente quando necessários ao diagnóstico.

Nunca registrar senha, credencial Bearer, token da entrada ou chave completa do
participante. Chaves de idempotência também não são registradas. Erros
inesperados possuem stack trace nos logs internos, mas a resposta HTTP permanece
sanitizada.

Eventos de domínio relevantes devem gerar um registro único depois do commit.
Tentativas revertidas podem ser registradas separadamente como diagnóstico, sem
ser contabilizadas como resultado de negócio.

## Saúde

- Liveness responde apenas se o processo consegue continuar executando.
- Readiness considera dependências obrigatórias, especialmente o banco.
- Falha do Prometheus não torna a API indisponível.
- O estado de uma fila ou falta de estoque não altera a saúde técnica da
  aplicação.

## Condições de comparabilidade

Cada variante futura deve usar:

1. o mesmo contrato HTTP versionado;
2. as mesmas regras e estados de domínio;
3. o mesmo conjunto inicial de recursos, filas e participantes;
4. a mesma distribuição de quantidades e padrão de chegada;
5. o mesmo ambiente de hardware ou limites de container;
6. versões registradas de runtime, banco e ferramentas;
7. a mesma política de aquecimento, duração e encerramento;
8. múltiplas repetições independentes;
9. relógios sincronizados e janela de coleta equivalente;
10. critérios iguais de sucesso funcional.

Configuração de intervalos, lotes e duração dos holds deve ser fixada por perfil
de carga e registrada com o resultado.

## Perfis de carga mínimos

Os valores numéricos serão definidos após o MVP funcionar, mas o gerador deve
cobrir:

- carga constante abaixo da capacidade;
- pico de ingresso muito acima da capacidade;
- quantidades variadas que provoquem espera por holds;
- confirmações, cancelamentos e expirações em proporções controladas;
- concorrência entre confirmação e expiração;
- período de esvaziamento após cessar novos ingressos.

Cada execução deve validar, além de desempenho:

- conservação do estoque;
- ausência de entradas ou reservas duplicadas;
- monotonicidade da sequência FIFO;
- quantidade esperada em cada estado terminal.

## Resultado de uma execução

O artefato experimental deve registrar:

- commit e variante arquitetural;
- configuração da aplicação e infraestrutura;
- seed ou arquivo da carga;
- horários de início, aquecimento, medição e término;
- séries e resumos das métricas;
- erros observados;
- validações das invariantes;
- observações sobre interferências externas.

Os cenários de granularidade grossa, intermediária ou fina serão definidos
somente depois do congelamento da linha de base. Este documento fixa o que deve
continuar comparável entre eles.
