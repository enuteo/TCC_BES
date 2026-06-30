# Roadmap

## Princípio de execução

O projeto deve estabilizar comportamento antes de distribuição. Cada marco
entrega um incremento executável e testável; não se inicia a comparação de
microserviços enquanto a linha de base monolítica não estiver congelada.

## Marco 0 — Contexto canônico

Estado: concluído com este conjunto documental.

- registrar objetivo, escopo e linguagem ubíqua;
- definir módulos, dependências e regras;
- fixar a API conceitual e as métricas necessárias;
- distinguir protótipo atual de arquitetura alvo.

Aceite: documentação navegável, sem contradições conhecidas e suficiente para
planejar os incrementos seguintes.

## Marco 1 — Estabilizar a fundação

- criar configuração de teste que não dependa de banco manual;
- substituir credenciais fixas por configuração externa;
- corrigir o tratamento de segredos e retirar o usuário demonstrativo do fluxo
  de produção;
- adotar migrações de banco versionadas;
- manter correlation ID, erros, health e Prometheus cobertos por testes.

Aceite: build e testes reproduzíveis em ambiente limpo; nenhum segredo real ou
senha em texto simples no caminho novo.

Estado: implementado parcialmente com PostgreSQL, Flyway, Testcontainers,
segredos por configuração e JWT para gestores. Em ambientes sem Docker, os
testes de integração são ignorados de forma explícita.

## Marco 2 — Estruturar o monólito modular

- criar os quatro módulos e suas APIs de aplicação;
- mover capacidades existentes para Identidade e Acesso ou infraestrutura
  transversal;
- estabelecer testes de arquitetura para dependências e acesso a dados;
- manter um único processo, banco e mecanismo de implantação.

Aceite: nenhuma dependência proibida entre pacotes e nenhuma consulta a tabela
de outro módulo.

## Marco 3 — Gestão de filas e inventário

- implementar criação e consulta de recurso;
- implementar criação, configuração e transições da fila;
- garantir exclusividade de recurso por fila não encerrada;
- implementar autorização do gestor e idempotência administrativa;
- instrumentar operações e invariantes de estoque.

Aceite: testes unitários e de integração cobrem transições válidas, conflitos,
isolamento por gestor e conservação do estoque.

## Marco 4 — Sala de espera

- implementar ingresso idempotente e token da entrada;
- impor quantidade válida e uma entrada ativa por participante;
- implementar sequência FIFO e consulta de posição;
- implementar cancelamento seguro;
- cobrir chamadas concorrentes e repetidas.

Aceite: nenhum ingresso duplicado, sequência estável e token incapaz de acessar
outra entrada.

## Marco 5 — Worker e reservas

- implementar seleção periódica em lotes;
- criar hold atomicamente com atualização da entrada;
- implementar confirmação, cancelamento e expiração;
- aplicar a regra de espera ou `UNFULFILLABLE` por capacidade recuperável;
- publicar eventos somente depois do commit;
- testar corridas entre ciclos, confirmação e expiração.

Aceite: estoque nunca negativo, conservação da quantidade em toda execução,
apenas um resultado terminal por reserva e FIFO preservado.

Estado: implementado no G0 com worker local, holds, confirmação, cancelamento,
expiração e testes de integração para o fluxo principal quando Docker está
disponível.

## Marco 6 — Contrato e observabilidade

- publicar OpenAPI de `/api/v1`;
- fechar limites, códigos de erro e retenção de idempotência;
- implementar métricas de domínio sem labels de alta cardinalidade;
- produzir logs estruturados e probes de saúde corretas;
- criar gerador de dados e carga totalmente automatizado.

Aceite: fluxo completo executável via API, métricas suficientes para explicar
cada perfil de carga e validação automática das invariantes após o teste.

Estado: iniciado com métricas de domínio, Prometheus, cAdvisor, k6 e cálculo
do ICCE em `experiments/`.

## Marco 7 — Congelar a linha de base

- definir configuração experimental versionada;
- executar todos os perfis várias vezes;
- registrar desempenho, consumo e resultados funcionais;
- corrigir instabilidade antes de tomar a medição como referência;
- marcar a versão do monólito usada pelo estudo.

Aceite: execuções repetíveis com variação compreendida e contrato funcional
congelado.

## Etapa posterior — Variantes de granularidade

Após o G0 estar funcional, foram criadas as primeiras variantes executáveis:
G1 extrai Inventário e Reservas; G2 extrai Inventário e Reservas e Gestão de
Filas; G3 extrai Identidade e Acesso, Sala de Espera, Gestão de Filas e
Inventário e Reservas. Todas preservam `/api/v1` no residual e usam HTTP
interno entre serviços com bancos separados.

Ainda permanecem nesta etapa:

- executar os perfis de carga completos para cada variante;
- validar invariantes funcionais depois de cada execução;
- registrar configuração, commit, métricas e interferências;
- preservar contrato, dados, carga e critérios funcionais;
- medir com o mesmo protocolo;
- comparar benefícios, custos e falhas introduzidas por cada granularidade.

G1/G2/G3 ainda não são resultados do TCC por si só; tornam-se resultados somente
depois das execuções repetidas e validadas pelo protocolo experimental.

## Definição de concluído para mudanças

Uma mudança funcional só está concluída quando:

- regras e contratos afetados estão documentados;
- testes unitários, integração e concorrência pertinentes passam;
- métricas e logs necessários foram incluídos;
- não há violação das dependências modulares;
- não há segredo ou dado sensível exposto;
- exemplos e OpenAPI correspondem ao comportamento observado.
