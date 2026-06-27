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

- criar configuração de teste que não dependa de MySQL manual;
- substituir credenciais fixas por configuração externa;
- corrigir o tratamento de segredos e retirar o usuário demonstrativo do fluxo
  de produção;
- adotar migrações de banco versionadas;
- manter correlation ID, erros, health e Prometheus cobertos por testes.

Aceite: build e testes reproduzíveis em ambiente limpo; nenhum segredo real ou
senha em texto simples no caminho novo.

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

## Marco 6 — Contrato e observabilidade

- publicar OpenAPI de `/api/v1`;
- fechar limites, códigos de erro e retenção de idempotência;
- implementar métricas de domínio sem labels de alta cardinalidade;
- produzir logs estruturados e probes de saúde corretas;
- criar gerador de dados e carga totalmente automatizado.

Aceite: fluxo completo executável via API, métricas suficientes para explicar
cada perfil de carga e validação automática das invariantes após o teste.

## Marco 7 — Congelar a linha de base

- definir configuração experimental versionada;
- executar todos os perfis várias vezes;
- registrar desempenho, consumo e resultados funcionais;
- corrigir instabilidade antes de tomar a medição como referência;
- marcar a versão do monólito usada pelo estudo.

Aceite: execuções repetíveis com variação compreendida e contrato funcional
congelado.

## Etapa posterior — Variantes de granularidade

Somente após o Marco 7:

- escolher agrupamentos de módulos para cada variante;
- definir mecanismos distribuídos de consistência e entrega de eventos;
- preservar contrato, dados, carga e critérios funcionais;
- medir com o mesmo protocolo;
- comparar benefícios, custos e falhas introduzidas por cada granularidade.

Essa etapa exigirá uma decisão experimental própria. Este roadmap não assume
quantos microserviços existirão nem qual variante será superior.

## Definição de concluído para mudanças

Uma mudança funcional só está concluída quando:

- regras e contratos afetados estão documentados;
- testes unitários, integração e concorrência pertinentes passam;
- métricas e logs necessários foram incluídos;
- não há violação das dependências modulares;
- não há segredo ou dado sensível exposto;
- exemplos e OpenAPI correspondem ao comportamento observado.
