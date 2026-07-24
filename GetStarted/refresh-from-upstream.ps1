<#
.SYNOPSIS
  Adds the canonical `upstream` remote to every OpenMRS repo under the workspace
  and refreshes each branch from it. Self-contained port of the lead's
  sync-upstreams.ps1, shipped here so teammates can refresh their own clones.

.DESCRIPTION
  Walks each git repo one level under -Root (Root\Backend\<repo>,
  Root\Dependencies\<repo>, Root\Frontend\<repo>) and:

    1. Ensures an `upstream` remote exists pointing at the repo's canonical
       GitHub source (see $OwnerOverrides; everything else defaults to 'openmrs').
    2. Fetches `upstream`.
    3. Branch handling:
         - *-local branch -> the -local working branch is left UNTOUCHED. The
           base branch (suffix stripped, e.g. 3.x-local -> 3.x) is fast-forwarded
           to upstream/<base> via `git fetch upstream <base>:<base>` (no checkout
           switch). Refuses if the base branch has diverged.
         - any other branch -> fast-forward only against upstream/<branch>.
           A dirty tree or diverged history is skipped with a warning.

  `origin` and its tracking config are never changed. No history is rewritten.

.PARAMETER Root
  The workspace root containing the Backend / Frontend / Dependencies folders.
  Defaults to three levels above this script.

.PARAMETER SkipRemoteSetup
  Skip adding/verifying the `upstream` remote (use if remotes are already set
  and you just want the sync).

.EXAMPLE
  .\refresh-from-upstream.ps1
#>
[CmdletBinding()]
param(
    [string]$Root,
    [switch]$SkipRemoteSetup
)

$ErrorActionPreference = 'Stop'
$ScriptDir = $PSScriptRoot
if (-not $Root) { $Root = (Get-Item (Join-Path $ScriptDir '..\..\..')).FullName }
if (-not (Test-Path $Root)) { throw "Root not found: $Root" }

# Canonical GitHub owner per repo. Anything not listed defaults to 'openmrs'.
$OwnerOverrides = @{
    'openmrs-module-appointments'     = 'Bahmni'
    'openmrs-module-initializer'      = 'mekomsolutions'
    'openmrs-module-teleconsultation' = 'Bahmni'
}
function Get-UpstreamUrl([string]$repoName) {
    $owner = if ($OwnerOverrides.ContainsKey($repoName)) { $OwnerOverrides[$repoName] } else { 'openmrs' }
    return "https://github.com/$owner/$repoName.git"
}

# Discover repos one level under $Root (category\<repo>).
$repos = @(
    Get-ChildItem -Directory $Root | ForEach-Object { Get-ChildItem -Directory $_.FullName }
) | Where-Object { Test-Path (Join-Path $_.FullName '.git') }

if (-not $repos) { throw "No git repos found under $Root" }

$updated  = New-Object System.Collections.Generic.List[string]
$upToDate = New-Object System.Collections.Generic.List[string]
$failed   = New-Object System.Collections.Generic.List[string]
function Fail([string]$msg) { $script:failed.Add($msg); Write-Host "FAIL  $msg" -ForegroundColor Yellow }

foreach ($repo in $repos) {
    $path   = $repo.FullName
    $name   = $repo.Name
    $branch = git -C $path rev-parse --abbrev-ref HEAD 2>$null
    if (-not $branch) { Fail "$name  (could not read current branch)"; continue }

    $upstreamUrl = Get-UpstreamUrl $name
    if (-not $SkipRemoteSetup) {
        $existing = git -C $path remote get-url upstream 2>$null
        if (-not $existing) {
            git -C $path remote add upstream $upstreamUrl 2>&1 | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkGray }
            Write-Host "setup $name  (added upstream -> $upstreamUrl)" -ForegroundColor Cyan
        }
        elseif ($existing -ne $upstreamUrl) {
            Fail "$name  (upstream already '$existing', expected '$upstreamUrl' - left as-is)"
        }
    }

    $isLocal = $branch -like '*-local'
    $base    = if ($isLocal) { $branch -replace '-local$', '' } else { $branch }
    $target  = "upstream/$base"

    Write-Host "..... $name  (fetching upstream/$base)" -ForegroundColor Gray
    git -C $path fetch upstream $base 2>&1 | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkGray }
    if ($LASTEXITCODE -ne 0) { Fail "$name  (fetch upstream/$base failed)"; continue }

    $hasTarget = git -C $path rev-parse --verify --quiet "refs/remotes/$target" 2>$null
    if (-not $hasTarget) { Fail "$name  (upstream has no branch '$base')"; continue }

    if ($isLocal) {
        $behind = [int](git -C $path rev-list --count "$base..$target" 2>$null)
        if ($behind -eq 0) {
            Write-Host "IDLE  $name  (${branch}: base '$base' already up to date)" -ForegroundColor DarkGray
            $upToDate.Add("$name  (base '$base')"); continue
        }
        git -C $path fetch upstream "${base}:${base}" 2>&1 | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkGray }
        if ($LASTEXITCODE -eq 0) {
            Write-Host "OK    $name  (${branch}: fast-forwarded base '$base' +$behind)" -ForegroundColor Green
            $updated.Add("$name  (base '$base' +$behind)")
        }
        else { Fail "$name  (base '$base' cannot fast-forward - likely diverged)" }
        continue
    }

    $dirty = git -C $path status --porcelain 2>$null
    if ($dirty) { Fail "$name  (uncommitted changes - not touched)"; continue }

    $before = git -C $path rev-parse HEAD 2>$null
    Write-Host "..... $name  ($branch <- $target)" -ForegroundColor Gray
    git -C $path merge --ff-only $target 2>&1 | ForEach-Object { Write-Host "      $_" -ForegroundColor DarkGray }
    if ($LASTEXITCODE -ne 0) { Fail "$name  (fast-forward failed - likely diverged)"; continue }
    $after = git -C $path rev-parse HEAD 2>$null
    if ($before -eq $after) {
        Write-Host "IDLE  $name  ($branch already up to date)" -ForegroundColor DarkGray
        $upToDate.Add("$name  ($branch)")
    }
    else {
        $gain = [int](git -C $path rev-list --count "$before..$after" 2>$null)
        Write-Host "OK    $name  ($branch +$gain)" -ForegroundColor Green
        $updated.Add("$name  ($branch +$gain)")
    }
}

Write-Host ""
Write-Host "==================== NO CHANGE ====================" -ForegroundColor DarkGray
if ($upToDate.Count) { $upToDate | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray } } else { Write-Host "  (none)" -ForegroundColor DarkGray }
Write-Host ""
Write-Host "==================== UPDATED ====================" -ForegroundColor Green
if ($updated.Count) { $updated | ForEach-Object { Write-Host "  $_" -ForegroundColor Green } } else { Write-Host "  (none)" -ForegroundColor DarkGray }
Write-Host ""
Write-Host "==================== FAILED ====================" -ForegroundColor Yellow
if ($failed.Count) { $failed | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow } } else { Write-Host "  (none)" -ForegroundColor DarkGray }
Write-Host ""
Write-Host ("Summary: {0} updated, {1} up-to-date, {2} failed - {3} repos seen." -f `
    $updated.Count, $upToDate.Count, $failed.Count, $repos.Count)
