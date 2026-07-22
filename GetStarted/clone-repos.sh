#!/usr/bin/env bash
#
# Clones all OpenMRS backend modules from their public GitHub upstreams and
# checks out each module's team branch of interest. Bash mirror of
# clone-repos.ps1 (Mac / Linux / Git Bash on Windows).
#
# Reads ./modules.csv (category, repo, owner, branch, group). For each row:
#   1. clones https://github.com/<owner>/<repo>.git into
#      <Destination>/<Category>/<repo>. Default Destination is the workspace
#      root three levels above this script (the folder that holds
#      Backend/openmrs-core), matching the lead's layout.
#   2. if the folder already exists, runs `git fetch --all` (idempotent).
#   3. checks out <branch>; if missing on the remote, falls back to the repo's
#      default branch with a WARNING.
#
# The <branch> column already has the personal `-local` suffix stripped
# (main-local -> main, 3.x-local -> 3.x). Everything is public GitHub
# (openmrs org, Bahmni for appointments/teleconsultation, mekomsolutions for
# initializer) - no Azure DevOps credentials needed.
#
# Usage:
#   ./clone-repos.sh
#   ./clone-repos.sh --only openmrs-module-idgen
#   ./clone-repos.sh --destination "$HOME/code/OpenMRS"
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_CSV="${MODULES_CSV:-$SCRIPT_DIR/modules.csv}"
DESTINATION="${DESTINATION:-$(cd "$SCRIPT_DIR/../.." && pwd)}"
ONLY=""

color() { printf '\033[%sm%s\033[0m' "$1" "$2"; }   # color <code> <text>  (36=cyan 32=green 33=yellow 31=red 90=gray)

while [[ $# -gt 0 ]]; do
    case "$1" in
        --destination) DESTINATION="$2"; shift 2 ;;
        --modules-csv) MODULES_CSV="$2"; shift 2 ;;
        --only)        ONLY="$2"; shift 2 ;;
        -h|--help)
            sed -n '2,30p' "$0"; exit 0 ;;
        *) echo "Unknown arg: $1" >&2; exit 2 ;;
    esac
done

command -v git >/dev/null || { echo "git not found on PATH." >&2; exit 1; }
[[ -f "$MODULES_CSV" ]]  || { echo "modules.csv not found at $MODULES_CSV" >&2; exit 1; }

echo "OpenMRS backend module clone"
echo "Workspace root : $DESTINATION"
echo "modules.csv    : $MODULES_CSV"
[[ -n "$ONLY" ]] && echo "Filter         : *$ONLY*"
echo ""

cloned=(); present=(); fallback=(); failed=()

# Read CSV, skip the header row.
{
    read -r _   # header
    while IFS=, read -r category repo owner branch group; do
        [[ -z "$repo" ]] && continue
        if [[ -n "$ONLY" && "$repo" != *"$ONLY"* ]]; then continue; fi

        url="https://github.com/${owner}/${repo}.git"
        target="$DESTINATION/$category/$repo"
        echo "---- $repo  ($owner/$branch)"

        if [[ -d "$target/.git" ]]; then
            echo "     $(color 90 'already cloned -> fetching')"
            if ! git -C "$target" fetch --all --prune >/tmp/omrs-clone.$$ 2>&1; then
                echo "     $(color 31 'FAIL: git fetch failed')"; failed+=("$repo (git fetch failed)")
                cat /tmp/omrs-clone.$$; rm -f /tmp/omrs-clone.$$; continue
            fi
            rm -f /tmp/omrs-clone.$$
            present+=("$repo")
        else
            mkdir -p "$DESTINATION/$category"
            echo "     $(color 90 "cloning $url")"
            if ! git clone "$url" "$target" >/tmp/omrs-clone.$$ 2>&1; then
                echo "     $(color 31 'FAIL: git clone failed')"; failed+=("$repo (git clone failed)")
                cat /tmp/omrs-clone.$$; rm -f /tmp/omrs-clone.$$; continue
            fi
            rm -f /tmp/omrs-clone.$$
            cloned+=("$repo")
        fi

        # Checkout the team branch; fall back to default if missing on the remote.
        if git -C "$target" rev-parse --verify --quiet "refs/remotes/origin/$branch" >/dev/null 2>&1; then
            if git -C "$target" checkout "$branch" >/dev/null 2>&1; then
                echo "     $(color 32 "OK: checked out '$branch'")"
            elif git -C "$target" checkout -B "$branch" "origin/$branch" >/dev/null 2>&1; then
                echo "     $(color 32 "OK: created/checked out '$branch' <- origin/$branch")"
            else
                echo "     $(color 31 "FAIL: could not check out '$branch'")"
                failed+=("$repo (could not check out branch '$branch')")
                continue
            fi
        else
            default="$(git -C "$target" rev-parse --abbrev-ref origin/HEAD 2>/dev/null | sed 's#^origin/##')"
            [[ -z "$default" ]] && default="$(git -C "$target" symbolic-ref --short HEAD 2>/dev/null || echo '<unknown>')"
            fallback+=("$repo (wanted '$branch', got '$default')")
            echo "     $(color 33 "WARNING: branch '$branch' not on remote -> staying on default '$default'")"
        fi
    done
} < "$MODULES_CSV"

echo ""
echo "==================== CLONE SUMMARY ===================="
echo "Newly cloned   : ${#cloned[@]}"
echo "Already present: ${#present[@]}"
echo "Fell back      : ${#fallback[@]}"
echo "Failed         : ${#failed[@]}"
echo "-------------------------------------------------------"
for r in "${cloned[@]:-}";  do [[ -n "$r" ]] && echo "  cloned   $r"; done
for r in "${present[@]:-}"; do [[ -n "$r" ]] && echo "  present  $r"; done
if [[ ${#fallback[@]} -gt 0 ]]; then
    echo ""
    echo "Branches that fell back to the default (review these):"
    for r in "${fallback[@]}"; do echo "  $r"; done
fi
if [[ ${#failed[@]} -gt 0 ]]; then
    echo ""
    echo "Failures:"
    for r in "${failed[@]}"; do echo "  $r"; done
fi
echo "======================================================="
