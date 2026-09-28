# OPC UA parser benchmark

This benchmark measures the total time of one complete `DomParser.process` invocation in a fresh JVM.

## Comparison commits

Use these commits to isolate the indexed-lookup change merged through pull request 18:

- baseline: `9ca113e6ee1e664e7a1496dd0e68f01dade98601`
- optimized: `3468e4557335af9e72904d65c69099dc7284c558`

The optimized commit is the merge commit on `elizaveta-andreeva/platform` `main`; the baseline is its first parent.
The benchmark inputs are identical between them.

## Prepare Ubuntu worktrees

From a current clone of `elizaveta-andreeva/platform`, create a patch from the benchmark commit and set up both worktrees:

```bash
git fetch origin --prune
git format-patch -1 fix/opcua-benchmark-isolated-jvms -o /tmp/benchmark-patch
git worktree add -b benchmark-new ../platform-benchmark-new origin/main
git worktree add --detach ../platform-benchmark-base 9ca113e6ee1e664e7a1496dd0e68f01dade98601
```

The benchmark harness does not exist in either commit, so apply the patch to both worktrees:

```bash
git -C ../platform-benchmark-new am /tmp/benchmark-patch/*.patch
git -C ../platform-benchmark-base apply /tmp/benchmark-patch/*.patch
```

The detached baseline worktree is used only to produce measurements.

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

The script activates the module's `Cfg` Maven profile and compiles both versions before measuring. Without `-PCfg`,
this module's default profile skips compilation and tests. The script then randomizes the NodeSet order, alternates
which version runs first and starts a new Maven test JVM for every measurement with `-Xms2g -Xmx2g`.

If required models are missing, standard input is closed and the invocation fails rather than waiting for interactive
input. Each invocation also has a 30-minute timeout; set `BENCHMARK_TIMEOUT` to another GNU `timeout` duration when
needed.

Results are written to a timestamped `benchmark-results-*` directory:

- `raw/`: one CSV for each version, round and NodeSet
- `all-results.csv`: all raw rows with one header
- `summary_baseline.csv` and `summary_optimized.csv`: one row per NodeSet with the columns `Run1Ms` to `RunNMs`,
  `AvgMs`, `MedianMs` and the model statistics
- `environment.txt`: commits, Java version, Maven version and number of rounds

The summary script prints a warning if the output hash differs between runs or between versions. Fix that before
accepting a performance comparison. Use at least five rounds and increase to ten if timings vary a lot.
