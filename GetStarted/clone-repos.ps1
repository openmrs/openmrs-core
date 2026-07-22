<#
.SYNOPSIS
    Clones all OpenMRS backend modules from their public GitHub upstreams and
    checks out each module's team branch of interest.

.DESCRIPTION
    Reads ./modules.csv (category, repo, owner, branch, group) and, for each row:

      1. Clones https://github.com/<owner>/<repo>.git into
         <Destination>\<Category>\<repo>. Default Destination is the workspace
         root three levels above this script (i.e. the folder that already holds
         Backend\openmrs-core) so the layout matches the lead's workspace
         (Backend\<repo> and Dependencies\<repo>).
      2. If the folder already exists, runs `git fetch --all` instead of cloning
         (idempotent - safe to re-run).
      3. Checks out the row's <branch>. If that branch is missing on the remote,
         falls back to the repository's default branch and prints a WARNING
         naming the expected branch.

    The <branch> column is the team's branch of interest with any personal
    `-local` suffix already stripped (so `main-local` -> `main`, `3.x-local` ->
    `3.x`). Everything comes from the public GitHub upstreams (openmrs org, plus
    Bahmni for appointments/teleconsultation and mekomsolutions for initializer),
    so NO Azure DevOps credentials are required - anyone can run this.

    Excluded by design: openmrs-module-chartsearchai and
    openmrs-module-azureblob-storage (simply not present in modules.csv).

.PARAMETER Destination
    Workspace root to clone into. Defaults to three levels above this script
    (the folder containing Backend\openmrs-core).

.PARAMETER ModulesCsv
    Path to modules.csv. Defaults to modules.csv next to this script.

.PARAMETER Only
    Optional substring filter: only process repos whose name contains it
    (e.g. -Only idgen). Useful for smoke-testing one module.

.EXAMPLE
    .\clone-repos.ps1
    .\clone-repos.ps1 -Only openmrs-module-idgen
    .\clone-repos.ps1 -Destination D:\code\OpenMRS
#>
[CmdletBinding()]
param(
    [string]$Destination,
    [string]$ModulesCsv,
    [string]$Only
)

$ErrorActionPreference = 'Stop'

$ScriptDir = $PSScriptRoot
if (-not $ModulesCsv)  { $ModulesCsv  = Join-Path $ScriptDir 'modules.csv' }
if (-not $Destination) { $Destination = (Get-Item (Join-Path $ScriptDir '..\..\..')).FullName }

if (-not (Test-Path $ModulesCsv)) {
    throw "modules.csv not found at '$ModulesCsv'."
}
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "git not found on PATH. Install Git before running this script."
}

$rows = @(Import-Csv -Path $ModulesCsv)
if ($Only) { $rows = @($rows | Where-Object { $_.repo -like "*$Only*" }) }

if ($rows.Count -eq 0) {
    Write-Host "No modules to process." -ForegroundColor Yellow
    return
}

Write-Host "OpenMRS backend module clone" -ForegroundColor Green
Write-Host "Workspace root : $Destination"
Write-Host "modules.csv    : $ModulesCsv"
Write-Host "Modules        : $($rows.Count)"
if ($Only) { Write-Host "Filter         : *$Only*" }
Write-Host ""

$cloned  = @(); $present = @(); $fallback = @(); $failed = @()

foreach ($r in $rows) {
    $category = $r.category
    $repo     = $r.repo
    $owner    = $r.owner
    $branch   = $r.branch
    $url      = "https://github.com/$owner/$repo.git"
    $target   = Join-Path $Destination (Join-Path $category $repo)

    Write-Host "---- $repo  ($owner/$branch)" -ForegroundColor Cyan

    if (Test-Path (Join-Path $target '.git')) {
        Write-Host "     already cloned -> fetching" -ForegroundColor DarkGray
        git -C $target fetch --all --prune 2>&1 |
            ForEach-Object { Write-Host "       $_" -ForegroundColor DarkGray }
        if ($LASTEXITCODE -ne 0) {
            $failed += "$repo (git fetch failed at $target)"
            Write-Host "     FAIL: git fetch failed" -ForegroundColor Red
            continue
        }
        $present += $repo
    }
    else {
        New-Item -ItemType Directory -Force -Path (Join-Path $Destination $category) | Out-Null
        Write-Host "     cloning $url" -ForegroundColor DarkGray
        git clone $url $target 2>&1 |
            ForEach-Object { Write-Host "       $_" -ForegroundColor DarkGray }
        if ($LASTEXITCODE -ne 0) {
            $failed += "$repo (git clone failed)"
            Write-Host "     FAIL: git clone failed" -ForegroundColor Red
            continue
        }
        $cloned += $repo
    }

    # --- Checkout the team's branch of interest, fall back to default branch ---
    git -C $target rev-parse --verify --quiet "refs/remotes/origin/$branch" | Out-Null
    if ($LASTEXITCODE -eq 0) {
        git -C $target checkout $branch 2>&1 |
            ForEach-Object { Write-Host "       $_" -ForegroundColor DarkGray }
        if ($LASTEXITCODE -eq 0) {
            Write-Host "     OK: checked out '$branch'" -ForegroundColor Green
        }
        else {
            # Local branch may be absent; create it tracking origin.
            git -C $target checkout -B $branch "origin/$branch" 2>&1 |
                ForEach-Object { Write-Host "       $_" -ForegroundColor DarkGray }
            if ($LASTEXITCODE -eq 0) {
                Write-Host "     OK: created/checked out '$branch' <- origin/$branch" -ForegroundColor Green
            }
            else {
                $failed += "$repo (could not check out branch '$branch')"
                Write-Host "     FAIL: could not check out '$branch'" -ForegroundColor Red
                continue
            }
        }
    }
    else {
        # Branch missing on the upstream remote - fall back to default branch.
        $default = git -C $target rev-parse --abbrev-ref origin/HEAD 2>$null
        $default = ($default -replace '^origin/', '')
        if (-not $default) {
            # origin/HEAD not always set; fall back to the HEAD at clone time.
            $default = git -C $target symbolic-ref --short HEAD 2>$null
            if (-not $default) { $default = '<unknown>' }
        }
        $fallback += "$repo (wanted '$branch', got '$default')"
        Write-Host "     WARNING: branch '$branch' not on remote -> staying on default '$default'" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "==================== CLONE SUMMARY ====================" -ForegroundColor Green
Write-Host ("Newly cloned   : {0}" -f $cloned.Count)
Write-Host ("Already present: {0}" -f $present.Count)
Write-Host ("Fell back      : {0}" -f $fallback.Count)
Write-Host ("Failed         : {0}" -f $failed.Count)
Write-Host "-------------------------------------------------------"
if ($cloned.Count)  { $cloned  | ForEach-Object { Write-Host "  cloned   $_" -ForegroundColor Green } }
if ($present.Count) { $present | ForEach-Object { Write-Host "  present  $_" -ForegroundColor DarkGray } }
if ($fallback.Count) {
    Write-Host ""
    Write-Host "Branches that fell back to the default (review these):" -ForegroundColor Yellow
    $fallback | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
}
if ($failed.Count)  {
    Write-Host ""
    Write-Host "Failures:" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
}
Write-Host "=======================================================" -ForegroundColor Green
