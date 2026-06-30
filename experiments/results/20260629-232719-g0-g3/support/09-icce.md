# ICCE

O ICCE é calculado por carga usando `G0` como baseline da mesma carga. Para cada run, são normalizados CPU média, duração, memória média e bytes de rede totais. O índice final é a média simples dessas razões.

Essa versão é um artefato de suporte: ela explicita o cálculo e os insumos, mas não substitui análise estatística nem discussão de validade.

| Grupo | Carga | Run | Fonte | CPU | Duração | Mem GB | Rede bytes | ICCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| G0 | contention | 1 | docker-stats | 0.280 | 182.696 | 0.455 | 64389270 | 0.948 |
| G0 | contention | 2 | docker-stats | 0.284 | 183.786 | 0.445 | 64583480 | 0.947 |
| G0 | contention | 3 | docker-stats | 0.518 | 183.375 | 0.443 | 63888640 | 1.105 |
| G0 | spike | 1 | docker-stats | 0.638 | 182.121 | 0.458 | 93191060 | 1.005 |
| G0 | spike | 2 | docker-stats | 0.651 | 181.926 | 0.439 | 93428040 | 0.999 |
| G0 | spike | 3 | docker-stats | 0.616 | 182.639 | 0.450 | 94686820 | 0.996 |
| G0 | steady | 1 | docker-stats | 0.297 | 184.029 | 0.442 | 52768320 | 1.037 |
| G0 | steady | 2 | docker-stats | 0.231 | 182.865 | 0.444 | 51997320 | 0.969 |
| G0 | steady | 3 | docker-stats | 0.246 | 182.058 | 0.449 | 53787220 | 0.994 |
| G1 | contention | 1 | docker-stats | 0.637 | 183.352 | 0.807 | 81482170 | 1.459 |
| G1 | contention | 2 | docker-stats | 0.494 | 183.084 | 0.801 | 83043540 | 1.362 |
| G1 | contention | 3 | docker-stats | 0.448 | 182.820 | 0.808 | 83752890 | 1.337 |
| G1 | spike | 1 | docker-stats | 0.556 | 181.717 | 0.781 | 115131160 | 1.210 |
| G1 | spike | 2 | docker-stats | 0.558 | 181.747 | 0.797 | 114948420 | 1.219 |
| G1 | spike | 3 | docker-stats | 0.673 | 182.279 | 0.803 | 112294130 | 1.262 |
| G1 | steady | 1 | docker-stats | 0.653 | 182.291 | 0.799 | 71217030 | 1.667 |
| G1 | steady | 2 | docker-stats | 0.688 | 182.053 | 0.781 | 70133210 | 1.686 |
| G1 | steady | 3 | docker-stats | 0.591 | 182.302 | 0.788 | 71865820 | 1.604 |
| G2 | contention | 1 | docker-stats | 1.252 | 183.967 | 1.143 | 92075220 | 2.116 |
| G2 | contention | 2 | docker-stats | 1.191 | 184.959 | 1.122 | 91941010 | 2.062 |
| G2 | contention | 3 | docker-stats | 1.253 | 183.516 | 1.143 | 91558500 | 2.114 |
| G2 | spike | 1 | docker-stats | 1.622 | 182.372 | 1.140 | 122529970 | 1.851 |
| G2 | spike | 2 | docker-stats | 1.507 | 182.739 | 1.173 | 125062590 | 1.831 |
| G2 | spike | 3 | docker-stats | 1.480 | 182.438 | 1.130 | 125333380 | 1.797 |
| G2 | steady | 1 | docker-stats | 1.389 | 182.983 | 1.127 | 74304380 | 2.581 |
| G2 | steady | 2 | docker-stats | 1.191 | 183.619 | 1.094 | 76488760 | 2.381 |
| G2 | steady | 3 | docker-stats | 1.158 | 182.410 | 1.113 | 77830210 | 2.365 |
| G3 | contention | 1 | docker-stats | 1.784 | 183.873 | 1.853 | 160002210 | 3.145 |
| G3 | contention | 2 | docker-stats | 1.694 | 184.249 | 1.825 | 160046330 | 3.068 |
| G3 | contention | 3 | docker-stats | 1.573 | 183.083 | 1.811 | 160098280 | 2.975 |
| G3 | spike | 1 | docker-stats | 1.905 | 181.771 | 1.757 | 225881060 | 2.580 |
| G3 | spike | 2 | docker-stats | 1.873 | 182.290 | 1.778 | 227050560 | 2.583 |
| G3 | spike | 3 | docker-stats | 2.066 | 182.101 | 1.760 | 227138760 | 2.649 |
| G3 | steady | 1 | docker-stats | 1.733 | 183.667 | 1.745 | 106490950 | 3.414 |
| G3 | steady | 2 | docker-stats | 1.719 | 183.245 | 1.772 | 106194850 | 3.413 |
| G3 | steady | 3 | docker-stats | 1.453 | 182.863 | 1.784 | 107389490 | 3.167 |
