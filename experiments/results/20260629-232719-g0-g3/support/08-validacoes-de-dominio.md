# Validações de domínio

As validações usam os snapshots dos bancos depois de cada run. Elas verificam conservação de estoque, ausência de participante ativo duplicado por fila, ausência de sequência duplicada por fila e vínculo entre entradas com reserva e reservas persistidas.

| Invariante | Passou | Falhou |
| --- | --- | --- |
| duplicate_active_participant | 36 | 0 |
| duplicate_queue_sequence | 36 | 0 |
| entry_reservation_links | 36 | 0 |
| stock_conservation | 36 | 0 |

Detalhes completos estão em `../aggregates/domain-validation.csv` e nos diretórios `domain/` de cada run.
