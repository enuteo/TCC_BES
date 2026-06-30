# Contexto experimental

O experimento compara quatro granularidades do mesmo sistema de sala de espera, estoque e reservas temporárias:

- `G0`: aplicação única com banco único.
- `G1`: separação entre núcleo residual e reservas.
- `G2`: separação adicional da gestão de filas.
- `G3`: separação adicional de identidade e acesso.

O comportamento esperado é o mesmo nos quatro grupos: entrada FIFO na fila, concessão de hold temporário quando há estoque suficiente, confirmação idempotente de reservas, expiração de holds e preservação das invariantes de estoque. A campanha foi desenhada para observar o custo operacional incremental de separar fronteiras mantendo o mesmo fluxo funcional.

O diretório raiz desta campanha é `C:\Users\Pardini\Organico\dev\TCC_BES\experiments\results\20260629-232719-g0-g3`. A matriz executada contém `36` runs observados pelo mesmo conjunto de fontes: k6, Prometheus, cAdvisor, logs do Compose e snapshots do banco de dados.

Os dados aqui são material de suporte. A interpretação acadêmica ainda deve considerar repetibilidade, variância, aquecimento da JVM, ruído do Docker Desktop, recursos da máquina, versões das imagens e validade estatística do número de repetições.
