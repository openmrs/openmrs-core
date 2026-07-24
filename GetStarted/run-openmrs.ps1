<#
.SYNOPSIS
    Runs openmrs-core 2.8.x under the Maven Jetty plugin (cross-platform).

.DESCRIPTION
    A thin, friendly wrapper around the mvn jetty:run commands the team uses,
    so the same invocation works for any teammate. Handles the three things
    people otherwise forget:

      1. MAVEN_OPTS with the --add-opens flags openmrs-core needs on Java 21
         (legacy reflection), plus an optional remote-debug JDWP agent.
      2. OPENMRS_APPLICATION_DATA_DIRECTORY pointing at a repo-local folder so
         modules, the H2 db, and runtime properties live inside
         GetStarted\openmrs-data (not your user home).
      3. OPENMRS_INSTALLATION_SCRIPT for MySQL/PostgreSQL auto-install (the same
         mechanism the -Ph2 profile uses for H2). Templates ship under .\config.

    Default (-H2) is zero-config and uses an in-memory database. After it starts,
    open http://localhost:8080/openmrs  (admin / Admin123).

.PARAMETER Mode
    H2 (default) | MySQL | Postgres. H2 uses the built-in -Ph2 profile; the
    others run plain `mvn jetty:run` with OPENMRS_INSTALLATION_SCRIPT pointing at
    the matching template in .\config (edit it first with your DB credentials).

.PARAMETER Port
    HTTP port. Default 8080.

.PARAMETER PlWebapp
    Run only the webapp module: `mvn -pl webapp ... jetty:run` (faster startup;
    skips the rest of the reactor).

.PARAMETER Debug
    Add the JDWP remote-debug agent (suspend=n, address=*:8000) to MAVEN_OPTS.

.PARAMETER DataDir
    OpenMRS application data directory. Default: .\openmrs-data. Copy your built
    .omod files into <DataDir>\modules\ before (or at) first start.

.EXAMPLE
    .\run-openmrs.ps1
    .\run-openmrs.ps1 -Mode MySQL
    .\run-openmrs.ps1 -Mode Postgres -PlWebapp -Debug
#>
[CmdletBinding()]
param(
    [ValidateSet('H2','MySQL','Postgres')] [string]$Mode = 'H2',
    [int]$Port = 8080,
    [switch]$PlWebapp,
    [switch]$Debug,
    [string]$DataDir,
    [string]$CoreRepo
)

$ErrorActionPreference = 'Stop'
$ScriptDir = $PSScriptRoot
if (-not $CoreRepo) { $CoreRepo = (Get-Item (Join-Path $ScriptDir '..')).FullName }
if (-not $DataDir)  { $DataDir  = Join-Path $ScriptDir 'openmrs-data' }

# --- Pre-flight ------------------------------------------------------------- #
if (-not (Test-Path (Join-Path $CoreRepo 'pom.xml'))) {
    throw "openmrs-core pom.xml not found at '$CoreRepo'. Pass -CoreRepo."
}
$branch = git -C $CoreRepo rev-parse --abbrev-ref HEAD 2>$null
if ($branch -and $branch -notlike '2.8*') {
    Write-Host "NOTE: openmrs-core is on branch '$branch'. These scripts target the '2.8' branch (run: git -C `"$CoreRepo`" checkout 2.8)." -ForegroundColor Yellow
}
New-Item -ItemType Directory -Force -Path $DataDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $DataDir 'modules') | Out-Null

# --- MAVEN_OPTS (always: --add-opens for Java 21; optionally JDWP) --------- #
$opts = @(
    '--add-opens=java.base/java.lang=ALL-UNNAMED',
    '--add-opens=java.base/java.util=ALL-UNNAMED'
)
if ($Debug) {
    $opts = @('-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000') + $opts
}
$env:MAVEN_OPTS = ($opts -join ' ')

# --- Assemble the mvn invocation ------------------------------------------- #
$goals = @('jetty:run')
if ($PlWebapp) { $goals = @('-pl','webapp','jetty:run') }
$mavenArgs = @()

if ($Mode -eq 'H2') {
    $mavenArgs += '-Ph2'
}
else {
    $template = Join-Path $ScriptDir "config\openmrs-installation.$($Mode.ToLower()).properties"
    if (-not (Test-Path $template)) { throw "Install template not found: $template" }
    # file: URL to the template; OpenMRS reads OPENMRS_INSTALLATION_SCRIPT as a resource.
    $fileUrl = 'file:///' + ($template -replace '\\','/')
    $mavenArgs += "-DOPENMRS_INSTALLATION_SCRIPT=$fileUrl"
}
$mavenArgs += "-Djetty.http.port=$Port"
$mavenArgs += "-DOPENMRS_APPLICATION_DATA_DIRECTORY=$DataDir"
$mavenArgs += $goals

Write-Host "openmrs-core Jetty run" -ForegroundColor Green
Write-Host "  Core repo  : $CoreRepo"
Write-Host "  Branch     : $branch"
Write-Host "  Mode       : $Mode"
Write-Host "  Port       : $Port"
Write-Host "  PlWebapp   : $([bool]$PlWebapp)"
Write-Host "  Data dir   : $DataDir"
Write-Host "  Modules    : $(Join-Path $DataDir 'modules')  <- copy your .omod files here"
Write-Host "  MAVEN_OPTS : $($env:MAVEN_OPTS)"
Write-Host "  mvn        : mvn $($mavenArgs -join ' ')"
Write-Host ""
Write-Host "  Open http://localhost:$Port/openmrs  (admin / Admin123)" -ForegroundColor Cyan
Write-Host ""

Push-Location $CoreRepo
try {
    & mvn @mavenArgs
}
finally {
    Pop-Location
}
