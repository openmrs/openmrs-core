<#
.SYNOPSIS
    Builds every OpenMRS backend module for the 2.8.x branch and collects the
    produced .omod artifacts into .\omod\.

.DESCRIPTION
    Cross-platform-friendly port of the lead's build-backend.ps1, with the
    Windows-hardcoded JDK paths replaced by env-var/param resolution.

    Reads ./modules.csv. The `group` column drives the build profile:
      - java21            -> mvn -B -ntp clean package          (JDK 21)
      - java8             -> mvn -B -ntp clean package          (JDK 8)
      - java21-skipTests  -> mvn -B -ntp clean package -DskipTests (JDK 21)

    The matrix (24 modules, chartsearchai & azureblob-storage excluded):
      19 Java 21 plain | 3 Java 8 plain | 2 Java 21 -DskipTests.

    For each successful build the freshly produced .omod (from <repo>\omod\target)
    is copied to the .\omod\ folder next to this script. openmrs-core is NOT
    built here (it produces openmrs.war, not a module .omod) - see the commented
    block at the bottom.

    JDK resolution (in priority order):
      -Java21 / -Java8 parameter, else $env:JAVA21_HOME / $env:JAVA8_HOME,
      else $env:JAVA_HOME (used for both if the specific vars are absent).

    Per-module build logs are written to .\.build-logs\<module>.log. The console
    streams the same output live.

.PARAMETER Java21
    Path to a JDK 21 home. Defaults to $env:JAVA21_HOME, then $env:JAVA_HOME.

.PARAMETER Java8
    Path to a JDK 8 home. Defaults to $env:JAVA8_HOME, then $env:JAVA_HOME.

.PARAMETER Destination
    Workspace root holding Backend\<repo> and Dependencies\<repo>. Defaults to
    three levels above this script.

.PARAMETER Only
    Optional substring filter on repo name (e.g. -Only idgen).

.EXAMPLE
    .\build-modules.ps1
    .\build-modules.ps1 -Java21 'C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot' -Java8 'C:\Program Files\Eclipse Adoptium\jdk-8.0.492.9-hotspot'
    .\build-modules.ps1 -Only openmrs-module-idgen
#>
[CmdletBinding()]
param(
    [string]$Java21,
    [string]$Java8,
    [string]$Destination,
    [string]$ModulesCsv,
    [string]$Only
)

$ErrorActionPreference = 'Stop'

$ScriptDir = $PSScriptRoot
if (-not $ModulesCsv)  { $ModulesCsv  = Join-Path $ScriptDir 'modules.csv' }
if (-not $Destination) { $Destination = (Get-Item (Join-Path $ScriptDir '..\..\..')).FullName }
$OmodDir = Join-Path $ScriptDir 'omod'
$LogDir  = Join-Path $ScriptDir '.build-logs'

# --------------------------------------------------------------------------- #
# Helpers
# --------------------------------------------------------------------------- #

# Maps a (category, repo) to its checkout path under the workspace root.
function Resolve-RepoPath {
    param([string]$Category, [string]$Repo)
    $p = Join-Path $Destination (Join-Path $Category $Repo)
    if (-not (Test-Path (Join-Path $p '.git'))) {
        throw "Repo '$Repo' not found at '$p'. Run clone-repos.ps1 first (or pass -Destination)."
    }
    return $p
}

# Resolves a JDK home from param / env, and verifies its java -version matches
# the expected major version (21 or 1.8).
function Resolve-Jdk {
    param(
        [string]$Explicit,
        [string]$EnvVar,
        [int]$Major   # 21 or 8 (we look for "21" or "1.8." in the version string)
    )
    $home_dir = $Explicit
    if (-not $home_dir) { $home_dir = [Environment]::GetEnvironmentVariable($EnvVar) }
    if (-not $home_dir) { $home_dir = $env:JAVA_HOME }
    if (-not $home_dir) {
        throw "No JDK configured for Java $Major. Pass -Java$($Major)1 / -Java8, or set $($EnvVar) (or JAVA_HOME)."
    }
    if (-not (Test-Path (Join-Path $home_dir 'bin' 'java*'))) {
        throw "JDK not found at '$home_dir'."
    }
    # Verify the major version.
    $ver = & (Join-Path $home_dir 'bin' 'java') -version 2>&1 | Select-Object -First 1
    $needle = if ($Major -eq 8) { '1.8.' } else { "version `"$Major" }
    if ($ver -notlike "*$needle*") {
        throw "JDK at '$home_dir' is not Java $Major (java -version reports: $ver)."
    }
    return $home_dir
}

# Session-scoped JDK switch (does not touch your system default).
function Set-Java {
    param([string]$JavaHome)
    $env:JAVA_HOME = $JavaHome
    $env:Path      = "$env:JAVA_HOME\bin;$env:Path"
}

# Builds one repo, copies its .omod(s) to $OmodDir, returns a result object.
function Invoke-ModuleBuild {
    param(
        [Parameter(Mandatory)] [string]$Repo,
        [Parameter(Mandatory)] [string]$Label,
        [switch]$SkipTests
    )

    $mvnArgs = @('-B', '-ntp', 'clean', 'package')
    if ($SkipTests) { $mvnArgs += '-DskipTests' }

    Write-Host ""
    Write-Host "================================================================" -ForegroundColor DarkGray
    Write-Host "=== Building: $Label" -ForegroundColor Cyan
    Write-Host "    JDK: $($env:JAVA_HOME)"
    Write-Host "    mvn $($mvnArgs -join ' ')"
    Write-Host "================================================================" -ForegroundColor DarkGray

    Push-Location $Repo
    try {
        # Tee-Object writes each line to the per-module log AND passes it on;
        # Out-Host renders it LIVE and consumes it so mvn output does not become
        # this function's return value. We tee stdout only (no 2>&1): merging
        # native stderr turns JVM warnings into NativeCommandError blocks and,
        # under EAP=Stop, aborts the run. EAP is briefly dropped as a guard.
        $logFile = Join-Path $LogDir "$Label.log"
        $prevEAP = $ErrorActionPreference
        $ErrorActionPreference = 'SilentlyContinue'
        try {
            & mvn @mvnArgs | Tee-Object -FilePath $logFile | Out-Host
        }
        finally {
            $ErrorActionPreference = $prevEAP
        }
        $exitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }

    $result = [pscustomobject]@{
        Label = $Label; Repo = $Repo; Success = ($exitCode -eq 0); Omods = @(); SkipTests = [bool]$SkipTests
    }
    if ($result.Success) {
        # Only the real artifact lives in <repo>\omod\target -- avoids test-fixture omods.
        $omodTarget = Join-Path $Repo (Join-Path 'omod' 'target')
        if (Test-Path $omodTarget) {
            foreach ($o in @(Get-ChildItem -Path $omodTarget -Filter '*.omod' -File -ErrorAction SilentlyContinue)) {
                Copy-Item -Path $o.FullName -Destination $OmodDir -Force
                $result.Omods += $o.Name
            }
        }
        if ($result.Omods.Count -eq 0) {
            Write-Host "  WARNING: build succeeded but no .omod found in $omodTarget" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "  FAILED -- see log: $logFile" -ForegroundColor Red
    }
    return $result
}

# --------------------------------------------------------------------------- #
# Pre-flight
# --------------------------------------------------------------------------- #
if (-not (Test-Path $ModulesCsv)) { throw "modules.csv not found at '$ModulesCsv'." }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "mvn not found on PATH. Install Maven (and add to PATH) before running this script."
}

$Jdk21Home = Resolve-Jdk -Explicit $Java21 -EnvVar 'JAVA21_HOME' -Major 21
$Jdk8Home  = Resolve-Jdk -Explicit $Java8  -EnvVar 'JAVA8_HOME'  -Major 8

New-Item -ItemType Directory -Force -Path $OmodDir | Out-Null
New-Item -ItemType Directory -Force -Path $LogDir  | Out-Null

$rows = @(Import-Csv -Path $ModulesCsv)
if ($Only) { $rows = @($rows | Where-Object { $_.repo -like "*$Only*" }) }

# Partition rows by build group, preserving CSV order within each group.
$g21        = @($rows | Where-Object { $_.group -eq 'java21' })
$g8         = @($rows | Where-Object { $_.group -eq 'java8' })
$g21Skip    = @($rows | Where-Object { $_.group -eq 'java21-skipTests' })

Write-Host "OpenMRS backend module build" -ForegroundColor Green
Write-Host "Workspace root : $Destination"
Write-Host "JDK 21         : $Jdk21Home"
Write-Host "JDK 8          : $Jdk8Home"
Write-Host "omod output    : $OmodDir"
Write-Host "build logs     : $LogDir"
Write-Host ("Modules        : {0} total ({1} Java21 / {2} Java8 / {3} -DskipTests)" -f `
    $rows.Count, $g21.Count, $g8.Count, $g21Skip.Count)
Write-Host ""

$results = @()
$batchStart = Get-Date

try {
    if ($g21.Count -gt 0) {
        Set-Java $Jdk21Home
        Write-Host "################## Group 1/3: Java 21 ($($g21.Count) modules) ##################" -ForegroundColor Green
        foreach ($r in $g21) {
            $results += Invoke-ModuleBuild -Repo (Resolve-RepoPath $r.category $r.repo) -Label $r.repo
        }
    }

    if ($g8.Count -gt 0) {
        Set-Java $Jdk8Home
        Write-Host ""
        Write-Host "################## Group 2/3: Java 8 ($($g8.Count) modules) ####################" -ForegroundColor Green
        foreach ($r in $g8) {
            $results += Invoke-ModuleBuild -Repo (Resolve-RepoPath $r.category $r.repo) -Label $r.repo
        }
    }

    if ($g21Skip.Count -gt 0) {
        Set-Java $Jdk21Home
        Write-Host ""
        Write-Host "################## Group 3/3: Java 21 -DskipTests ($($g21Skip.Count) modules) ###" -ForegroundColor Green
        foreach ($r in $g21Skip) {
            $results += Invoke-ModuleBuild -Repo (Resolve-RepoPath $r.category $r.repo) -Label $r.repo -SkipTests
        }
    }

    # --------------------------------------------------------------------------- #
    # Summary
    # --------------------------------------------------------------------------- #
    $elapsed = (Get-Date) - $batchStart
    Write-Host ""
    Write-Host "==================== BUILD SUMMARY ====================" -ForegroundColor Red
    Write-Host ("Total modules : {0}" -f $results.Count)
    Write-Host ("Succeeded     : {0}" -f ($results | Where-Object { $_.Success }).Count)
    Write-Host ("Failed        : {0}" -f ($results | Where-Object { -not $_.Success }).Count)
    Write-Host ("Elapsed       : {0:hh\:mm\:ss}" -f $elapsed)
    Write-Host "--------------------------------------------------------"

    foreach ($r in $results) {
        if ($r.Success) {
            $tag  = if ($r.SkipTests) { 'OK (skipTests)' } else { 'OK' }
            $omod = if ($r.Omods.Count -gt 0) { ($r.Omods -join ', ') } else { '<no .omod>' }
            Write-Host ("  [{0,-14}] {1}  ->  {2}" -f $tag, $r.Label, $omod) -ForegroundColor Green
        }
        else {
            Write-Host ("  [{0,-14}] {1}" -f 'FAILED', $r.Label) -ForegroundColor Red
        }
    }

    $failed = @($results | Where-Object { -not $_.Success })
    if ($failed.Count -gt 0) {
        Write-Host ""
        Write-Host "Failed modules (re-run individually; logs in $LogDir):" -ForegroundColor Red
        foreach ($f in $failed) { Write-Host "  - $($f.Label)  ($($f.Repo))" }
        Write-Host "Tip: run .\retry-failed-builds.ps1 to re-run these under the same profile." -ForegroundColor Cyan
    }

    $collected = @(Get-ChildItem -Path $OmodDir -Filter '*.omod' -File -ErrorAction SilentlyContinue)
    Write-Host ""
    Write-Host (".omod files now in omod\ ({0}):" -f $collected.Count) -ForegroundColor Cyan
    if ($collected.Count -gt 0) { $collected | ForEach-Object { Write-Host ("  {0}" -f $_.Name) } }
    else { Write-Host "  <none>" }
    Write-Host "========================================================" -ForegroundColor Red
}
finally {
    # Per-module mvn logs are in $LogDir\<module>.log (one per build). The console
    # above already streamed the same output live; open a log to re-read a failure.
}

# --------------------------------------------------------------------------- #
# openmrs-core (the platform) -- NOT part of the automatic batch above.
# The modules resolve openmrs-api from your local .m2, so core is only needed
# when you have changed it. It produces openmrs.war (and omods), not a single
# module omod. Uncomment to build it separately on Java 21. Use `install` (not
# `package`) if you want the modules to pick up your core changes.
# --------------------------------------------------------------------------- #
# Set-Java $Jdk21Home
# $coreRepo = Join-Path $Destination 'Backend\openmrs-core'
# Push-Location $coreRepo
# try { mvn -B -ntp clean install -DskipTests } finally { Pop-Location }
