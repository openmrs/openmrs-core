#!/usr/bin/env bash
#
# Adds the canonical `upstream` remote to every OpenMRS repo under the workspace
# and refreshes each branch from it. Bash mirror of refresh-from-upstream.ps1
# (port of the lead's sync-upstreams.ps1).
#
# For each git repo one level under --root (Backend/<repo>, Dependencies/<repo>,
# Frontend/<repo>):
#   1. ensure `upstream` -> https://github.com/<owner>/<repo>.git (owner map
#      below; defaults to 'openmrs').
#   2. fetch upstream.
#   3. branch handling:
#        *-local -> leave -local untouched; fast-forward the base branch
#                   (suffix stripped) via `git fetch upstream <base>:<base>`.
#        other   -> fast-forward only against upstream/<branch>.
#      Dirty trees / diverged histories are skipped with a warning.
#
# `origin` is never changed. No history is rewritten.
#
# Usage: ./refresh-from-upstream.sh [--root <path>] [--skip-remote-setup]
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="${ROOT:-$(cd "$SCRIPT_DIR/../.." && pwd)}"
SKIP_REMOTE_SETUP=0

color() { printf '\033[%sm%s\033[0m' "$1" "$2"; }

while [[ $# -gt 0 ]]; do
    case "$1" in
        --root)              ROOT="$2"; shift 2 ;;
        --skip-remote-setup) SKIP_REMOTE_SETUP=1; shift ;;
        -h|--help)           sed -n '2,26p' "$0"; exit 0 ;;
        *) echo "Unknown arg: $1" >&2; exit 2 ;;
    esac
done

[[ -d "$ROOT" ]] || { echo "Root not found: $ROOT" >&2; exit 1; }

declare -A OWNER_OVERRIDES=(
    [openmrs-module-appointments]=Bahmni
    [openmrs-module-initializer]=mekomsolutions
    [openmrs-module-teleconsultation]=Bahmni
)
upstream_url() {
    local repo="$1" owner
    owner="${OWNER_OVERRIDES[$repo]:-openmrs}"
    printf 'https://github.com/%s/%s.git' "$owner" "$repo"
}

updated=(); uptodate=(); failed=()
fail() { failed+=("$1"); echo "$(color 33 'FAIL  ') $1"; }

# Discover repos one level under $ROOT.
mapfile -t REPOS < <(find "$ROOT" -mindepth 2 -maxdepth 2 -type d -name '.git' -printf '%h\n' 2>/dev/null)
[[ ${#REPOS[@]} -gt 0 ]] || { echo "No git repos found under $ROOT" >&2; exit 1; }

for path in "${REPOS[@]}"; do
    name="$(basename "$path")"
    branch="$(git -C "$path" rev-parse --abbrev-ref HEAD 2>/dev/null || true)"
    [[ -n "$branch" ]] || { fail "$name  (could not read current branch)"; continue; }

    url="$(upstream_url "$name")"
    if [[ "$SKIP_REMOTE_SETUP" -eq 0 ]]; then
        existing="$(git -C "$path" remote get-url upstream 2>/dev/null || true)"
        if [[ -z "$existing" ]]; then
            git -C "$path" remote add upstream "$url"
            echo "$(color 36 'setup') $name  (added upstream -> $url)"
        elif [[ "$existing" != "$url" ]]; then
            fail "$name  (upstream already '$existing', expected '$url' - left as-is)"
        fi
    fi

    is_local=0; [[ "$branch" == *-local ]] && is_local=1
    if [[ "$is_local" -eq 1 ]]; then base="${branch%-local}"; else base="$branch"; fi
    target="upstream/$base"

    echo "$(color 90 '.....') $name  (fetching $target)"
    if ! git -C "$path" fetch upstream "$base" >/dev/null 2>&1; then
        fail "$name  (fetch $target failed)"; continue
    fi
    if ! git -C "$path" rev-parse --verify --quiet "refs/remotes/$target" >/dev/null 2>&1; then
        fail "$name  (upstream has no branch '$base')"; continue
    fi

    if [[ "$is_local" -eq 1 ]]; then
        behind="$(git -C "$path" rev-list --count "$base..$target" 2>/dev/null || echo 0)"
        if [[ "${behind:-0}" -eq 0 ]]; then
            echo "$(color 90 'IDLE  ') $name  ($branch: base '$base' up to date)"
            uptodate+=("$name  (base '$base')"); continue
        fi
        if git -C "$path" fetch upstream "${base}:${base}" >/dev/null 2>&1; then
            echo "$(color 32 'OK    ') $name  ($branch: fast-forwarded base '$base' +$behind)"
            updated+=("$name  (base '$base' +$behind)")
        else
            fail "$name  (base '$base' cannot fast-forward - likely diverged)"
        fi
        continue
    fi

    if [[ -n "$(git -C "$path" status --porcelain 2>/dev/null)" ]]; then
        fail "$name  (uncommitted changes - not touched)"; continue
    fi
    before="$(git -C "$path" rev-parse HEAD 2>/dev/null)"
    if ! git -C "$path" merge --ff-only "$target" >/dev/null 2>&1; then
        fail "$name  (fast-forward failed - likely diverged)"; continue
    fi
    after="$(git -C "$path" rev-parse HEAD 2>/dev/null)"
    if [[ "$before" == "$after" ]]; then
        echo "$(color 90 'IDLE  ') $name  ($branch up to date)"
        uptodate+=("$name  ($branch)")
    else
        gain="$(git -C "$path" rev-list --count "$before..$after" 2>/dev/null || echo 0)"
        echo "$(color 32 'OK    ') $name  ($branch +$gain)"
        updated+=("$name  ($branch +$gain)")
    fi
done

echo ""
echo "==================== UPDATED ===================="
for r in "${updated[@]:-}"; do [[ -n "$r" ]] && echo "$(color 32 "  $r")"; done
[[ ${#updated[@]} -eq 0 ]] && echo "  (none)"
echo ""
echo "==================== FAILED ===================="
for r in "${failed[@]:-}"; do [[ -n "$r" ]] && echo "$(color 33 "  $r")"; done
[[ ${#failed[@]} -eq 0 ]] && echo "  (none)"
echo ""
echo "Summary: ${#updated[@]} updated, ${#uptodate[@]} up-to-date, ${#failed[@]} failed - ${#REPOS[@]} repos seen."
