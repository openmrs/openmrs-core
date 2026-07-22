<#
.SYNOPSIS
    Re-runs the modules that failed under build-modules.ps1 and, if they still
    fail, captures the failure in maximum detail.

.DESCRIPTION
    Flow:
      1. Decide which modules to retry:
           - if -Modules is given, use that list verbatim; otherwise
           - scan every <module>.log under .\.build-logs and treat the module as
             "failed last run" if its log's final BUILD marker is BUILD FAILURE
             (or has no marker / no log at all).
      2. For each candidate, look up its build profile (JDK 21 / JDK 8 /
         -DskipTests) from modules.csv, so the retry runs under identical
         conditions to build-modules.ps1.
      3. Run `mvn -B -ntp clean package [-DskipTests]`, streaming live to console
         and to the per-module log (overwrites the prior log).
      4. If the build still fails, write a structured <module>.failure.md digest
         (reactor summary, every [ERROR] line, failed goals, the exception /
         "Caused by:" chain, the [Help 1] line, and the last 40 log lines) plus
         update an aggregate retry-report.md.

    The raw mvn log remains at .build-logs\<module>.log; the human digest is
    .build-logs\<module>.failure.md; the roll-up is .build-logs\retry-report.md.

    JDK resolution mirrors build-modules.ps1 (-Java21/-Java8, JAVA21_HOME/
    JAVA8_HOME, JAVA_HOME).

.PARAMETER Modules
    Optional explicit list of repo folder names to retry (e.g.
    'openmrs-module-webservices.rest','openmrs-module-legacyui'). If omitted,
    failures are auto-detected from the existing per-module logs.

.PARAMETER NoRebuild
    Skip the mvn re-run and just (re)build the .failure.md digests from whatever
    the current logs say. Useful to re-parse after manually tweaking a module.

.EXAMPLE
    .\retry-failed-builds.ps1
    .\retry-failed-builds.ps1 -Modules openmrs-module-legacyui
    .\retry-failed-builds.ps1 -NoRebuild
#>
[CmdletBinding()]
param(
    [string[]]$Modules,
    [switch]$NoRebuild,
    [string]$Java21,
    [string]$Java8,
    [string]$Destination,
    [string]$ModulesCsv
)

$ErrorActionPreference = 'Stop'

$ScriptDir = $PSScriptRoot
if (-not $ModulesCsv)  { $ModulesCsv  = Join-Path $ScriptDir 'modules.csv' }
if (-not $Destination) { $Destination = (Get-Item (Join-Path $ScriptDir '..\..\..')).FullName }
$OmodDir = Join-Path $ScriptDir 'omod'
$LogDir  = Join-Path $ScriptDir '.build-logs'

# --------------------------------------------------------------------------- #
# Helpers (Resolve-RepoPath / Set-Java / Resolve-Jdk mirror build-modules.ps1)
# --------------------------------------------------------------------------- #
function Resolve-RepoPath {
    param([string]$Category, [string]$Repo)
    $p = Join-Path $Destination (Join-Path $Category $Repo)
    if (-not (Test-Path (Join-Path $p '.git'))) {
        throw "Repo '$Repo' not found at '$p'. Run clone-repos.ps1 first."
    }
    return $p
}
function Resolve-Jdk {
    param([string]$Explicit, [string]$EnvVar, [int]$Major)
    $h = $Explicit
    if (-not $h) { $h = [Environment]::GetEnvironmentVariable($EnvVar) }
    if (-not $h) { $h = $env:JAVA_HOME }
    if (-not $h) { throw "No JDK configured for Java $Major (param/env/JAVA_HOME)." }
    if (-not (Test-Path (Join-Path $h 'bin' 'java*'))) { throw "JDK not found at '$h'." }
    $ver = & (Join-Path $h 'bin' 'java') -version 2>&1 | Select-Object -First 1
    $needle = if ($Major -eq 8) { '1.8.' } else { "version `"$Major" }
    if ($ver -notlike "*$needle*") { throw "JDK at '$h' is not Java $Major ($ver)." }
    return $h
}
function Set-Java { param([string]$JavaHome); $env:JAVA_HOME = $JavaHome; $env:Path = "$env:JAVA_HOME\bin;$env:Path" }

# Build profile from modules.csv. Returns @{ Category=...; JavaHome=...; SkipTests=$bool }.
function Get-BuildProfile {
    param([string]$Label)
    $row = $script:Rows | Where-Object { $_.repo -eq $Label } | Select-Object -First 1
    if (-not $row) { throw "No entry for '$Label' in modules.csv." }
    switch ($row.group) {
        'java8'            { return @{ Category = $row.category; JavaHome = $script:Jdk8Home;  SkipTests = $false } }
        'java21-skipTests' { return @{ Category = $row.category; JavaHome = $script:Jdk21Home; SkipTests = $true  } }
        default            { return @{ Category = $row.category; JavaHome = $script:Jdk21Home; SkipTests = $false } }
    }
}

# Decide whether the LAST build of a module failed, by reading its log's final
# BUILD marker (walking from EOF; the last marker wins).
function Test-LastBuildFailed {
    param([string]$Label)
    $log = Join-Path $LogDir "$Label.log"
    if (-not (Test-Path $log)) { return $true }
    $lines = Get-Content -Path $log -ErrorAction SilentlyContinue
    if (-not $lines) { return $true }
    for ($i = $lines.Count - 1; $i -ge 0; $i--) {
        if ($lines[$i] -match 'BUILD SUCCESS') { return $false }
        if ($lines[$i] -match 'BUILD FAILURE') { return $true  }
    }
    return $true
}

function Invoke-ModuleBuild {
    param([string]$Repo, [string]$Label, [string]$JavaHome, [switch]$SkipTests)
    Set-Java $JavaHome
    $mvnArgs = @('-B', '-ntp', 'clean', 'package')
    if ($SkipTests) { $mvnArgs += '-DskipTests' }

    Write-Host ""
    Write-Host "================================================================" -ForegroundColor DarkGray
    Write-Host "=== Retrying: $Label" -ForegroundColor Cyan
    Write-Host "    JDK : $($env:JAVA_HOME)"
    Write-Host "    mvn $($mvnArgs -join ' ')"
    Write-Host "================================================================" -ForegroundColor DarkGray

    Push-Location $Repo
    try {
        $logFile = Join-Path $LogDir "$Label.log"
        $prevEAP = $ErrorActionPreference; $ErrorActionPreference = 'SilentlyContinue'
        try { & mvn @mvnArgs | Tee-Object -FilePath $logFile | Out-Host }
        finally { $ErrorActionPreference = $prevEAP }
        $exitCode = $LASTEXITCODE
    }
    finally { Pop-Location }

    return [pscustomobject]@{
        Label = $Label; Repo = $Repo; Success = ($exitCode -eq 0)
        ExitCode = $exitCode; LogFile = (Join-Path $LogDir "$Label.log"); SkipTests = [bool]$SkipTests
    }
}

# Pull the useful signal out of a failed mvn log so you can see what broke
# without scrolling 1.9 MB of text.
function Extract-FailureDetail {
    param([string]$LogFile, [string]$Label)
    if (-not (Test-Path $LogFile)) { return "# $Label -- log missing`n`nNo log at $LogFile." }
    $lines = Get-Content -Path $LogFile -ErrorAction SilentlyContinue
    if (-not $lines) { return "# $Label -- log empty`n`n$LogFile produced no output." }

    $sb = [System.Text.StringBuilder]::new()
    [void]$sb.AppendLine("# Build failure: $Label"); [void]$sb.AppendLine("")
    [void]$sb.AppendLine("- log: ``$LogFile``"); [void]$sb.AppendLine("- lines: $($lines.Count)"); [void]$sb.AppendLine("")

    $reactorStart = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^\[INFO\] Reactor Summary') { $reactorStart = $i; break }
    }
    if ($reactorStart -ge 0) {
        [void]$sb.AppendLine("## Reactor summary")
        $j = $reactorStart
        while ($j -lt $lines.Count) {
            $l = $lines[$j]
            if ($j -gt $reactorStart -and $l -notmatch '^\[INFO\]') { break }
            [void]$sb.AppendLine($l); $j++
            if ($j - $reactorStart -gt 80) { [void]$sb.AppendLine("[... truncated ...]"); break }
        }
        [void]$sb.AppendLine("")
    }

    $errs = $lines | Where-Object { $_ -match '^\[ERROR\]' }
    if ($errs) {
        [void]$sb.AppendLine("## All [ERROR] lines ($($errs.Count))")
        foreach ($e in $errs) { [void]$sb.AppendLine('```'); [void]$sb.AppendLine($e); [void]$sb.AppendLine('```') }
        [void]$sb.AppendLine("")
    }
    $goals = $lines | Where-Object { $_ -match 'Failed to execute goal' }
    if ($goals) { [void]$sb.AppendLine("## Failed execution goals"); foreach ($g in $goals) { [void]$sb.AppendLine("- $g") }; [void]$sb.AppendLine("") }
    $causes = $lines | Where-Object { $_ -match 'Caused by:|^\s*at .+\(.+:\d+\)|^\[ERROR\] .*Exception|^\[ERROR\] .*Error\b' } | Select-Object -Unique
    if ($causes) { [void]$sb.AppendLine("## Exception / stack-trace chain"); foreach ($c in $causes) { [void]$sb.AppendLine($c) }; [void]$sb.AppendLine("") }
    $help = $lines | Where-Object { $_ -match '\[Help 1\]|-> \[Help' } | Select-Object -Last 3
    if ($help) { [void]$sb.AppendLine("## Maven help line"); foreach ($h in $help) { [void]$sb.AppendLine($h) }; [void]$sb.AppendLine("") }
    [void]$sb.AppendLine("## Last 40 log lines"); [void]$sb.AppendLine('```')
    foreach ($t in ($lines | Select-Object -Last 40)) { [void]$sb.AppendLine($t) }
    [void]$sb.AppendLine('```')
    return $sb.ToString()
}

# --------------------------------------------------------------------------- #
# Pre-flight
# --------------------------------------------------------------------------- #
if (-not (Test-Path $ModulesCsv)) { throw "modules.csv not found at '$ModulesCsv'." }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw "mvn not found on PATH." }
New-Item -ItemType Directory -Force -Path $OmodDir, $LogDir | Out-Null

$script:Rows     = @(Import-Csv -Path $ModulesCsv)
$script:Jdk21Home = Resolve-Jdk -Explicit $Java21 -EnvVar 'JAVA21_HOME' -Major 21
$script:Jdk8Home  = Resolve-Jdk -Explicit $Java8  -EnvVar 'JAVA8_HOME'  -Major 8

# --------------------------------------------------------------------------- #
# Decide what to retry
# --------------------------------------------------------------------------- #
if ($Modules -and $Modules.Count -gt 0) {
    $candidates = $Modules
    Write-Host "Retry list supplied on command line." -ForegroundColor Cyan
}
else {
    Write-Host "Auto-detecting failed modules from $LogDir ..." -ForegroundColor Cyan
    $known = Get-ChildItem -Path $LogDir -Filter '*.log' -File |
        ForEach-Object { [System.IO.Path]::GetFileNameWithoutExtension($_.Name) }
    $candidates = @($known | Where-Object { Test-LastBuildFailed -Label $_ })
}

if ($candidates.Count -eq 0) {
    Write-Host "Nothing to retry -- no failed modules detected." -ForegroundColor Green
    return
}

Write-Host "Modules to retry ($($candidates.Count)):"
foreach ($c in $candidates) { Write-Host "  - $c" }
Write-Host ""

# --------------------------------------------------------------------------- #
# Retry loop
# --------------------------------------------------------------------------- #
$results = @(); $stillFailing = @(); $batchStart = Get-Date

foreach ($label in $candidates) {
    $profile = Get-BuildProfile -Label $label
    $repo = Resolve-RepoPath -Category $profile.Category -Repo $label

    if ($NoRebuild) {
        $r = [pscustomobject]@{
            Label = $label; Repo = $repo; Success = (-not (Test-LastBuildFailed -Label $label))
            ExitCode = $null; LogFile = (Join-Path $LogDir "$label.log"); SkipTests = $profile.SkipTests
        }
    }
    else {
        $r = Invoke-ModuleBuild -Repo $repo -Label $label -JavaHome $profile.JavaHome -SkipTests:$profile.SkipTests
    }
    $results += $r

    if (-not $r.Success) {
        $stillFailing += $r
        $digest = Extract-FailureDetail -LogFile $r.LogFile -Label $label
        $digestPath = Join-Path $LogDir "$label.failure.md"
        Set-Content -Path $digestPath -Value $digest -Encoding utf8
        Write-Host "  detailed failure digest -> $digestPath" -ForegroundColor Yellow
    }
}

$elapsed = (Get-Date) - $batchStart
Write-Host ""
Write-Host "==================== RETRY SUMMARY ====================" -ForegroundColor Red
Write-Host ("Retried       : {0}" -f $results.Count)
Write-Host ("Now succeed   : {0}" -f ($results | Where-Object { $_.Success }).Count)
Write-Host ("Still failing : {0}" -f $stillFailing.Count)
Write-Host ("Elapsed       : {0:hh\:mm\:ss}" -f $elapsed)
Write-Host "--------------------------------------------------------"
foreach ($r in $results) {
    if ($r.Success) {
        Write-Host ("  [{0,-10}] {1}" -f 'RECOVERED', $r.Label) -ForegroundColor Green
    }
    else {
        Write-Host ("  [{0,-10}] {1}" -f 'STILL BAD', $r.Label) -ForegroundColor Red
        Write-Host ("              log    : $($r.LogFile)")
        Write-Host ("              digest : $(Join-Path $LogDir ($r.Label + '.failure.md'))")
    }
}

# Aggregate roll-up of every still-failing module -> retry-report.md
if ($stillFailing.Count -gt 0) {
    $report = [System.Text.StringBuilder]::new()
    [void]$report.AppendLine("# OpenMRS backend retry report")
    [void]$report.AppendLine("")
    [void]$report.AppendLine("- generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
    [void]$report.AppendLine("- retried: $($results.Count)  recovered: $(($results | Where-Object {$_.Success}).Count)  still failing: $($stillFailing.Count)")
    [void]$report.AppendLine("")
    foreach ($f in $stillFailing) {
        [void]$report.AppendLine("---"); [void]$report.AppendLine("")
        [void]$report.AppendLine((Extract-FailureDetail -LogFile $f.LogFile -Label $f.Label))
        [void]$report.AppendLine("")
    }
    $reportPath = Join-Path $LogDir 'retry-report.md'
    Set-Content -Path $reportPath -Value $report.ToString() -Encoding utf8
    Write-Host ""
    Write-Host "Aggregate report -> $reportPath" -ForegroundColor Yellow
}
Write-Host "========================================================" -ForegroundColor Red
