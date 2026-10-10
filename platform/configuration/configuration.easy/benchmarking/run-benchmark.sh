#!/usr/bin/env bash
set -euo pipefail

baseline_root=$(realpath "$1")
optimized_root=$(realpath "$2")
rounds=${3:-5}
warmup=${WARMUP:-1} # warm-up iterations for normal NodeSets
small_warmup=${SMALL_WARMUP:-4} # warm-up iterations for small NodeSets
small_kb=${SMALL_KB:-300} # NodeSets smaller than this (KB) count as small
heap=${HEAP:-4g}
results_dir=${4:-"$PWD/benchmark-results-$(date +%Y%m%d-%H%M%S)"}
mkdir -p "$results_dir/raw"
results_dir=$(realpath "$results_dir")

module_path="platform/configuration/configuration.easy"
baseline_module="$baseline_root/$module_path"
optimized_module="$optimized_root/$module_path"
benchmark_class="DomParserBenchmarkIT"
benchmark_test="test.de.iip_ecosphere.platform.configuration.easyProducer.opcua.$benchmark_class"


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
    echo "warmup=$warmup"
    echo "small_warmup=$small_warmup (NodeSets smaller than ${small_kb} KB)"
    echo "heap=-Xms$heap -Xmx$heap"
    echo "mode=one JVM per version and NodeSet, started directly with JUnitCore (no surefire)"
    echo "mvn_offline=${MVN_OFFLINE:-0}"
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

echo "Compiling benchmark code and building classpaths..."
for entry in "baseline:$baseline_module" "optimized:$optimized_module"; do
    version=${entry%%:*}
    module=${entry#*:}
    (
        cd "$module"
        mvn ${MVN_OFFLINE:+-o} -q -PCfg -DskipTests test-compile
        mvn ${MVN_OFFLINE:+-o} -q -PCfg dependency:build-classpath \
            "-Dmdep.outputFile=$results_dir/classpath_${version}.txt"
    )
done

warmup_for() {
    local size_kb=$(( $(stat -c %s "$optimized_module/src/test/resources/NodeSets/$1") / 1024 ))
    if (( size_kb < small_kb )); then
        echo "$small_warmup"
    else
        echo "$warmup"
    fi
}

run_measurement() {
    local version=$1
    local module=$2
    local nodeset=$3
    local safe_name=${nodeset//[^A-Za-z0-9._-]/_}
    local output="$results_dir/raw/${version}_${safe_name}.csv"
    local classpath_file="$results_dir/classpath_${version}.txt"
    local w
    w=$(warmup_for "$nodeset")
    local expected=$((w + rounds))

    rm -f "$output"
    echo "[$(date +%T)] [$version] nodeset=$nodeset ($w warm-up + $rounds measured)"
    local started=$SECONDS
    local status=0
    (
        cd "$module"
        timeout --foreground "${BENCHMARK_TIMEOUT:-6h}" java "-Xms$heap" "-Xmx$heap" \
            "-Dnodeset=$nodeset" \
            "-Dbenchmark.version=$version" \
            "-Dbenchmark.warmup=$w" \
            "-Dbenchmark.iterations=$rounds" \
            "-Dbenchmark.output=$output" \
            -cp "target/classes:target/test-classes:$(cat "$classpath_file")" \
            org.junit.runner.JUnitCore "$benchmark_test" \
            </dev/null
    ) || status=$?

    local ok_rows=0
    if [[ -f "$output" ]]; then
        ok_rows=$(grep -c ',OK,OK$' "$output" || true)
    fi
    if (( ok_rows == expected )); then
        echo "[$(date +%T)] [done] version=$version nodeset=$nodeset in $((SECONDS - started))s"
        if (( status != 0 )); then
            echo "[warn] version=$version nodeset=$nodeset: exit code $status, but all $expected iterations are recorded" >&2
        fi
        return 0
    fi
    echo "[error] version=$version nodeset=$nodeset: $ok_rows of $expected iterations recorded, exit code $status" >&2
    return 1
}

mapfile -t shuffled_nodesets < <(printf '%s\n' "${nodesets[@]}" | shuf)
for nodeset in "${shuffled_nodesets[@]}"; do
    run_measurement baseline "$baseline_module" "$nodeset"
    run_measurement optimized "$optimized_module" "$nodeset"
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

