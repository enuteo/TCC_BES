# Limitações e cuidados

- G1, G2 e G3 representam topologias experimentais e scaffolding de execução. A validade experimental depende de os adaptadores HTTP internos preservarem a semântica do fluxo completo entre sala de espera, gestão de filas e reservas.
- O Docker Desktop adiciona ruído de virtualização, agendamento e I/O que deve ser descrito no TCC.
- A JVM pode ter efeito de aquecimento; a ordem de execução e repetições devem ser consideradas na análise.
- Os perfis usam dados sintéticos gerados pelo k6. Eles exercitam o fluxo principal, mas não representam uma população real.
- Métricas de rede coletadas por cAdvisor podem incluir tráfego interno de health checks, scraping e infraestrutura da topologia.
- O ICCE usa média simples de dimensões heterogêneas; ele deve ser tratado como índice auxiliar, não como prova isolada.
- Runs com falhas de domínio, falhas k6 ou erros Prometheus devem ser separados antes de qualquer conclusão comparativa.
- A campanha foi registrada com o manifesto `2026-06-30T02:27:19.1406346Z` e deve ser reproduzida com versões equivalentes de Docker, imagens, JDK, Maven e código.
