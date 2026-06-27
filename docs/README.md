# Documentação do projeto

Esta pasta é a fonte de verdade para o objetivo, o domínio e a arquitetura alvo
do TCC. Ela separa deliberadamente o protótipo existente do sistema que será
construído.

## Ordem de leitura

1. [Visão e escopo](01-visao-e-escopo.md): problema, objetivo acadêmico,
   atores, capacidades e exclusões.
2. [Domínio e regras](02-dominio-e-regras.md): linguagem ubíqua, agregados,
   estados, invariantes e fluxo principal.
3. [Arquitetura](03-arquitetura.md): estado atual, monólito modular alvo,
   módulos e dependências.
4. [API conceitual](04-api-conceitual.md): recursos HTTP, autenticação,
   idempotência e erros.
5. [Observabilidade e experimentos](05-observabilidade-e-experimentos.md):
   métricas e requisitos de comparabilidade.
6. [Roadmap](06-roadmap.md): sequência de entrega do monólito e preparação dos
   experimentos.

## Resumo da decisão

- Produto: API genérica de sala de espera para recursos limitados.
- Política inicial: FIFO com quantidade variável por participante.
- Resultado do atendimento: hold temporário, posteriormente confirmado,
  cancelado ou expirado.
- Execução inicial: um processo, um banco e módulos separados por contexto.
- Evolução: extrações futuras em diferentes granularidades, sem cenários
  fechados nesta fase.
- Foco das métricas: execução, comportamento da fila e consumo de recursos.

## Estado documental

Os documentos descrevem dois momentos:

- **Atual**: fatos verificáveis no código existente.
- **Alvo do MVP**: decisões que orientarão a próxima implementação.

Uma capacidade alvo não deve ser inferida como disponível na API atual.
