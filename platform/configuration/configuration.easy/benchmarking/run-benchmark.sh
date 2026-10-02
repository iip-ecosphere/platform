#!/usr/bin/env bash
set -euo pipefail

if (( $# < 2 || $# > 4 )); then
    echo "Usage: $0 BASELINE_WORKTREE OPTIMIZED_WORKTREE [ROUNDS] [RESULTS_DIR]" >&2
    exit 2
fi

baseline_root=$(realpath "$1")
optimized_root=$(realpath "$2")
rounds=${3:-5}
results_dir=${4:-"$PWD/benchmark-results-$(date +%Y%m%d-%H%M%S)"}
mkdir -p "$results_dir/raw"
results_dir=$(realpath "$results_dir")

module_path="platform/configuration/configuration.easy"
baseline_module="$baseline_root/$module_path"
optimized_module="$optimized_root/$module_path"
benchmark_class="DomParserBenchmarkIT"

for command in java mvn python3 shuf timeout; do
    if ! command -v "$command" >/dev/null 2>&1; then
        echo "Required command is not installed: $command" >&2
        exit 1
    fi
done

if ! [[ "$rounds" =~ ^[1-9][0-9]*$ ]]; then
    echo "ROUNDS must be a positive integer: $rounds" >&2
    exit 2
fi

for module in "$baseline_module" "$optimized_module"; do
    if [[ ! -f "$module/pom.xml" ]]; then
        echo "Maven module not found: $module" >&2
        exit 1
    fi
    benchmark_file="$module/src/test/java/test/de/iip_ecosphere/platform/configuration/easyProducer/opcua/${benchmark_class}.java"
    if [[ ! -f "$benchmark_file" ]]; then
        echo "Benchmark harness not found in $module" >&2
        echo "Apply the benchmark patch to both worktrees before running this script." >&2
        exit 1
    fi
done

{
    echo "created_at=$(date --iso-8601=seconds)"
    echo "baseline_commit=$(git -C "$baseline_root" rev-parse HEAD)"
    echo "optimized_commit=$(git -C "$optimized_root" rev-parse HEAD)"
    echo "rounds=$rounds"
    echo "surefire_exit_timeout=${SUREFIRE_EXIT_TIMEOUT:-3}"
    java -version
    mvn -version
} >"$results_dir/environment.txt" 2>&1

mapfile -t nodesets < <(
    # Exclude the negative fixture that intentionally has an unknown namespace.
    find "$optimized_module/src/test/resources/NodeSets" -maxdepth 1 -type f -iname '*.xml' ! -name 'Opc.Ua.ExternalReferenceUnknownNamespace.NodeSet2.xml' -printf '%f\n' \
        | sort
)
if (( ${#nodesets[@]} == 0 )); then
    echo "No top-level NodeSet XML files found." >&2
    exit 1
fi

for nodeset in "${nodesets[@]}"; do
    if [[ ! -f "$baseline_module/src/test/resources/NodeSets/$nodeset" ]]; then
        echo "NodeSet is missing from the baseline worktree: $nodeset" >&2
        exit 1
    fi
done

echo "Compiling benchmark code in both worktrees..."
(cd "$baseline_module" && mvn -q -PCfg -DskipTests test-compile)
(cd "$optimized_module" && mvn -q -PCfg -DskipTests test-compile)

run_measurement() {
    local version=$1
    local module=$2
    local round=$3
    local nodeset=$4
    local safe_name=${nodeset//[^A-Za-z0-9._-]/_}
    local output="$results_dir/raw/${version}_round${round}_${safe_name}.csv"

    if [[ -f "$output" ]] && grep -q ',OK,OK$' "$output"; then
        echo "[skip] version=$version round=$round nodeset=$nodeset"
        return
    fi

    rm -f "$output"
    echo "[$version] round=$round nodeset=$nodeset"
    (
        cd "$module"
        timeout --foreground "${BENCHMARK_TIMEOUT:-2h}" mvn -q \
            -PCfg \
            "-Dtest=$benchmark_class" \
            "-Dnodeset=$nodeset" \
            "-Dbenchmark.version=$version" \
            "-Dbenchmark.round=$round" \
            "-Dbenchmark.output=$output" \
            "-DargLine=-Xms2g -Xmx2g" \
            "-Dsurefire.exitTimeout=${SUREFIRE_EXIT_TIMEOUT:-3}" \
            test </dev/null
    )
}

for round in $(seq 1 "$rounds"); do
    mapfile -t shuffled_nodesets < <(printf '%s\n' "${nodesets[@]}" | shuf)
    index=0
    for nodeset in "${shuffled_nodesets[@]}"; do
        if (( (round + index) % 2 == 0 )); then
            run_measurement baseline "$baseline_module" "$round" "$nodeset"
            run_measurement optimized "$optimized_module" "$round" "$nodeset"
        else
            run_measurement optimized "$optimized_module" "$round" "$nodeset"
            run_measurement baseline "$baseline_module" "$round" "$nodeset"
        fi
        ((index += 1))
    done
done

mapfile -d '' -t result_files < <(find "$results_dir/raw" -type f -name '*.csv' -print0 | sort -z)
if (( ${#result_files[@]} == 0 )); then
    echo "No benchmark result files were produced." >&2
    exit 1
fi

combined="$results_dir/all-results.csv"
head -n 1 "${result_files[0]}" >"$combined"
for result_file in "${result_files[@]}"; do
    tail -n +2 "$result_file" >>"$combined"
done

python3 "$(dirname "$0")/summarize-benchmark.py" "$combined" "$results_dir"

echo "Benchmark complete."
echo "Raw and summary results: $results_dir"
