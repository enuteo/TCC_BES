# Prometheus e cAdvisor

As métricas HTTP e de domínio vêm da instrumentação Micrometer exposta pelas aplicações e consultada via Prometheus. As métricas de recursos tentam usar cAdvisor; quando cAdvisor não expõe containers por projeto no Docker Desktop, o pacote usa `docker stats` amostrado durante o k6 como fonte consolidada de CPU, memória e rede.

Resumo por grupo e perfil:

| Grupo | Perfil | Runs | CPU média | Mem GB média |
| --- | --- | --- | --- | --- |
| G0 | contention | 3 | 0.361 | 0.447 |
| G0 | spike | 3 | 0.635 | 0.449 |
| G0 | steady | 3 | 0.258 | 0.445 |
| G1 | contention | 3 | 0.526 | 0.805 |
| G1 | spike | 3 | 0.596 | 0.794 |
| G1 | steady | 3 | 0.644 | 0.789 |
| G2 | contention | 3 | 1.232 | 1.136 |
| G2 | spike | 3 | 1.537 | 1.148 |
| G2 | steady | 3 | 1.246 | 1.111 |
| G3 | contention | 3 | 1.684 | 1.830 |
| G3 | spike | 3 | 1.948 | 1.765 |
| G3 | steady | 3 | 1.635 | 1.767 |

Resumo por run:

| Grupo | Perfil | Run | Fonte | CPU | Mem GB | RX bytes | TX bytes | HTTP rps |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| G0 | contention | 1 | docker-stats | 0.280 | 0.455 | 30654180 | 33735090 | 71.957 |
| G0 | contention | 2 | docker-stats | 0.284 | 0.445 | 30750340 | 33833140 | 71.962 |
| G0 | contention | 3 | docker-stats | 0.518 | 0.443 | 30452880 | 33435760 | 77.512 |
| G0 | spike | 1 | docker-stats | 0.638 | 0.458 | 44254590 | 48936470 | 126.534 |
| G0 | spike | 2 | docker-stats | 0.651 | 0.439 | 44373450 | 49054590 | 116.404 |
| G0 | spike | 3 | docker-stats | 0.616 | 0.450 | 44952880 | 49733940 | 115.417 |
| G0 | steady | 1 | docker-stats | 0.297 | 0.442 | 25652580 | 27115740 | 34.633 |
| G0 | steady | 2 | docker-stats | 0.231 | 0.444 | 25252080 | 26745240 | 34.367 |
| G0 | steady | 3 | docker-stats | 0.246 | 0.449 | 26152020 | 27635200 | 35.805 |
| G1 | contention | 1 | docker-stats | 0.637 | 0.807 | 39280200 | 42201970 | 102.101 |
| G1 | contention | 2 | docker-stats | 0.494 | 0.801 | 40011240 | 43032300 | 103.419 |
| G1 | contention | 3 | docker-stats | 0.448 | 0.808 | 40396100 | 43356790 | 104.488 |
| G1 | spike | 1 | docker-stats | 0.556 | 0.781 | 55339600 | 59791560 | 161.488 |
| G1 | spike | 2 | docker-stats | 0.558 | 0.797 | 55238300 | 59710120 | 161.444 |
| G1 | spike | 3 | docker-stats | 0.673 | 0.803 | 53945640 | 58348490 | 156.681 |
| G1 | steady | 1 | docker-stats | 0.653 | 0.799 | 34907240 | 36309790 | 67.085 |
| G1 | steady | 2 | docker-stats | 0.688 | 0.781 | 34366100 | 35767110 | 66.375 |
| G1 | steady | 3 | docker-stats | 0.591 | 0.788 | 35157620 | 36708200 | 67.867 |
| G2 | contention | 1 | docker-stats | 1.252 | 1.143 | 44610580 | 47464640 | 109.067 |
| G2 | contention | 2 | docker-stats | 1.191 | 1.122 | 44538790 | 47402220 | 114.470 |
| G2 | contention | 3 | docker-stats | 1.253 | 1.143 | 44382690 | 47175810 | 114.300 |
| G2 | spike | 1 | docker-stats | 1.622 | 1.140 | 59077790 | 63452180 | 169.437 |
| G2 | spike | 2 | docker-stats | 1.507 | 1.173 | 60344650 | 64717940 | 170.632 |
| G2 | spike | 3 | docker-stats | 1.480 | 1.130 | 60593920 | 64739460 | 172.002 |
| G2 | steady | 1 | docker-stats | 1.389 | 1.127 | 36454750 | 37849630 | 70.799 |
| G2 | steady | 2 | docker-stats | 1.191 | 1.094 | 37556890 | 38931870 | 71.168 |
| G2 | steady | 3 | docker-stats | 1.158 | 1.113 | 38213780 | 39616430 | 71.733 |
| G3 | contention | 1 | docker-stats | 1.784 | 1.853 | 78636610 | 81365600 | 232.134 |
| G3 | contention | 2 | docker-stats | 1.694 | 1.825 | 78563590 | 81482740 | 237.811 |
| G3 | contention | 3 | docker-stats | 1.573 | 1.811 | 78613090 | 81485190 | 235.903 |
| G3 | spike | 1 | docker-stats | 1.905 | 1.757 | 110785600 | 115095460 | 373.730 |
| G3 | spike | 2 | docker-stats | 1.873 | 1.778 | 111325860 | 115724700 | 373.269 |
| G3 | spike | 3 | docker-stats | 2.066 | 1.760 | 111375220 | 115763540 | 372.729 |
| G3 | steady | 1 | docker-stats | 1.733 | 1.745 | 52493160 | 53997790 | 119.474 |
| G3 | steady | 2 | docker-stats | 1.719 | 1.772 | 52390050 | 53804800 | 121.004 |
| G3 | steady | 3 | docker-stats | 1.453 | 1.784 | 52945870 | 54443620 | 124.956 |
