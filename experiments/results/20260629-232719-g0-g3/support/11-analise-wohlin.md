# Análise metodológica baseada em Wohlin

Este arquivo organiza a campanha no formato de apoio esperado para experimentos em Engenharia de Software. Ele não substitui a seção final do TCC, mas deixa explícito o desenho experimental, a operação, os dados observados e as ameaças à validade.

## Definição

Objeto de estudo: sistema de sala de espera com estoque e reservas temporárias, executado em quatro granularidades arquiteturais.

Propósito: avaliar, de forma descritiva e reprodutível, o efeito operacional da granularidade sobre latência, consumo de recursos, tráfego e preservação de invariantes de domínio.

Perspectiva: Engenharia de Software experimental, interessada no custo técnico de decomposição arquitetural sob comportamento funcional equivalente.

Foco de qualidade: desempenho HTTP, custo computacional, custo de comunicação, estabilidade funcional e validade das regras de domínio.

Contexto: execução local com Docker Desktop, k6, Prometheus, coleta `docker stats`, bancos PostgreSQL descartáveis e topologias Compose G0-G3.

## Planejamento

| Item | Valor |
| --- | --- |
| Fator principal | Granularidade arquitetural: G0, G1, G2 e G3. |
| Tratamentos | G0 monolítico; G1 separa reservas; G2 separa reservas e filas; G3 separa identidade, sala de espera, filas e reservas. |
| Perfis de carga | steady, contention e spike. |
| Repetições | 3 execuções independentes por combinação grupo/perfil. |
| Unidade experimental | Uma topologia Docker Compose descartável executando um perfil k6. |
| Variáveis dependentes | p95 HTTP, taxa de falha, checks k6, CPU média, memória média, bytes de rede, ICCE e invariantes de domínio. |
| Critério funcional | Run só é interpretável quando k6 termina com código 0 e as invariantes de domínio passam. |

## Operação

A matriz executada contém `36` runs. Runs com status `success`: `36`. Runs com validação de domínio positiva: `36`.

Janela operacional registrada no manifesto: início `2026-06-30T02:27:19.1406346Z`; fim `2026-06-30T04:49:32.8959053Z`.

A coleta de recursos usa `docker stats` como fonte consolidada porque o cAdvisor disponível no Docker Desktop registrou limitação de compatibilidade com a API do Docker 29. Prometheus permanece como fonte das métricas HTTP e de domínio expostas por Micrometer.

## Análise descritiva

| Grupo | Perfil | Runs | p95 ms | CPU | Mem GB | Rede bytes | ICCE | Domínio OK |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| G0 | contention | 3 | 20.035 | 0.361 | 0.447 | 64287130 | 1.000 | 3 |
| G0 | spike | 3 | 13.645 | 0.635 | 0.449 | 93768640 | 1.000 | 3 |
| G0 | steady | 3 | 15.466 | 0.258 | 0.445 | 52850953 | 1.000 | 3 |
| G1 | contention | 3 | 19.771 | 0.526 | 0.805 | 82759533 | 1.386 | 3 |
| G1 | spike | 3 | 12.244 | 0.596 | 0.794 | 114124570 | 1.230 | 3 |
| G1 | steady | 3 | 25.878 | 0.644 | 0.789 | 71072020 | 1.652 | 3 |
| G2 | contention | 3 | 34.660 | 1.232 | 1.136 | 91858243 | 2.097 | 3 |
| G2 | spike | 3 | 33.280 | 1.537 | 1.148 | 124308647 | 1.826 | 3 |
| G2 | steady | 3 | 44.285 | 1.246 | 1.111 | 76207783 | 2.442 | 3 |
| G3 | contention | 3 | 32.942 | 1.684 | 1.830 | 160048940 | 3.063 | 3 |
| G3 | spike | 3 | 28.531 | 1.948 | 1.765 | 226690127 | 2.604 | 3 |
| G3 | steady | 3 | 42.527 | 1.635 | 1.767 | 106691763 | 3.331 | 3 |

Leitura inicial dos dados:

- Todas as combinações executadas preservaram o comportamento funcional observado pelo k6 e pelas validações de domínio.
- O aumento de granularidade elevou o custo médio de CPU, memória e rede, especialmente em G2 e G3, como esperado pela introdução de mais processos, bancos e chamadas HTTP internas.
- O p95 HTTP permaneceu abaixo do limiar operacional configurado em todos os perfis, mas variou entre topologias e perfis de carga.
- O ICCE deve ser usado como índice auxiliar: ele consolida dimensões heterogêneas e precisa ser discutido junto das métricas individuais.

## Ameaças à validade

| Tipo | Risco | Mitigação |
| --- | --- | --- |
| Conclusão | Amostra de 3 repetições por combinação é adequada para triagem, mas limitada para inferência estatística forte. | Preservar dados brutos e ampliar repetições em campanhas futuras. |
| Construção | ICCE combina CPU, duração, memória e rede com média simples. | Reportar também as métricas individuais e justificar pesos se o índice virar evidência principal. |
| Interna | Ruído do Docker Desktop, aquecimento da JVM e estado da máquina podem afetar medições. | Registrar ambiente, repetir por perfil e isolar topologias com volumes descartáveis. |
| Externa | Carga sintética k6 pode não representar tráfego real de produção. | Tratar os perfis como cenários controlados e calibrar novos perfis quando houver workload realista. |
| Confiabilidade | cAdvisor não expôs dados de container por projeto nesta máquina. | Usar `docker stats` amostrado e documentar a troca de fonte. |
