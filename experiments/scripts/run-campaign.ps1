[CmdletBinding()]
param(
    [string[]] $Groups = @("g0", "g1", "g2", "g3"),
    [string[]] $Profiles = @("steady", "contention", "spike"),
    [int] $Runs = 3,
    [switch] $DryRun,
    [switch] $SkipBuild,
    [string] $DockerContext = "default",
    [string] $K6Image = "grafana/k6:0.54.0",
    [string] $OutputRoot = "",
    [int] $ApiPort = 18080,
    [int] $PrometheusPort = 19090,
    [int] $GrafanaPort = 13000,
    [int] $CAdvisorPort = 18081,
    [int] $PostgresPort = 15433,
    [string] $DurationOverride = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = (Resolve-Path (Join-Path $ScriptDir "..\..")).Path
$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
if ([string]::IsNullOrWhiteSpace($OutputRoot)) {
    $OutputRoot = Join-Path $RepoRoot "experiments\results"
}
$ResultRoot = Join-Path $OutputRoot "$Timestamp-g0-g3"

function ConvertTo-GroupName([string] $Group) {
    return $Group.ToUpperInvariant()
}

function Get-ComposeFile([string] $Group) {
    return Join-Path $RepoRoot "experiments\compose\$($Group.ToLowerInvariant()).yml"
}

function Write-JsonFile($Path, $Value) {
    $Parent = Split-Path -Parent $Path
    if (-not (Test-Path $Parent)) {
        New-Item -ItemType Directory -Path $Parent | Out-Null
    }
    $Value | ConvertTo-Json -Depth 30 | Set-Content -Path $Path -Encoding utf8
}

function Invoke-Native {
    param(
        [Parameter(Mandatory = $true)] [string] $File,
        [Parameter(Mandatory = $true)] [string[]] $Arguments,
        [string] $LogPath = "",
        [switch] $AllowFailure,
        [string] $WorkingDirectory = $RepoRoot
    )

    Push-Location $WorkingDirectory
    try {
        if ([string]::IsNullOrWhiteSpace($LogPath)) {
            & $File @Arguments
        } else {
            & $File @Arguments *> $LogPath
        }
        $Code = $LASTEXITCODE
        if ($Code -ne 0 -and -not $AllowFailure) {
            throw "$File $($Arguments -join ' ') exited with code $Code"
        }
        return $Code
    } finally {
        Pop-Location
    }
}

function Invoke-Docker {
    param(
        [Parameter(Mandatory = $true)] [string[]] $Arguments,
        [string] $LogPath = "",
        [switch] $AllowFailure
    )
    return Invoke-Native -File "docker" -Arguments (@("--context", $DockerContext) + $Arguments) -LogPath $LogPath -AllowFailure:$AllowFailure
}

function Wait-HttpJson {
    param(
        [Parameter(Mandatory = $true)] [string] $Url,
        [int] $Seconds = 180
    )
    $Deadline = (Get-Date).AddSeconds($Seconds)
    $LastError = $null
    while ((Get-Date) -lt $Deadline) {
        try {
            return Invoke-RestMethod -Uri $Url -TimeoutSec 3
        } catch {
            $LastError = $_.Exception.Message
            Start-Sleep -Seconds 2
        }
    }
    throw "Timeout waiting for $Url. Last error: $LastError"
}

function Invoke-TextCommand {
    param([string] $Command)

    try {
        return ((& cmd /c $Command) -join "`n").Trim()
    } catch {
        return $_.Exception.Message
    }
}

function Get-ProfileConfig {
    param([string] $Profile)

    $Common = @{
        PROFILE = $Profile
        MANAGER_USERNAME = "admin"
        MANAGER_PASSWORD = "admin123"
        MAX_QUANTITY = "3"
        POLL_SLEEP_SECONDS = "1"
        HTTP_FAILED_RATE = "0.20"
        HTTP_P95_MS = "5000"
    }

    if ($Profile -eq "steady") {
        $Config = @{
            VUS = "10"
            DURATION = "3m"
            RESOURCE_TOTAL = "5000"
            HOLD_SECONDS = "30"
            BATCH_SIZE = "50"
            POLL_ATTEMPTS = "20"
        }
    } elseif ($Profile -eq "contention") {
        $Config = @{
            VUS = "40"
            DURATION = "3m"
            RESOURCE_TOTAL = "120"
            HOLD_SECONDS = "45"
            BATCH_SIZE = "20"
            POLL_ATTEMPTS = "30"
        }
    } elseif ($Profile -eq "spike") {
        $Config = @{
            SPIKE_VUS = "80"
            STAGE_RAMP_UP = "30s"
            STAGE_HOLD = "2m"
            STAGE_RAMP_DOWN = "30s"
            RESOURCE_TOTAL = "300"
            HOLD_SECONDS = "45"
            BATCH_SIZE = "30"
            POLL_ATTEMPTS = "30"
        }
    } else {
        throw "Unknown profile: $Profile"
    }

    foreach ($Key in $Common.Keys) {
        $Config[$Key] = $Common[$Key]
    }
    if (-not [string]::IsNullOrWhiteSpace($DurationOverride)) {
        if ($Profile -eq "spike") {
            $Config["STAGE_HOLD"] = $DurationOverride
        } else {
            $Config["DURATION"] = $DurationOverride
        }
    }
    return $Config
}

function Get-EnvironmentSnapshot {
    $GitCommitText = Invoke-TextCommand "git rev-parse --short HEAD 2>&1"
    $GitStatusText = Invoke-TextCommand "git status --short 2>&1"
    $JavaVersion = Invoke-TextCommand "java -version 2>&1"
    $PythonVersion = Invoke-TextCommand "python --version 2>&1"
    $DockerVersion = Invoke-TextCommand "docker --context $DockerContext version 2>&1"

    return [ordered]@{
        gitCommit = $GitCommitText
        gitStatusShort = $GitStatusText
        javaVersion = $JavaVersion
        pythonVersion = $PythonVersion
        dockerContext = $DockerContext
        dockerVersion = $DockerVersion
        k6Image = $K6Image
    }
}

function Save-ComposeLogs {
    param(
        [string] $Project,
        [string] $ComposeFile,
        [string] $RunDir
    )
    $LogsPath = Join-Path $RunDir "compose.log"
    Invoke-Docker -Arguments @("compose", "-p", $Project, "-f", $ComposeFile, "logs", "--no-color", "--tail=1500") -LogPath $LogsPath -AllowFailure | Out-Null
}

function Start-DockerStatsCollector {
    param(
        [string] $Project,
        [string] $RunDir
    )

    $DockerStatsDir = Join-Path $RunDir "docker-stats"
    New-Item -ItemType Directory -Path $DockerStatsDir -Force | Out-Null
    $StopFile = Join-Path $DockerStatsDir "stop"
    if (Test-Path $StopFile) {
        Remove-Item -LiteralPath $StopFile -Force
    }

    $Args = @(
        (Join-Path $ScriptDir "collect-docker-stats.py"),
        "--docker-context", $DockerContext,
        "--project", $Project,
        "--output", $DockerStatsDir,
        "--stop-file", $StopFile,
        "--interval-seconds", "2"
    )

    $Process = Start-Process -FilePath "python" `
        -ArgumentList $Args `
        -RedirectStandardOutput (Join-Path $DockerStatsDir "collector.out.log") `
        -RedirectStandardError (Join-Path $DockerStatsDir "collector.err.log") `
        -PassThru `
        -WindowStyle Hidden

    return [ordered]@{
        process = $Process
        stopFile = $StopFile
        outputDir = $DockerStatsDir
    }
}

function Stop-DockerStatsCollector {
    param($Collector)

    if ($null -eq $Collector) {
        return
    }
    if (-not (Test-Path $Collector.stopFile)) {
        "stop" | Set-Content -Path $Collector.stopFile -Encoding ascii
    }
    if ($null -ne $Collector.process) {
        $Exited = $Collector.process.WaitForExit(15000)
        if (-not $Exited) {
            $Collector.process.Kill()
            $Collector.process.WaitForExit()
        }
    }
}

$Matrix = foreach ($Group in $Groups) {
    foreach ($Profile in $Profiles) {
        for ($Run = 1; $Run -le $Runs; $Run++) {
            [ordered]@{
                group = (ConvertTo-GroupName $Group)
                profile = $Profile
                run = $Run
                compose = (Get-ComposeFile $Group)
                profileConfig = (Get-ProfileConfig $Profile)
            }
        }
    }
}

if ($DryRun) {
    $Matrix | ConvertTo-Json -Depth 10
    exit 0
}

New-Item -ItemType Directory -Path $ResultRoot -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $ResultRoot "aggregates") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $ResultRoot "support") -Force | Out-Null

$env:API_PORT = [string] $ApiPort
$env:PROMETHEUS_PORT = [string] $PrometheusPort
$env:GRAFANA_PORT = [string] $GrafanaPort
$env:CADVISOR_PORT = [string] $CAdvisorPort
$env:POSTGRES_PORT = [string] $PostgresPort
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"

$Manifest = [ordered]@{
    startedAt = (Get-Date).ToUniversalTime().ToString("o")
    resultRoot = $ResultRoot
    groups = $Groups
    profiles = $Profiles
    runsPerProfile = $Runs
    ports = [ordered]@{
        api = $ApiPort
        prometheus = $PrometheusPort
        grafana = $GrafanaPort
        cadvisor = $CAdvisorPort
        postgres = $PostgresPort
    }
    environment = Get-EnvironmentSnapshot
    runs = @()
}
Write-JsonFile (Join-Path $ResultRoot "manifest.json") $Manifest

if (-not $SkipBuild) {
    Invoke-Native -File ".\mvnw.cmd" -Arguments @("package", "-DskipTests") -WorkingDirectory (Join-Path $RepoRoot "api-monolito")
}

foreach ($Group in $Groups) {
    $ComposeFile = Get-ComposeFile $Group
    Invoke-Docker -Arguments @("compose", "-f", $ComposeFile, "config", "--quiet") | Out-Null
}

$K6Script = (Resolve-Path (Join-Path $RepoRoot "experiments\k6\waiting-room.js")).Path

foreach ($Item in $Matrix) {
    $Group = [string] $Item.group
    $GroupLower = $Group.ToLowerInvariant()
    $Profile = [string] $Item.profile
    $RunNumber = [int] $Item.run
    $RunLabel = "run-{0:D2}" -f $RunNumber
    $Project = "tcc-bes-$GroupLower-$Profile-run$RunNumber"
    $ComposeFile = [string] $Item.compose
    $RunDir = Join-Path $ResultRoot "runs\$Group\$Profile\$RunLabel"
    New-Item -ItemType Directory -Path $RunDir -Force | Out-Null
    $DockerStatsCollector = $null

    $ProfileConfig = Get-ProfileConfig $Profile
    Write-JsonFile (Join-Path $RunDir "profile-config.json") $ProfileConfig

    $RunRecord = [ordered]@{
        group = $Group
        profile = $Profile
        run = $RunNumber
        project = $Project
        composeFile = $ComposeFile
        runDir = $RunDir
        status = "started"
        startedAt = (Get-Date).ToUniversalTime().ToString("o")
        endedAt = $null
        k6ExitCode = $null
        error = $null
    }

    try {
        Invoke-Docker -Arguments @("compose", "-p", $Project, "-f", $ComposeFile, "up", "--build", "-d", "--quiet-pull") | Out-Null
        $Health = Wait-HttpJson -Url "http://localhost:$ApiPort/actuator/health" -Seconds 300
        if ($Health.status -ne "UP") {
            throw "API health is $($Health.status)"
        }
        Wait-HttpJson -Url "http://localhost:$PrometheusPort/-/ready" -Seconds 120 | Out-Null
        Start-Sleep -Seconds 10

        $K6Args = @(
            "run", "--rm",
            "--network", "${Project}_default",
            "-v", "${K6Script}:/scripts/waiting-room.js:ro",
            "-v", "${RunDir}:/out",
            "-e", "BASE_URL=http://api:8080"
        )
        foreach ($Key in $ProfileConfig.Keys) {
            $K6Args += @("-e", "$Key=$($ProfileConfig[$Key])")
        }
        $K6Args += @(
            $K6Image,
            "run",
            "--summary-export", "/out/k6-summary.json",
            "/scripts/waiting-room.js"
        )

        $DockerStatsCollector = Start-DockerStatsCollector -Project $Project -RunDir $RunDir
        Start-Sleep -Seconds 2
        $LoadStartedAt = (Get-Date).ToUniversalTime()
        $K6Log = Join-Path $RunDir "k6.log"
        $K6Exit = Invoke-Docker -Arguments $K6Args -LogPath $K6Log -AllowFailure
        $LoadEndedAt = (Get-Date).ToUniversalTime()
        Stop-DockerStatsCollector -Collector $DockerStatsCollector
        $DockerStatsCollector = $null

        $RunRecord.k6ExitCode = $K6Exit
        $RunRecord.loadStartedAt = $LoadStartedAt.ToString("o")
        $RunRecord.loadEndedAt = $LoadEndedAt.ToString("o")
        $RunRecord.dockerStatsDir = (Join-Path $RunDir "docker-stats")

        $PrometheusDir = Join-Path $RunDir "prometheus"
        $PrometheusArgs = @(
            (Join-Path $ScriptDir "collect-prometheus.py"),
            "--base-url", "http://localhost:$PrometheusPort",
            "--output", $PrometheusDir,
            "--group", $Group,
            "--profile", $Profile,
            "--run", [string] $RunNumber,
            "--project", $Project,
            "--start", $LoadStartedAt.ToString("o"),
            "--end", $LoadEndedAt.ToString("o"),
            "--step", "15"
        )
        Invoke-Native -File "python" -Arguments $PrometheusArgs -AllowFailure | Out-Null

        $DomainDir = Join-Path $RunDir "domain"
        $DomainArgs = @(
            (Join-Path $ScriptDir "collect-domain-state.py"),
            "--docker-context", $DockerContext,
            "--project", $Project,
            "--group", $Group,
            "--output", $DomainDir
        )
        Invoke-Native -File "python" -Arguments $DomainArgs -AllowFailure | Out-Null

        if ($K6Exit -eq 0) {
            $RunRecord.status = "success"
        } else {
            $RunRecord.status = "k6_failed"
        }
    } catch {
        $RunRecord.status = "failed"
        $RunRecord.error = $_.Exception.Message
    } finally {
        Stop-DockerStatsCollector -Collector $DockerStatsCollector
        Save-ComposeLogs -Project $Project -ComposeFile $ComposeFile -RunDir $RunDir
        Invoke-Docker -Arguments @("compose", "-p", $Project, "-f", $ComposeFile, "down", "-v") -AllowFailure | Out-Null
        $RunRecord.endedAt = (Get-Date).ToUniversalTime().ToString("o")
        Write-JsonFile (Join-Path $RunDir "run-metadata.json") $RunRecord
        $Manifest.runs += $RunRecord
        Write-JsonFile (Join-Path $ResultRoot "manifest.json") $Manifest
    }
}

$Manifest.endedAt = (Get-Date).ToUniversalTime().ToString("o")
Write-JsonFile (Join-Path $ResultRoot "manifest.json") $Manifest

Invoke-Native -File "python" -Arguments @(
    (Join-Path $ScriptDir "render-support-md.py"),
    "--root", $ResultRoot
) | Out-Null

Write-Output "CAMPAIGN_RESULT_ROOT=$ResultRoot"
