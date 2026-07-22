#!/usr/bin/env bash
#
# Re-runs the modules that failed under build-modules.sh and, if they still fail,
# captures the failure in maximum detail. Bash mirror of retry-failed-builds.ps1.
#
# Flow:
#   1. Decide what to retry: --modules list, else auto-detect failures from
#      .build-logs/<module>.log (last BUILD FAILURE / no marker / no log).
#   2. Look up each module's profile (JDK 21 / JDK 8 / -DskipTests) from
#      modules.csv so the retry matches build-modules.sh exactly.
#   3. Run `mvn -B -ntp clean package [-DskipTests]`, streaming live + to log.
#   4. On second failure write .build-logs/<module>.failure.md (reactor summary,
#      all [ERROR] lines, failed goals, exception/Caused-by chain, last 40 lines)
#      and an aggregate .build-logs/retry-report.md.
#
# Usage:
#   ./retry-failed-builds.sh
#   ./retry-failed-builds.sh --modules openmrs-module-legacyui,openmrs-module-idgen
#   ./retry-failed-builds.sh --no-rebuild
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_CSV="${MODULES_CSV:-$SCRIPT_DIR/modules.csv}"
DESTINATION="${DESTINATION:-$(cd "$SCRIPT_DIR/../.." && pwd)}"
OMOD_DIR="$SCRIPT_DIR/omod"
LOG_DIR="$SCRIPT_DIR/.build-logs"
JAVA21="${JAVA21:-}"; JAVA8="${JAVA8:-}"
MODULES_CLI=""; NO_REBUILD=0

color() { printf '\033[%sm%s\033[0m' "$1" "$2"; }

while [[ $# -gt 0 ]]; do
    case "$1" in
        --java21)      JAVA21="$2"; shift 2 ;;
        --java8)       JAVA8="$2";  shift 2 ;;
        --destination) DESTINATION="$2"; shift 2 ;;
        --modules-csv) MODULES_CSV="$2"; shift 2 ;;
        --modules)     MODULES_CLI="$2"; shift 2 ;;
        --no-rebuild)  NO_REBUILD=1; shift ;;
        -h|--help)     sed -n '2,24p' "$0"; exit 0 ;;
        *) echo "Unknown arg: $1" >&2; exit 2 ;;
    esac
done

command -v mvn >/dev/null || { echo "mvn not found on PATH." >&2; exit 1; }
[[ -f "$MODULES_CSV" ]] || { echo "modules.csv not found at $MODULES_CSV" >&2; exit 1; }
mkdir -p "$OMOD_DIR" "$LOG_DIR"

# ---- JDK resolution (mirror of build-modules.sh) ----
resolve_jdk() {
    local explicit="$1" envvar="$2" major="$3" home_dir ver needle
    home_dir="$explicit"; [[ -z "$home_dir" ]] && home_dir="${!envvar}"; [[ -z "$home_dir" ]] && home_dir="${JAVA_HOME:-}"
    [[ -z "$home_dir" ]] && { echo "No JDK for Java $major (flag/env/JAVA_HOME)." >&2; exit 1; }
    [[ -x "$home_dir/bin/java" ]] || { echo "JDK not found at '$home_dir'." >&2; exit 1; }
    ver="$("$home_dir/bin/java" -version 2>&1 | head -n1)"
    if [[ "$major" == "8" ]]; then needle="1.8."; else needle="version \"$major"; fi
    [[ "$ver" == *"$needle"* ]] || { echo "JDK at '$home_dir' is not Java $major ($ver)." >&2; exit 1; }
    printf '%s' "$home_dir"
}
set_java() { export JAVA_HOME="$1"; export PATH="$JAVA_HOME/bin:$PATH"; }
JDK21_HOME="$(resolve_jdk "$JAVA21" JAVA21_HOME 21)"
JDK8_HOME="$(resolve_jdk "$JAVA8" JAVA8_HOME 8)"

resolve_repo() { local p="$DESTINATION/$1/$2"; [[ -d "$p/.git" ]] || { echo "Repo '$2' not found at '$p'." >&2; exit 1; }; printf '%s' "$p"; }

# Profile lookup from modules.csv: echoes "category|javahome|skip(0|1)".
get_profile() {
    local label="$1" row
    row="$(awk -F, -v l="$label" 'NR>1 && $2==l {print $1"|"$5; exit}' "$MODULES_CSV")"
    [[ -z "$row" ]] && { echo "No entry for '$label' in modules.csv." >&2; exit 1; }
    local cat="${row%%|*}" grp="${row##*|}"
    case "$grp" in
        java8)            echo "$cat|$JDK8_HOME|0" ;;
        java21-skipTests) echo "$cat|$JDK21_HOME|1" ;;
        *)                echo "$cat|$JDK21_HOME|0" ;;
    esac
}

last_build_failed() {   # <label> -> 0/1
    local log="$LOG_DIR/$1.log"
    [[ -f "$log" ]] || return 0
    local last
    last="$(grep -E 'BUILD (SUCCESS|FAILURE)' "$log" | tail -n1)"
    [[ "$last" == *"BUILD SUCCESS"* ]] && return 1 || return 0
}

extract_failure() {   # <logfile> <label>  -> markdown digest on stdout
    local log="$1" label="$2"
    [[ -f "$log" ]] || { echo "# $label -- log missing"; echo; echo "No log at $log."; return; }
    local lines; lines="$(wc -l < "$log")"
    echo "# Build failure: $label"; echo
    echo "- log: \`$log\`"; echo "- lines: $lines"; echo
    echo "## All [ERROR] lines"; echo '```'; grep '^\[ERROR\]' "$log" || true; echo '```'; echo
    echo "## Failed execution goals"; grep 'Failed to execute goal' "$log" | sed 's/^/- /' || true; echo
    echo "## Exception / stack-trace chain"
    grep -E 'Caused by:|^[[:space:]]*at .+\(.+:[0-9]+\)|^\[ERROR\] .*Exception|^\[ERROR\] .*Error' "$log" | awk '!seen[$0]++' || true
    echo
    echo "## Last 40 log lines"; echo '```'; tail -n40 "$log"; echo '```'
}

# ---- Decide what to retry ----
candidates=()
if [[ -n "$MODULES_CLI" ]]; then
    IFS=',' read -r -a candidates <<< "$MODULES_CLI"
else
    echo "Auto-detecting failed modules from $LOG_DIR ..."
    for f in "$LOG_DIR"/*.log; do
        [[ -f "$f" ]] || continue
        label="$(basename "$f" .log)"
        if last_build_failed "$label"; then candidates+=("$label"); fi
    done
fi

if [[ ${#candidates[@]} -eq 0 ]]; then
    echo "Nothing to retry -- no failed modules detected."; exit 0
fi

echo "Modules to retry (${#candidates[@]}):"
for c in "${candidates[@]}"; do echo "  - $c"; done
echo ""

results_count=0; recovered=0; still_bad=()
for label in "${candidates[@]}"; do
    IFS='|' read -r cat jhome skip <<< "$(get_profile "$label")"
    repo="$(resolve_repo "$cat" "$label")"
    log="$LOG_DIR/$label.log"

    if [[ "$NO_REBUILD" -eq 1 ]]; then
        if last_build_failed "$label"; then code=1; else code=0; fi
    else
        set_java "$jhome"
        mvn_args=(-B -ntp clean package); [[ "$skip" == "1" ]] && mvn_args+=(-DskipTests)
        echo ""; echo "=== Retrying: $label  (JDK $JAVA_HOME)"
        mvn "${mvn_args[@]}" 2>&1 | tee "$log" || true
        code="${PIPESTATUS[0]}"
        if [[ "$code" -eq 0 ]]; then
            find "$repo/omod/target" -maxdepth 1 -name '*.omod' -type f -print0 2>/dev/null |
                while IFS= read -r -d '' o; do cp -f "$o" "$OMOD_DIR/"; done
        fi
    fi

    results_count=$((results_count+1))
    if [[ "$code" -eq 0 ]]; then
        recovered=$((recovered+1))
        echo "  $(color 32 '[RECOVERED]') $label"
    else
        still_bad+=("$label")
        extract_failure "$log" "$label" > "$LOG_DIR/$label.failure.md"
        echo "  $(color 31 '[STILL BAD]') $label"
        echo "              log    : $log"
        echo "              digest : $LOG_DIR/$label.failure.md"
    fi
done

echo ""
echo "==================== RETRY SUMMARY ===================="
echo "Retried       : $results_count"
echo "Now succeed   : $recovered"
echo "Still failing : ${#still_bad[@]}"
echo "======================================================="

if [[ ${#still_bad[@]} -gt 0 ]]; then
    : > "$LOG_DIR/retry-report.md"
    echo "# OpenMRS backend retry report" >> "$LOG_DIR/retry-report.md"
    echo >> "$LOG_DIR/retry-report.md"
    echo "- retried: $results_count  recovered: $recovered  still failing: ${#still_bad[@]}" >> "$LOG_DIR/retry-report.md"
    echo >> "$LOG_DIR/retry-report.md"
    for f in "${still_bad[@]}"; do
        echo "---" >> "$LOG_DIR/retry-report.md"; echo >> "$LOG_DIR/retry-report.md"
        extract_failure "$LOG_DIR/$f.log" "$f" >> "$LOG_DIR/retry-report.md"
        echo >> "$LOG_DIR/retry-report.md"
    done
    echo "Aggregate report -> $LOG_DIR/retry-report.md"
fi
