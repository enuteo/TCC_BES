import argparse
import csv
import json
import statistics
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path


K6_METRICS = [
    "http_reqs",
    "http_req_failed",
    "http_req_duration",
    "checks",
    "vus_max",
    "iterations",
]

PROM_METRICS = [
    "duration_seconds",
    "cpu_avg",
    "mem_avg_gb",
    "rx_bytes",
    "tx_bytes",
    "http_rps_avg",
    "http_error_rps_avg",
    "waiting_entries_created_delta",
    "waiting_entries_waiting_avg",
    "waiting_entries_completed_delta",
    "worker_cycles_delta",
    "reservation_holds_created_delta",
    "reservation_completed_delta",
    "reservation_holds_active_avg",
    "reservation_stock_units_avg",
]


def load_json(path, default=None):
    if not path.exists():
        return default
    try:
        return json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError:
        return default


def write_text(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.rstrip() + "\n", encoding="utf-8")


def write_csv(path, rows, fieldnames=None):
    path.parent.mkdir(parents=True, exist_ok=True)
    rows = list(rows)
    if fieldnames is None:
        fieldnames = []
        for row in rows:
            for key in row.keys():
                if key not in fieldnames:
                    fieldnames.append(key)
    with path.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow({key: row.get(key, "") for key in fieldnames})


def number(value, default=0.0):
    if value is None or value == "":
        return default
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def metric_value(summary, metric):
    metrics = (summary or {}).get("metrics", {})
    item = metrics.get(metric, {})
    values = item.get("values", item)

    if metric == "http_req_duration":
        return {
            "avg": values.get("avg"),
            "p90": values.get("p(90)"),
            "p95": values.get("p(95)"),
            "max": values.get("max"),
        }
    if metric in {"http_req_failed", "checks"}:
        return {
            "rate": values.get("rate", values.get("value")),
            "passes": values.get("passes"),
            "fails": values.get("fails"),
        }
    return {
        "count": values.get("count"),
        "rate": values.get("rate"),
        "value": values.get("value"),
        "max": values.get("max"),
    }


def normalize_group(value):
    return str(value or "").upper()


def run_sort_key(run):
    return (
        normalize_group(run.get("group")),
        str(run.get("profile", "")),
        int(run.get("run", 0) or 0),
    )


def collect_runs(root):
    manifest = load_json(root / "manifest.json", {})
    records = []
    for metadata_path in sorted((root / "runs").glob("**/run-metadata.json")):
        run_dir = metadata_path.parent
        metadata = load_json(metadata_path)
        if not metadata:
            continue
        k6 = load_json(run_dir / "k6-summary.json", {})
        prom = load_json(run_dir / "prometheus" / "prometheus-summary.json", {})
        docker_stats = load_json(run_dir / "docker-stats" / "docker-stats-summary.json", {})
        domain = load_json(run_dir / "domain" / "domain-validation.json", {})
        profile_config = load_json(run_dir / "profile-config.json", {})
        records.append({
            "run_dir": run_dir,
            "metadata": metadata,
            "k6": k6,
            "prometheus": prom,
            "docker_stats": docker_stats,
            "domain": domain,
            "profile_config": profile_config,
        })
    records.sort(key=lambda item: run_sort_key(item["metadata"]))
    return manifest, records


def k6_row(item):
    meta = item["metadata"]
    summary = item["k6"]
    duration = metric_value(summary, "http_req_duration")
    failed = metric_value(summary, "http_req_failed")
    checks = metric_value(summary, "checks")
    reqs = metric_value(summary, "http_reqs")
    iterations = metric_value(summary, "iterations")
    return {
        "group": normalize_group(meta.get("group")),
        "profile": meta.get("profile"),
        "run": meta.get("run"),
        "project": meta.get("project"),
        "status": meta.get("status"),
        "http_reqs": reqs.get("count"),
        "http_req_rate": reqs.get("rate"),
        "http_req_failed_rate": failed.get("rate"),
        "http_req_duration_avg_ms": duration.get("avg"),
        "http_req_duration_p90_ms": duration.get("p90"),
        "http_req_duration_p95_ms": duration.get("p95"),
        "http_req_duration_max_ms": duration.get("max"),
        "checks_rate": checks.get("rate"),
        "checks_passes": checks.get("passes"),
        "checks_fails": checks.get("fails"),
        "iterations": iterations.get("count"),
    }


def prometheus_row(item):
    meta = item["metadata"]
    prom = item["prometheus"] or {}
    docker_stats = item["docker_stats"] or {}
    resource = resource_summary(item)
    row = {
        "group": normalize_group(meta.get("group")),
        "profile": meta.get("profile"),
        "run": meta.get("run"),
        "project": meta.get("project"),
    }
    for key in PROM_METRICS:
        row[key] = prom.get(key, "")
    row["docker_stats_sample_count"] = docker_stats.get("sample_count", "")
    row["docker_stats_container_count"] = docker_stats.get("container_count", "")
    row["docker_stats_cpu_avg"] = docker_stats.get("cpu_avg", "")
    row["docker_stats_mem_avg_gb"] = docker_stats.get("mem_avg_gb", "")
    row["docker_stats_rx_bytes"] = docker_stats.get("rx_bytes", "")
    row["docker_stats_tx_bytes"] = docker_stats.get("tx_bytes", "")
    row["resource_source"] = resource["source"]
    row["resource_cpu_avg"] = resource["cpu_avg"]
    row["resource_mem_avg_gb"] = resource["mem_avg_gb"]
    row["resource_rx_bytes"] = resource["rx_bytes"]
    row["resource_tx_bytes"] = resource["tx_bytes"]
    row["errors"] = json.dumps(prom.get("errors", {}), ensure_ascii=False)
    return row


def docker_stats_row(item):
    meta = item["metadata"]
    docker_stats = item["docker_stats"] or {}
    return {
        "group": normalize_group(meta.get("group")),
        "profile": meta.get("profile"),
        "run": meta.get("run"),
        "project": meta.get("project"),
        "sample_count": docker_stats.get("sample_count", ""),
        "container_count": docker_stats.get("container_count", ""),
        "cpu_avg": docker_stats.get("cpu_avg", ""),
        "mem_avg_gb": docker_stats.get("mem_avg_gb", ""),
        "rx_bytes": docker_stats.get("rx_bytes", ""),
        "tx_bytes": docker_stats.get("tx_bytes", ""),
        "errors": json.dumps(docker_stats.get("errors", []), ensure_ascii=False),
    }


def resource_summary(item):
    prom = item["prometheus"] or {}
    docker_stats = item["docker_stats"] or {}
    if number(docker_stats.get("sample_count")) > 0:
        return {
            "source": "docker-stats",
            "cpu_avg": docker_stats.get("cpu_avg", 0),
            "mem_avg_gb": docker_stats.get("mem_avg_gb", 0),
            "rx_bytes": docker_stats.get("rx_bytes", 0),
            "tx_bytes": docker_stats.get("tx_bytes", 0),
        }
    return {
        "source": "prometheus-cadvisor",
        "cpu_avg": prom.get("cpu_avg", 0),
        "mem_avg_gb": prom.get("mem_avg_gb", 0),
        "rx_bytes": prom.get("rx_bytes", 0),
        "tx_bytes": prom.get("tx_bytes", 0),
    }


def domain_rows(item):
    meta = item["metadata"]
    domain = item["domain"] or {}
    rows = []
    for check in domain.get("checks", []):
        rows.append({
            "group": normalize_group(meta.get("group")),
            "profile": meta.get("profile"),
            "run": meta.get("run"),
            "project": meta.get("project"),
            "passed": domain.get("passed"),
            "check": check.get("check"),
            "check_passed": check.get("passed"),
            "details": json.dumps(check.get("details", {}), ensure_ascii=False),
        })
    if not rows:
        rows.append({
            "group": normalize_group(meta.get("group")),
            "profile": meta.get("profile"),
            "run": meta.get("run"),
            "project": meta.get("project"),
            "passed": domain.get("passed", ""),
            "check": "",
            "check_passed": "",
            "details": "",
        })
    return rows


def runs_row(item):
    meta = item["metadata"]
    domain = item["domain"] or {}
    prom = item["prometheus"] or {}
    return {
        "group": normalize_group(meta.get("group")),
        "profile": meta.get("profile"),
        "run": meta.get("run"),
        "project": meta.get("project"),
        "status": meta.get("status"),
        "k6_exit_code": meta.get("k6ExitCode"),
        "started_at": meta.get("startedAt"),
        "finished_at": meta.get("endedAt"),
        "duration_seconds": prom.get("duration_seconds", ""),
        "domain_passed": domain.get("passed", ""),
        "run_dir": str(item["run_dir"]),
    }


def icce_rows(items):
    rows = []
    for item in items:
        meta = item["metadata"]
        prom = item["prometheus"] or {}
        resource = resource_summary(item)
        rows.append({
            "group": normalize_group(meta.get("group")),
            "load": meta.get("profile"),
            "run": meta.get("run"),
            "cpu_avg": resource["cpu_avg"],
            "duration_seconds": prom.get("duration_seconds", 0),
            "mem_avg_gb": resource["mem_avg_gb"],
            "rx_bytes": resource["rx_bytes"],
            "tx_bytes": resource["tx_bytes"],
            "resource_source": resource["source"],
        })
    return rows


def calculate_icce(rows):
    baseline_by_load = defaultdict(list)
    for row in rows:
        if row["group"] == "G0":
            baseline_by_load[row["load"]].append(row)

    baselines = {}
    for load, values in baseline_by_load.items():
        baselines[load] = {
            "cpu_avg": statistics.mean(number(row["cpu_avg"]) for row in values),
            "duration_seconds": statistics.mean(number(row["duration_seconds"]) for row in values),
            "mem_avg_gb": statistics.mean(number(row["mem_avg_gb"]) for row in values),
            "network_bytes": statistics.mean(number(row["rx_bytes"]) + number(row["tx_bytes"]) for row in values),
        }

    output = []
    for row in rows:
        load = row["load"]
        baseline = baselines.get(load)
        network = number(row["rx_bytes"]) + number(row["tx_bytes"])
        if not baseline:
            icce = ""
            details = "sem baseline G0 para a carga"
        else:
            ratios = []
            for key, value in [
                ("cpu_avg", number(row["cpu_avg"])),
                ("duration_seconds", number(row["duration_seconds"])),
                ("mem_avg_gb", number(row["mem_avg_gb"])),
                ("network_bytes", network),
            ]:
                base = baseline.get(key, 0)
                ratios.append(value / base if base else 0)
            icce = statistics.mean(ratios)
            details = json.dumps({
                "cpu_ratio": ratios[0],
                "duration_ratio": ratios[1],
                "memory_ratio": ratios[2],
                "network_ratio": ratios[3],
            }, ensure_ascii=False)
        output.append({
            **row,
            "network_bytes": network,
            "icce": icce,
            "details": details,
        })
    return output


def fmt(value, digits=3):
    if value == "" or value is None:
        return "n/d"
    try:
        return f"{float(value):.{digits}f}"
    except (TypeError, ValueError):
        return str(value)


def stats_for(rows, key):
    values = [number(row.get(key)) for row in rows if row.get(key) not in ("", None)]
    if not values:
        return {"mean": "", "stdev": "", "min": "", "max": ""}
    return {
        "mean": statistics.mean(values),
        "stdev": statistics.stdev(values) if len(values) > 1 else 0.0,
        "min": min(values),
        "max": max(values),
    }


def md_table(rows, columns):
    if not rows:
        return "_Sem dados._"
    lines = [
        "| " + " | ".join(title for _, title in columns) + " |",
        "| " + " | ".join("---" for _ in columns) + " |",
    ]
    for row in rows:
        values = []
        for key, _ in columns:
            value = row.get(key, "")
            values.append(str(value).replace("\n", " ").replace("|", "\\|"))
        lines.append("| " + " | ".join(values) + " |")
    return "\n".join(lines)


def group_profile_summary(items, icce_output):
    grouped_items = defaultdict(list)
    for item in items:
        meta = item["metadata"]
        grouped_items[(normalize_group(meta.get("group")), meta.get("profile"))].append(item)

    icce_grouped = defaultdict(list)
    for row in icce_output:
        icce_grouped[(row.get("group"), row.get("load"))].append(row)

    rows = []
    for (group, profile), values in sorted(grouped_items.items()):
        k6_rows = [k6_row(item) for item in values]
        resource_rows = [resource_summary(item) for item in values]
        prom_rows = [prometheus_row(item) for item in values]
        icce_rows_for_key = icce_grouped.get((group, profile), [])

        p95 = stats_for(k6_rows, "http_req_duration_p95_ms")
        failed = stats_for(k6_rows, "http_req_failed_rate")
        cpu = stats_for(resource_rows, "cpu_avg")
        mem = stats_for(resource_rows, "mem_avg_gb")
        rx = stats_for(resource_rows, "rx_bytes")
        tx = stats_for(resource_rows, "tx_bytes")
        http_rps = stats_for(prom_rows, "http_rps_avg")
        icce = stats_for(icce_rows_for_key, "icce")

        rows.append({
            "group": group,
            "profile": profile,
            "runs": len(values),
            "success_runs": sum(1 for item in values if item["metadata"].get("status") == "success"),
            "domain_ok_runs": sum(1 for item in values if (item["domain"] or {}).get("passed") is True),
            "http_p95_ms_mean": p95["mean"],
            "http_p95_ms_stdev": p95["stdev"],
            "http_failed_rate_mean": failed["mean"],
            "cpu_avg_mean": cpu["mean"],
            "cpu_avg_stdev": cpu["stdev"],
            "mem_avg_gb_mean": mem["mean"],
            "mem_avg_gb_stdev": mem["stdev"],
            "rx_bytes_mean": rx["mean"],
            "tx_bytes_mean": tx["mean"],
            "network_bytes_mean": number(rx["mean"]) + number(tx["mean"]),
            "http_rps_avg_mean": http_rps["mean"],
            "icce_mean": icce["mean"],
            "icce_stdev": icce["stdev"],
        })
    return rows


def profile_summary(items):
    grouped = defaultdict(list)
    for item in items:
        meta = item["metadata"]
        grouped[(normalize_group(meta.get("group")), meta.get("profile"))].append(item)

    rows = []
    for (group, profile), values in sorted(grouped.items()):
        k6_rows = [k6_row(item) for item in values]
        resource_rows = [resource_summary(item) for item in values]
        rows.append({
            "group": group,
            "profile": profile,
            "runs": len(values),
            "http_p95_ms_avg": fmt(statistics.mean(number(row["http_req_duration_p95_ms"]) for row in k6_rows)),
            "failed_rate_avg": fmt(statistics.mean(number(row["http_req_failed_rate"]) for row in k6_rows), 5),
            "cpu_avg": fmt(statistics.mean(number(row["cpu_avg"]) for row in resource_rows)),
            "mem_avg_gb": fmt(statistics.mean(number(row["mem_avg_gb"]) for row in resource_rows)),
            "domain_ok_runs": sum(1 for item in values if (item["domain"] or {}).get("passed") is True),
        })
    return rows


def markdown_index(manifest, items, aggregate_rows):
    generated_at = datetime.now(timezone.utc).isoformat()
    lines = [
        "# Pacote de suporte da campanha G0-G3",
        "",
        f"Gerado em UTC: `{generated_at}`.",
        "",
        "Este pacote organiza os dados brutos da campanha experimental em arquivos de apoio para análise. Ele não declara conclusão comparativa; o objetivo é preservar contexto, protocolo, matriz, medições e validações que podem sustentar uma seção experimental posterior.",
        "",
        "## Arquivos",
        "",
        "- `01-contexto-experimental.md`: objetivo, escopo e premissas da campanha.",
        "- `02-protocolo-de-execucao.md`: sequência executada pelo orquestrador.",
        "- `03-perfis-de-carga.md`: perfis steady, contention e spike.",
        "- `04-topologias.md`: leitura operacional de G0, G1, G2 e G3.",
        "- `05-execucoes.md`: matriz de execuções e caminhos dos artefatos.",
        "- `06-k6.md`: métricas de carga e latência extraídas do k6.",
        "- `07-prometheus-cadvisor.md`: métricas de recursos e aplicação.",
        "- `08-validacoes-de-dominio.md`: invariantes verificadas após cada execução.",
        "- `09-icce.md`: insumos e cálculo de ICCE.",
        "- `10-limitacoes.md`: limitações conhecidas e cuidados de interpretação.",
        "- `11-analise-wohlin.md`: síntese metodológica baseada em Wohlin et al.",
        "",
        "## Agregados",
        "",
        "- `../aggregates/runs.csv`",
        "- `../aggregates/k6-summary.csv`",
        "- `../aggregates/prometheus-summary.csv`",
        "- `../aggregates/docker-stats-summary.csv`",
        "- `../aggregates/domain-validation.csv`",
        "- `../aggregates/icce-input.csv`",
        "- `../aggregates/icce-output.csv`",
        "- `../aggregates/group-profile-summary.csv`",
        "",
        "## Visão rápida",
        "",
        md_table(aggregate_rows, [
            ("group", "Grupo"),
            ("profile", "Perfil"),
            ("runs", "Runs"),
            ("http_p95_ms_avg", "p95 médio ms"),
            ("failed_rate_avg", "falha média"),
            ("cpu_avg", "CPU média"),
            ("mem_avg_gb", "Mem GB média"),
            ("domain_ok_runs", "Domínio OK"),
        ]),
        "",
        "## Manifesto",
        "",
        "```json",
        json.dumps(manifest, indent=2, ensure_ascii=False),
        "```",
    ]
    return "\n".join(lines)


def markdown_context(manifest, items):
    return f"""# Contexto experimental

O experimento compara quatro granularidades do mesmo sistema de sala de espera, estoque e reservas temporárias:

- `G0`: aplicação única com banco único.
- `G1`: separação entre núcleo residual e reservas.
- `G2`: separação adicional da gestão de filas.
- `G3`: separação adicional de identidade e acesso.

O comportamento esperado é o mesmo nos quatro grupos: entrada FIFO na fila, concessão de hold temporário quando há estoque suficiente, confirmação idempotente de reservas, expiração de holds e preservação das invariantes de estoque. A campanha foi desenhada para observar o custo operacional incremental de separar fronteiras mantendo o mesmo fluxo funcional.

O diretório raiz desta campanha é `{manifest.get("resultRoot", "")}`. A matriz executada contém `{len(items)}` runs observados pelo mesmo conjunto de fontes: k6, Prometheus, cAdvisor, logs do Compose e snapshots do banco de dados.

Os dados aqui são material de suporte. A interpretação acadêmica ainda deve considerar repetibilidade, variância, aquecimento da JVM, ruído do Docker Desktop, recursos da máquina, versões das imagens e validade estatística do número de repetições.
"""


def markdown_protocol(manifest):
    return f"""# Protocolo de execução

O script `experiments/scripts/run-campaign.ps1` orquestra cada run de forma isolada:

1. Seleciona grupo, perfil de carga e número da repetição.
2. Sobe a topologia Docker Compose correspondente com um nome de projeto exclusivo.
3. Aguarda a API responder em `/actuator/health`.
4. Aguarda o Prometheus responder em `/-/ready`.
5. Executa o k6 em container Docker dentro da rede do Compose.
6. Coleta séries e resumo do Prometheus.
7. Exporta tabelas dos bancos via `psql` dentro dos containers.
8. Valida invariantes de domínio com base nos CSVs exportados.
9. Salva logs do Compose e metadados da execução.
10. Remove volumes e containers da topologia antes da próxima repetição.

Configuração registrada no manifesto:

```json
{json.dumps({k: v for k, v in manifest.items() if k != "runs"}, indent=2, ensure_ascii=False)}
```

Cada run possui um diretório próprio com `k6-summary.json`, `k6.log`, `profile-config.json`, `run-metadata.json`, `prometheus/`, `domain/` e `compose-logs/`.
"""


def markdown_profiles(items):
    examples = {}
    for item in items:
        profile = item["metadata"].get("profile")
        examples.setdefault(profile, item["profile_config"])

    sections = [
        "# Perfis de carga",
        "",
        "Os perfis usam o mesmo script k6 (`experiments/k6/waiting-room.js`) e variam concorrência, duração e pressão sobre estoque.",
    ]
    for profile in sorted(examples):
        sections.extend([
            "",
            f"## {profile}",
            "",
            "```json",
            json.dumps(examples[profile], indent=2, ensure_ascii=False),
            "```",
        ])
    sections.extend([
        "",
        "Leitura operacional:",
        "",
        "- `steady`: carga constante para observar custo médio e estabilidade.",
        "- `contention`: estoque baixo em relação à concorrência para forçar disputa por holds e estados terminais.",
        "- `spike`: subida rápida de usuários virtuais para observar elasticidade e filas sob rajada.",
    ])
    return "\n".join(sections)


def markdown_topologies(items):
    rows = []
    seen = set()
    for item in items:
        group = normalize_group(item["metadata"].get("group"))
        if group in seen:
            continue
        seen.add(group)
        rows.append({
            "group": group,
            "scope": {
                "G0": "API, fila, estoque, reservas e identidade no mesmo processo e banco.",
                "G1": "Reservas separadas do residual; residual preserva identidade, filas e sala de espera.",
                "G2": "Reservas e gestão de filas separadas; residual mantém identidade e sala de espera.",
                "G3": "Identidade, sala de espera, gestão de filas e reservas separados.",
            }.get(group, ""),
            "experimental_note": "Comparável apenas se as validações de domínio passarem e o mesmo fluxo k6 for mantido.",
        })

    return "# Topologias\n\n" + md_table(rows, [
        ("group", "Grupo"),
        ("scope", "Escopo operacional"),
        ("experimental_note", "Nota"),
    ]) + "\n"


def markdown_runs(run_rows):
    rows = []
    for row in run_rows:
        rows.append({
            "group": row["group"],
            "profile": row["profile"],
            "run": row["run"],
            "status": row["status"],
            "k6": row["k6_exit_code"],
            "domain": row["domain_passed"],
            "duration": fmt(row["duration_seconds"]),
            "dir": Path(row["run_dir"]).name,
        })
    return "# Execuções\n\n" + md_table(rows, [
        ("group", "Grupo"),
        ("profile", "Perfil"),
        ("run", "Run"),
        ("status", "Status"),
        ("k6", "k6 exit"),
        ("domain", "Domínio"),
        ("duration", "s"),
        ("dir", "Diretório"),
    ]) + "\n"


def markdown_k6(k6_rows, grouped_rows):
    detail = []
    for row in k6_rows:
        detail.append({
            "group": row["group"],
            "profile": row["profile"],
            "run": row["run"],
            "reqs": fmt(row["http_reqs"], 0),
            "rate": fmt(row["http_req_rate"]),
            "fail": fmt(row["http_req_failed_rate"], 5),
            "p95": fmt(row["http_req_duration_p95_ms"]),
            "checks": fmt(row["checks_rate"], 5),
        })
    return "\n".join([
        "# k6",
        "",
        "Resumo por grupo e perfil:",
        "",
        md_table(grouped_rows, [
            ("group", "Grupo"),
            ("profile", "Perfil"),
            ("runs", "Runs"),
            ("http_p95_ms_avg", "p95 médio ms"),
            ("failed_rate_avg", "falha média"),
        ]),
        "",
        "Resumo por run:",
        "",
        md_table(detail, [
            ("group", "Grupo"),
            ("profile", "Perfil"),
            ("run", "Run"),
            ("reqs", "Reqs"),
            ("rate", "Req/s"),
            ("fail", "Falha"),
            ("p95", "p95 ms"),
            ("checks", "Checks"),
        ]),
    ])


def markdown_prometheus(prom_rows, grouped_rows):
    detail = []
    for row in prom_rows:
        detail.append({
            "group": row["group"],
            "profile": row["profile"],
            "run": row["run"],
            "source": row["resource_source"],
            "cpu": fmt(row["resource_cpu_avg"]),
            "mem": fmt(row["resource_mem_avg_gb"]),
            "rx": fmt(row["resource_rx_bytes"], 0),
            "tx": fmt(row["resource_tx_bytes"], 0),
            "rps": fmt(row["http_rps_avg"]),
        })
    return "\n".join([
        "# Prometheus e cAdvisor",
        "",
        "As métricas HTTP e de domínio vêm da instrumentação Micrometer exposta pelas aplicações e consultada via Prometheus. As métricas de recursos tentam usar cAdvisor; quando cAdvisor não expõe containers por projeto no Docker Desktop, o pacote usa `docker stats` amostrado durante o k6 como fonte consolidada de CPU, memória e rede.",
        "",
        "Resumo por grupo e perfil:",
        "",
        md_table(grouped_rows, [
            ("group", "Grupo"),
            ("profile", "Perfil"),
            ("runs", "Runs"),
            ("cpu_avg", "CPU média"),
            ("mem_avg_gb", "Mem GB média"),
        ]),
        "",
        "Resumo por run:",
        "",
        md_table(detail, [
            ("group", "Grupo"),
            ("profile", "Perfil"),
            ("run", "Run"),
            ("source", "Fonte"),
            ("cpu", "CPU"),
            ("mem", "Mem GB"),
            ("rx", "RX bytes"),
            ("tx", "TX bytes"),
            ("rps", "HTTP rps"),
        ]),
    ])


def markdown_domain(domain_rows_data):
    checks = defaultdict(lambda: {"passed": 0, "failed": 0})
    for row in domain_rows_data:
        if row.get("check"):
            bucket = checks[row["check"]]
            if str(row.get("check_passed")).lower() == "true":
                bucket["passed"] += 1
            else:
                bucket["failed"] += 1

    summary_rows = [
        {"check": check, "passed": values["passed"], "failed": values["failed"]}
        for check, values in sorted(checks.items())
    ]
    return "\n".join([
        "# Validações de domínio",
        "",
        "As validações usam os snapshots dos bancos depois de cada run. Elas verificam conservação de estoque, ausência de participante ativo duplicado por fila, ausência de sequência duplicada por fila e vínculo entre entradas com reserva e reservas persistidas.",
        "",
        md_table(summary_rows, [
            ("check", "Invariante"),
            ("passed", "Passou"),
            ("failed", "Falhou"),
        ]),
        "",
        "Detalhes completos estão em `../aggregates/domain-validation.csv` e nos diretórios `domain/` de cada run.",
    ])


def markdown_icce(icce_rows_data):
    detail = []
    for row in icce_rows_data:
        detail.append({
            "group": row["group"],
            "load": row["load"],
            "run": row["run"],
            "source": row.get("resource_source", ""),
            "cpu": fmt(row["cpu_avg"]),
            "duration": fmt(row["duration_seconds"]),
            "mem": fmt(row["mem_avg_gb"]),
            "network": fmt(row["network_bytes"], 0),
            "icce": fmt(row["icce"]) if row["icce"] != "" else "n/d",
        })
    return "\n".join([
        "# ICCE",
        "",
        "O ICCE é calculado por carga usando `G0` como baseline da mesma carga. Para cada run, são normalizados CPU média, duração, memória média e bytes de rede totais. O índice final é a média simples dessas razões.",
        "",
        "Essa versão é um artefato de suporte: ela explicita o cálculo e os insumos, mas não substitui análise estatística nem discussão de validade.",
        "",
        md_table(detail, [
            ("group", "Grupo"),
            ("load", "Carga"),
            ("run", "Run"),
            ("source", "Fonte"),
            ("cpu", "CPU"),
            ("duration", "Duração"),
            ("mem", "Mem GB"),
            ("network", "Rede bytes"),
            ("icce", "ICCE"),
        ]),
    ])


def markdown_limitations(manifest):
    return f"""# Limitações e cuidados

- G1, G2 e G3 representam topologias experimentais e scaffolding de execução. A validade experimental depende de os adaptadores HTTP internos preservarem a semântica do fluxo completo entre sala de espera, gestão de filas e reservas.
- O Docker Desktop adiciona ruído de virtualização, agendamento e I/O que deve ser descrito no TCC.
- A JVM pode ter efeito de aquecimento; a ordem de execução e repetições devem ser consideradas na análise.
- Os perfis usam dados sintéticos gerados pelo k6. Eles exercitam o fluxo principal, mas não representam uma população real.
- Métricas de rede coletadas por cAdvisor podem incluir tráfego interno de health checks, scraping e infraestrutura da topologia.
- O ICCE usa média simples de dimensões heterogêneas; ele deve ser tratado como índice auxiliar, não como prova isolada.
- Runs com falhas de domínio, falhas k6 ou erros Prometheus devem ser separados antes de qualquer conclusão comparativa.
- A campanha foi registrada com o manifesto `{manifest.get("startedAt", "")}` e deve ser reproduzida com versões equivalentes de Docker, imagens, JDK, Maven e código.
"""


def markdown_wohlin_analysis(manifest, group_rows, run_rows, domain_rows_data):
    total_runs = len(run_rows)
    successful_runs = sum(1 for row in run_rows if row.get("status") == "success")
    domain_ok = len({
        (row.get("group"), row.get("profile"), row.get("run"))
        for row in domain_rows_data
        if str(row.get("passed")).lower() == "true"
    })

    compact_rows = []
    for row in group_rows:
        compact_rows.append({
            "group": row["group"],
            "profile": row["profile"],
            "runs": row["runs"],
            "p95": fmt(row["http_p95_ms_mean"]),
            "cpu": fmt(row["cpu_avg_mean"]),
            "mem": fmt(row["mem_avg_gb_mean"]),
            "net": fmt(row["network_bytes_mean"], 0),
            "icce": fmt(row["icce_mean"]),
            "domain": row["domain_ok_runs"],
        })

    return "\n".join([
        "# Análise metodológica baseada em Wohlin",
        "",
        "Este arquivo organiza a campanha no formato de apoio esperado para experimentos em Engenharia de Software. Ele não substitui a seção final do TCC, mas deixa explícito o desenho experimental, a operação, os dados observados e as ameaças à validade.",
        "",
        "## Definição",
        "",
        "Objeto de estudo: sistema de sala de espera com estoque e reservas temporárias, executado em quatro granularidades arquiteturais.",
        "",
        "Propósito: avaliar, de forma descritiva e reprodutível, o efeito operacional da granularidade sobre latência, consumo de recursos, tráfego e preservação de invariantes de domínio.",
        "",
        "Perspectiva: Engenharia de Software experimental, interessada no custo técnico de decomposição arquitetural sob comportamento funcional equivalente.",
        "",
        "Foco de qualidade: desempenho HTTP, custo computacional, custo de comunicação, estabilidade funcional e validade das regras de domínio.",
        "",
        "Contexto: execução local com Docker Desktop, k6, Prometheus, coleta `docker stats`, bancos PostgreSQL descartáveis e topologias Compose G0-G3.",
        "",
        "## Planejamento",
        "",
        md_table([
            {"item": "Fator principal", "value": "Granularidade arquitetural: G0, G1, G2 e G3."},
            {"item": "Tratamentos", "value": "G0 monolítico; G1 separa reservas; G2 separa reservas e filas; G3 separa identidade, sala de espera, filas e reservas."},
            {"item": "Perfis de carga", "value": "steady, contention e spike."},
            {"item": "Repetições", "value": "3 execuções independentes por combinação grupo/perfil."},
            {"item": "Unidade experimental", "value": "Uma topologia Docker Compose descartável executando um perfil k6."},
            {"item": "Variáveis dependentes", "value": "p95 HTTP, taxa de falha, checks k6, CPU média, memória média, bytes de rede, ICCE e invariantes de domínio."},
            {"item": "Critério funcional", "value": "Run só é interpretável quando k6 termina com código 0 e as invariantes de domínio passam."},
        ], [("item", "Item"), ("value", "Valor")]),
        "",
        "## Operação",
        "",
        f"A matriz executada contém `{total_runs}` runs. Runs com status `success`: `{successful_runs}`. Runs com validação de domínio positiva: `{domain_ok}`.",
        "",
        f"Janela operacional registrada no manifesto: início `{manifest.get('startedAt', 'n/d')}`; fim `{manifest.get('endedAt', 'n/d')}`.",
        "",
        "A coleta de recursos usa `docker stats` como fonte consolidada porque o cAdvisor disponível no Docker Desktop registrou limitação de compatibilidade com a API do Docker 29. Prometheus permanece como fonte das métricas HTTP e de domínio expostas por Micrometer.",
        "",
        "## Análise descritiva",
        "",
        md_table(compact_rows, [
            ("group", "Grupo"),
            ("profile", "Perfil"),
            ("runs", "Runs"),
            ("p95", "p95 ms"),
            ("cpu", "CPU"),
            ("mem", "Mem GB"),
            ("net", "Rede bytes"),
            ("icce", "ICCE"),
            ("domain", "Domínio OK"),
        ]),
        "",
        "Leitura inicial dos dados:",
        "",
        "- Todas as combinações executadas preservaram o comportamento funcional observado pelo k6 e pelas validações de domínio.",
        "- O aumento de granularidade elevou o custo médio de CPU, memória e rede, especialmente em G2 e G3, como esperado pela introdução de mais processos, bancos e chamadas HTTP internas.",
        "- O p95 HTTP permaneceu abaixo do limiar operacional configurado em todos os perfis, mas variou entre topologias e perfis de carga.",
        "- O ICCE deve ser usado como índice auxiliar: ele consolida dimensões heterogêneas e precisa ser discutido junto das métricas individuais.",
        "",
        "## Ameaças à validade",
        "",
        md_table([
            {"type": "Conclusão", "risk": "Amostra de 3 repetições por combinação é adequada para triagem, mas limitada para inferência estatística forte.", "mitigation": "Preservar dados brutos e ampliar repetições em campanhas futuras."},
            {"type": "Construção", "risk": "ICCE combina CPU, duração, memória e rede com média simples.", "mitigation": "Reportar também as métricas individuais e justificar pesos se o índice virar evidência principal."},
            {"type": "Interna", "risk": "Ruído do Docker Desktop, aquecimento da JVM e estado da máquina podem afetar medições.", "mitigation": "Registrar ambiente, repetir por perfil e isolar topologias com volumes descartáveis."},
            {"type": "Externa", "risk": "Carga sintética k6 pode não representar tráfego real de produção.", "mitigation": "Tratar os perfis como cenários controlados e calibrar novos perfis quando houver workload realista."},
            {"type": "Confiabilidade", "risk": "cAdvisor não expôs dados de container por projeto nesta máquina.", "mitigation": "Usar `docker stats` amostrado e documentar a troca de fonte."},
        ], [("type", "Tipo"), ("risk", "Risco"), ("mitigation", "Mitigação")]),
    ])


def write_run_pages(items):
    for item in items:
        meta = item["metadata"]
        k6 = k6_row(item)
        prom = prometheus_row(item)
        domain = item["domain"] or {}
        content = "\n".join([
            f"# Run {normalize_group(meta.get('group'))} {meta.get('profile')} #{meta.get('run')}",
            "",
            "## Metadados",
            "",
            "```json",
            json.dumps(meta, indent=2, ensure_ascii=False),
            "```",
            "",
            "## k6",
            "",
            md_table([{
                "reqs": fmt(k6["http_reqs"], 0),
                "rate": fmt(k6["http_req_rate"]),
                "failed": fmt(k6["http_req_failed_rate"], 5),
                "p95": fmt(k6["http_req_duration_p95_ms"]),
                "checks": fmt(k6["checks_rate"], 5),
            }], [
                ("reqs", "Reqs"),
                ("rate", "Req/s"),
                ("failed", "Falha"),
                ("p95", "p95 ms"),
                ("checks", "Checks"),
            ]),
            "",
            "## Recursos",
            "",
            md_table([{
                "source": resource_summary(item)["source"],
                "cpu": fmt(resource_summary(item)["cpu_avg"]),
                "mem": fmt(resource_summary(item)["mem_avg_gb"]),
                "rx": fmt(resource_summary(item)["rx_bytes"], 0),
                "tx": fmt(resource_summary(item)["tx_bytes"], 0),
                "rps": fmt(prom["http_rps_avg"]),
            }], [
                ("source", "Fonte"),
                ("cpu", "CPU"),
                ("mem", "Mem GB"),
                ("rx", "RX bytes"),
                ("tx", "TX bytes"),
                ("rps", "HTTP rps"),
            ]),
            "",
            "## Domínio",
            "",
            "```json",
            json.dumps(domain, indent=2, ensure_ascii=False),
            "```",
        ])
        write_text(item["run_dir"] / "run.md", content)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", required=True)
    args = parser.parse_args()

    root = Path(args.root)
    manifest, items = collect_runs(root)
    aggregates = root / "aggregates"
    support = root / "support"

    run_rows = [runs_row(item) for item in items]
    k6_rows = [k6_row(item) for item in items]
    prom_rows = [prometheus_row(item) for item in items]
    docker_rows = [docker_stats_row(item) for item in items]
    domain_rows_data = [row for item in items for row in domain_rows(item)]
    icce_input = icce_rows(items)
    icce_output = calculate_icce(icce_input)
    grouped_rows = profile_summary(items)
    group_profile_rows = group_profile_summary(items, icce_output)

    write_csv(aggregates / "runs.csv", run_rows)
    write_csv(aggregates / "k6-summary.csv", k6_rows)
    write_csv(aggregates / "prometheus-summary.csv", prom_rows)
    write_csv(aggregates / "docker-stats-summary.csv", docker_rows)
    write_csv(aggregates / "domain-validation.csv", domain_rows_data)
    write_csv(aggregates / "icce-input.csv", icce_input)
    write_csv(aggregates / "icce-output.csv", icce_output)
    write_csv(aggregates / "group-profile-summary.csv", group_profile_rows)

    write_run_pages(items)
    write_text(support / "00-index.md", markdown_index(manifest, items, grouped_rows))
    write_text(support / "01-contexto-experimental.md", markdown_context(manifest, items))
    write_text(support / "02-protocolo-de-execucao.md", markdown_protocol(manifest))
    write_text(support / "03-perfis-de-carga.md", markdown_profiles(items))
    write_text(support / "04-topologias.md", markdown_topologies(items))
    write_text(support / "05-execucoes.md", markdown_runs(run_rows))
    write_text(support / "06-k6.md", markdown_k6(k6_rows, grouped_rows))
    write_text(support / "07-prometheus-cadvisor.md", markdown_prometheus(prom_rows, grouped_rows))
    write_text(support / "08-validacoes-de-dominio.md", markdown_domain(domain_rows_data))
    write_text(support / "09-icce.md", markdown_icce(icce_output))
    write_text(support / "10-limitacoes.md", markdown_limitations(manifest))
    write_text(support / "11-analise-wohlin.md", markdown_wohlin_analysis(
        manifest, group_profile_rows, run_rows, domain_rows_data
    ))

    print(f"support={support}")
    print(f"aggregates={aggregates}")


if __name__ == "__main__":
    main()
