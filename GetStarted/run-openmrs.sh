#!/usr/bin/env bash
#
# Runs openmrs-core 2.8.x under the Maven Jetty plugin. Bash mirror of
# run-openmrs.ps1 (Mac / Linux / Git Bash on Windows).
#
# Handles the three things people otherwise forget:
#   1. MAVEN_OPTS --add-opens flags openmrs-core needs on Java 21 (+ optional
#      JDWP debug agent via --debug).
#   2. OPENMRS_APPLICATION_DATA_DIRECTORY -> a repo-local folder so modules, the
#      H2 db, and runtime properties live in GetStarted/openmrs-data (not $HOME).
#   3. OPENMRS_INSTALLATION_SCRIPT for MySQL/PostgreSQL auto-install (same
#      mechanism the -Ph2 profile uses for H2). Templates ship under ./config.
#
# Usage:
#   ./run-openmrs.sh                     # H2, in-memory, zero-config
#   ./run-openmrs.sh --mode mysql        # uses config/openmrs-installation.mysql.properties
#   ./run-openmrs.sh --mode postgres --pl-webapp --debug
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CORE_REPO="${CORE_REPO:-$(cd "$SCRIPT_DIR/.." && pwd)}"
DATA_DIR="${DATA_DIR:-$SCRIPT_DIR/openmrs-data}"
MODE="h2"; PORT=8080; PL_WEBAPP=0; DEBUG=0

color() { printf '\033[%sm%s\033[0m' "$1" "$2"; }

while [[ $# -gt 0 ]]; do
    case "$1" in
        --mode)     MODE="${2,,}"; shift 2 ;;       # bash 4+ lowercase
        --port)     PORT="$2"; shift 2 ;;
        --pl-webapp) PL_WEBAPP=1; shift ;;
        --debug)    DEBUG=1; shift ;;
        --data-dir) DATA_DIR="$2"; shift 2 ;;
        --core-repo) CORE_REPO="$2"; shift 2 ;;
        -h|--help)  sed -n '2,22p' "$0"; exit 0 ;;
        *) echo "Unknown arg: $1" >&2; exit 2 ;;
    esac
done

[[ "$MODE" =~ ^(h2|mysql|postgres)$ ]] || { echo "--mode must be h2|mysql|postgres" >&2; exit 2; }
[[ -f "$CORE_REPO/pom.xml" ]] || { echo "openmrs-core pom.xml not found at $CORE_REPO" >&2; exit 1; }

branch="$(git -C "$CORE_REPO" rev-parse --abbrev-ref HEAD 2>/dev/null || echo '')"
if [[ -n "$branch" && "$branch" != 2.8* ]]; then
    echo "$(color 33 "NOTE: openmrs-core is on branch '$branch'. These scripts target '2.8' (run: git -C \"$CORE_REPO\" checkout 2.8).")"
fi
mkdir -p "$DATA_DIR/modules"

# --- MAVEN_OPTS -------------------------------------------------------------- #
opts=(--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED)
if [[ "$DEBUG" -eq 1 ]]; then
    opts=(-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000 "${opts[@]}")
fi
export MAVEN_OPTS="${opts[*]}"

# --- Assemble the mvn invocation -------------------------------------------- #
args=()
if [[ "$MODE" == "h2" ]]; then
    args+=(-Ph2)
else
    template="$SCRIPT_DIR/config/openmrs-installation.${MODE}.properties"
    [[ -f "$template" ]] || { echo "Install template not found: $template" >&2; exit 1; }
    args+=("-DOPENMRS_INSTALLATION_SCRIPT=file://${template}")
fi
args+=("-Djetty.http.port=${PORT}" "-DOPENMRS_APPLICATION_DATA_DIRECTORY=${DATA_DIR}")
if [[ "$PL_WEBAPP" -eq 1 ]]; then args+=(-pl webapp); fi
args+=(jetty:run)

echo "openmrs-core Jetty run"
echo "  Core repo  : $CORE_REPO"
echo "  Branch     : ${branch:-<unknown>}"
echo "  Mode       : $MODE"
echo "  Port       : $PORT"
echo "  PlWebapp   : $([[ $PL_WEBAPP -eq 1 ]] && echo true || echo false)"
echo "  Data dir   : $DATA_DIR"
echo "  Modules    : $DATA_DIR/modules  <- copy your .omod files here"
echo "  MAVEN_OPTS : $MAVEN_OPTS"
echo "  mvn        : mvn ${args[*]}"
echo ""
echo "  $(color 36 "Open http://localhost:${PORT}/openmrs  (admin / Admin123)")"
echo ""

cd "$CORE_REPO"
exec mvn "${args[@]}"
