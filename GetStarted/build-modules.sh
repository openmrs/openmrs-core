#!/usr/bin/env bash
#
# Builds every OpenMRS backend module for the 2.8.x branch and collects the
# produced .omod artifacts into ./omod/. Bash mirror of build-modules.ps1
# (Mac / Linux / Git Bash on Windows).
#
# Reads ./modules.csv. The `group` column drives the build profile:
#   java21           -> mvn -B -ntp clean package            (JDK 21)
#   java8            -> mvn -B -ntp clean package            (JDK 8)
#   java21-skipTests -> mvn -B -ntp clean package -DskipTests (JDK 21)
#
# Matrix (24 modules, chartsearchai & azureblob-storage excluded):
#   19 Java 21 plain | 3 Java 8 plain | 2 Java 21 -DskipTests.
#
# JDK resolution (priority): --java21 / --java8 flag, else $JAVA21_HOME /
# $JAVA8_HOME, else $JAVA_HOME (used for both if the specific vars are absent).
# The version is sanity-checked (21.x or 1.8.x) before any build runs.
#
# openmrs-core is NOT built here (it yields openmrs.war, not a module .omod) -
# see the commented block at the bottom.
#
# Usage:
#   ./build-modules.sh
#   ./build-modules.sh --java21 /opt/jdk-21 --java8 /opt/jdk-8
#   ./build-modules.sh --only openmrs-module-idgen
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_CSV="${MODULES_CSV:-$SCRIPT_DIR/modules.csv}"
DESTINATION="${DESTINATION:-$(cd "$SCRIPT_DIR/../.." && pwd)}"
OMOD_DIR="$SCRIPT_DIR/omod"
LOG_DIR="$SCRIPT_DIR/.build-logs"
JAVA21="${JAVA21:-}"
JAVA8="${JAVA8:-}"
ONLY=""

color() { printf '\033[%sm%s\033[0m' "$1" "$2"; }   # 31=red 32=green 33=yellow 36=cyan 90=gray

while [[ $# -gt 0 ]]; do
    case "$1" in
        --java21)     JAVA21="$2"; shift 2 ;;
        --java8)      JAVA8="$2";  shift 2 ;;
        --destination) DESTINATION="$2"; shift 2 ;;
        --modules-csv) MODULES_CSV="$2"; shift 2 ;;
        --only)       ONLY="$2"; shift 2 ;;
        -h|--help)    sed -n '2,30p' "$0"; exit 0 ;;
        *) echo "Unknown arg: $1" >&2; exit 2 ;;
    esac
done

command -v mvn >/dev/null || { echo "mvn not found on PATH. Install Maven first." >&2; exit 1; }
[[ -f "$MODULES_CSV" ]] || { echo "modules.csv not found at $MODULES_CSV" >&2; exit 1; }

# resolve_jdk <explicit> <envvar> <major>
resolve_jdk() {
    local explicit="$1" envvar="$2" major="$3" home_dir ver needle
    home_dir="$explicit"
    [[ -z "$home_dir" ]] && home_dir="${!envvar}"
    [[ -z "$home_dir" ]] && home_dir="${JAVA_HOME:-}"
    [[ -z "$home_dir" ]] && { echo "No JDK configured for Java $major. Pass --java$major or set $envvar (or JAVA_HOME)." >&2; exit 1; }
    [[ -x "$home_dir/bin/java" ]] || { echo "JDK not found at '$home_dir'." >&2; exit 1; }
    ver="$("$home_dir/bin/java" -version 2>&1 | head -n1)"
    if [[ "$major" == "8" ]]; then needle="1.8."; else needle="version \"$major"; fi
    [[ "$ver" == *"$needle"* ]] || { echo "JDK at '$home_dir' is not Java $major (java -version: $ver)." >&2; exit 1; }
    printf '%s' "$home_dir"
}

set_java() {   # session-scoped swap; system default untouched
    export JAVA_HOME="$1"
    export PATH="$JAVA_HOME/bin:$PATH"
}

resolve_repo() {   # <category> <repo> -> path, must exist
    local p="$DESTINATION/$1/$2"
    [[ -d "$p/.git" ]] || { echo "Repo '$2' not found at '$p'. Run clone-repos.sh first (or set DESTINATION)." >&2; exit 1; }
    printf '%s' "$p"
}

# build_one <repo-path> <label> <skip_tests:0|1>
build_one() {
    local repo="$1" label="$2" skip="$3"
    local mvn_args=(-B -ntp clean package)
    [[ "$skip" == "1" ]] && mvn_args+=(-DskipTests)

    echo ""
    echo "================================================================"
    echo "=== Building: $label"
    echo "    JDK: $JAVA_HOME"
    echo "    mvn ${mvn_args[*]}"
    echo "================================================================"

    local log="$LOG_DIR/$label.log"
    local code=0
    mvn "${mvn_args[@]}" 2>&1 | tee "$log" || true
    # tee + pipefail: re-read the true mvn exit from PIPESTATUS.
    code="${PIPESTATUS[0]}"

    local omods=()
    if [[ "$code" -eq 0 ]]; then
        local t="$repo/omod/target"
        if [[ -d "$t" ]]; then
            while IFS= read -r -d '' o; do
                cp -f "$o" "$OMOD_DIR/"
                omods+=("$(basename "$o")")
            done < <(find "$t" -maxdepth 1 -name '*.omod' -type f -print0 2>/dev/null)
        fi
        if [[ ${#omods[@]} -eq 0 ]]; then
            echo "  $(color 33 "WARNING: build succeeded but no .omod found in $t")"
        fi
        RESULTS_SUCCESS+=("$label"); RESULTS_SKIP+=("$skip")
        if [[ ${#omods[@]} -gt 0 ]]; then
            local joined; IFS=,; joined="${omods[*]}"; unset IFS
            RESULTS_OMODS+=("$joined")
        else
            RESULTS_OMODS+=("<no .omod>")
        fi
    else
        echo "  $(color 31 "FAILED -- see log: $log")"
        RESULTS_FAILED+=("$label ($repo)")
    fi
}

# Globals collected across the run (bash has no easy object lists).
RESULTS_SUCCESS=(); RESULTS_FAILED=(); RESULTS_SKIP=(); RESULTS_OMODS=()

JDK21_HOME="$(resolve_jdk "$JAVA21" JAVA21_HOME 21)"
JDK8_HOME="$(resolve_jdk "$JAVA8" JAVA8_HOME 8)"

mkdir -p "$OMOD_DIR" "$LOG_DIR"

# Partition the CSV by group, preserving order.
g21=(); g8=(); g21skip=(); total=0
{
    read -r _
    while IFS=, read -r category repo owner branch group; do
        [[ -z "$repo" ]] && continue
        [[ -n "$ONLY" && "$repo" != *"$ONLY"* ]] && continue
        total=$((total+1))
        case "$group" in
            java21)           g21+=("$category|$repo") ;;
            java8)            g8+=("$category|$repo") ;;
            java21-skipTests) g21skip+=("$category|$repo") ;;
            *) echo "Unknown group '$group' for $repo in modules.csv" >&2; exit 1 ;;
        esac
    done
} < "$MODULES_CSV"

echo "OpenMRS backend module build"
echo "Workspace root : $DESTINATION"
echo "JDK 21         : $JDK21_HOME"
echo "JDK 8          : $JDK8_HOME"
echo "omod output    : $OMOD_DIR"
echo "build logs     : $LOG_DIR"
echo "Modules        : $total total (${#g21[@]} Java21 / ${#g8[@]} Java8 / ${#g21skip[@]} -DskipTests)"
echo ""

batch_start=$(date +%s)

build_group() {   # <jq_home> <skip> <name> <count> <array...>
    local jhome="$1" skip="$2" name="$3"; shift 3
    [[ $# -eq 0 ]] && return 0
    set_java "$jhome"
    echo ""
    echo "################## $name ($# modules) ##################"
    for entry in "$@"; do
        local cat="${entry%%|*}" r="${entry##*|}"
        build_one "$(resolve_repo "$cat" "$r")" "$r" "$skip"
    done
}

build_group "$JDK21_HOME" 0 "Group 1/3: Java 21"            "${g21[@]}"
build_group "$JDK8_HOME"  0 "Group 2/3: Java 8"             "${g8[@]}"
build_group "$JDK21_HOME" 1 "Group 3/3: Java 21 -DskipTests" "${g21skip[@]}"

elapsed=$(( $(date +%s) - batch_start ))
printf -v elapsed_fmt '%02d:%02d:%02d' $((elapsed/3600)) $(((elapsed%3600)/60)) $((elapsed%60))

echo ""
echo "==================== BUILD SUMMARY ===================="
echo "Total modules : $total"
echo "Succeeded     : ${#RESULTS_SUCCESS[@]}"
echo "Failed        : ${#RESULTS_FAILED[@]}"
echo "Elapsed       : $elapsed_fmt"
echo "--------------------------------------------------------"
for i in "${!RESULTS_SUCCESS[@]}"; do
    if [[ "${RESULTS_SKIP[$i]}" == "1" ]]; then tag="OK (skipTests)"; else tag="OK"; fi
    printf '  [%-14s] %s  ->  %s\n' "$tag" "${RESULTS_SUCCESS[$i]}" "${RESULTS_OMODS[$i]}"
done
for f in "${RESULTS_FAILED[@]:-}"; do [[ -n "$f" ]] && echo "  [FAILED        ] $f"; done

if [[ ${#RESULTS_FAILED[@]} -gt 0 ]]; then
    echo ""
    echo "Failed modules (logs in $LOG_DIR):"
    for f in "${RESULTS_FAILED[@]}"; do echo "  - $f"; done
    echo "Tip: run ./retry-failed-builds.sh to re-run these under the same profile."
fi

shopt -s nullglob
collected=( "$OMOD_DIR"/*.omod )
shopt -u nullglob
echo ""
echo ".omod files now in omod/ (${#collected[@]}):"
if [[ ${#collected[@]} -gt 0 ]]; then for c in "${collected[@]}"; do echo "  $(basename "$c")"; done
else echo "  <none>"; fi
echo "======================================================="

# --------------------------------------------------------------------------- #
# openmrs-core (the platform) -- NOT part of the automatic batch above.
# Modules resolve openmrs-api from your local .m2, so core is only needed when
# you have changed it. It produces openmrs.war, not a module .omod. Uncomment to
# build it on Java 21 (use `install` if you want modules to pick up core changes):
#
# set_java "$JDK21_HOME"
# pushd "$DESTINATION/Backend/openmrs-core" >/dev/null
#   mvn -B -ntp clean install -DskipTests
# popd >/dev/null
# --------------------------------------------------------------------------- #
