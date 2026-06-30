# Topologias

| Grupo | Escopo operacional | Nota |
| --- | --- | --- |
| G0 | API, fila, estoque, reservas e identidade no mesmo processo e banco. | Comparável apenas se as validações de domínio passarem e o mesmo fluxo k6 for mantido. |
| G1 | Reservas separadas do residual; residual preserva identidade, filas e sala de espera. | Comparável apenas se as validações de domínio passarem e o mesmo fluxo k6 for mantido. |
| G2 | Reservas e gestão de filas separadas; residual mantém identidade e sala de espera. | Comparável apenas se as validações de domínio passarem e o mesmo fluxo k6 for mantido. |
| G3 | Identidade, sala de espera, gestão de filas e reservas separados. | Comparável apenas se as validações de domínio passarem e o mesmo fluxo k6 for mantido. |
