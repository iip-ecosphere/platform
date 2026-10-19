# OPC UA parser benchmark

This benchmark measures the time of one complete `DomParser.process` invocation per NodeSet. For each version
(baseline, optimized) and NodeSet it starts one fresh JVM, runs a few warm-up iterations first and then the measured
iterations in the same JVM. Only the measured iterations go into the statistics.

## Comparison commits

Use these commits to isolate the indexed-lookup change merged through pull request 18:

- baseline: `9ca113e6ee1e664e7a1496dd0e68f01dade98601`
- optimized: `3468e4557335af9e72904d65c69099dc7284c558`

The optimized commit is the merge commit on `elizaveta-andreeva/platform` `main`; the baseline is its first parent.
The benchmark inputs are identical between them.

## Requirements

Ubuntu, Java 17 or newer (the harness uses `HexFormat`), Maven, Python 3, GNU `timeout`, `shuf` and `stat`.

## Prepare Ubuntu worktrees

From a current clone of `elizaveta-andreeva/platform`, create the patches from the benchmark branch and set up both
worktrees:

```bash
git fetch origin --prune
git format-patch origin/main..feat/fix_benchmark_test -o /tmp/benchmark-patch
git worktree add -b benchmark-new ../platform-benchmark-new origin/main
git worktree add --detach ../platform-benchmark-base 9ca113e6ee1e664e7a1496dd0e68f01dade98601
```

The benchmark harness does not exist in either commit, so apply the patches to both worktrees:

```bash
git -C ../platform-benchmark-new apply /tmp/benchmark-patch/*.patch
git -C ../platform-benchmark-base apply /tmp/benchmark-patch/*.patch
```

The detached baseline worktree is used only to produce measurements. For an offline run (`MVN_OFFLINE=1`), fetch all
dependencies once in both worktrees before the first run:

```bash
(cd ../platform-benchmark-new/platform/configuration/configuration.easy && mvn -PCfg dependency:go-offline)
(cd ../platform-benchmark-base/platform/configuration/configuration.easy && mvn -PCfg dependency:go-offline)
```

## Run

Run the script from the optimized worktree, passing both repository roots:

```bash
cd ../platform-benchmark-new/platform/configuration/configuration.easy/benchmarking
chmod +x run-benchmark.sh summarize-benchmark.py
./run-benchmark.sh \
    ../../../../../platform-benchmark-base \
    ../../../.. \
    5
```

The arguments are the baseline root, the optimized root, the number of measured iterations per NodeSet and version
(default 5) and an optional results directory.

The script first compiles both versions with the `Cfg` Maven profile (without it, this module's default profile skips
compilation and tests) and writes the classpath of each version. It then randomizes the NodeSet order and, for every
NodeSet, measures baseline first and optimized second. Each measurement starts a plain `java` process that runs the
benchmark test through JUnit directly, so neither Maven nor surefire is part of the run. Every JVM runs with a fixed
heap of 4 GB, performs the warm-up iterations and then the measured iterations. The time is taken with
`System.nanoTime()` around `DomParser.process` only.

Warm-up iterations depend on the size of the NodeSet: 4 for files smaller than 300 KB, 1 for all others.


The following environment variables change the defaults:

| Variable | Default | Meaning |
|---|---|---|
| `WARMUP` | 1 | warm-up iterations for NodeSets of 300 KB or more |
| `SMALL_WARMUP` | 4 | warm-up iterations for smaller NodeSets |
| `SMALL_KB` | 300 | size limit in KB below which a NodeSet counts as small |
| `HEAP` | 4g | `-Xms` and `-Xmx` of the measured JVM |
| `BENCHMARK_TIMEOUT` | 6h | GNU `timeout` duration for one JVM |
| `MVN_OFFLINE` | unset | set to `1` to run Maven with `-o` |

Example: `WARMUP=2 SMALL_WARMUP=6 MVN_OFFLINE=1 ./run-benchmark.sh ...`

## Results

Results are written to a timestamped `benchmark-results-*` directory:

- `raw/`: one CSV for each version and NodeSet with one row per iteration; rounds 0 and below are warm-up iterations,
  rounds 1 to N are the measured ones
- `all-results.csv`: all raw rows with one header
- `summary_baseline.csv` and `summary_optimized.csv`: one row per NodeSet with the columns `Run1Ms` to `RunNMs`,
  `AvgMs`, `MedianMs`, `StdMs` (sample standard deviation), `CvPct` (standard deviation relative to the mean, in
  percent) and the model statistics; warm-up iterations are not included
- `environment.txt`: commits, warm-up settings, heap, Java version and Maven version
- `classpath_baseline.txt` and `classpath_optimized.txt`: the classpaths used for the runs

The summary script prints a warning if the output hash differs between iterations or between versions. Fix that before
accepting a performance comparison.

