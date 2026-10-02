#!/usr/bin/env python3
import csv
import statistics
import sys
from collections import defaultdict
from pathlib import Path

STATIC = (
    "IvmlLines",
    "UnknownDataTypes",
    "IvmlElements_RootObjectType",
    "IvmlElements_FieldVariableType",
    "IvmlElements_EnumType",
    "IvmlElements_ObjectTypeType",
    "InputLines",
    "InputOutputRatio",
    "CheckRequiredModels",
    "CheckRedundancy",
)


def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: summarize-benchmark.py INPUT.csv OUTPUT_DIR")
    input_file = Path(sys.argv[1])
    out_dir = Path(sys.argv[2])
    out_dir.mkdir(parents=True, exist_ok=True)

    with input_file.open(newline="", encoding="utf-8") as source:
        rows = list(csv.DictReader(source))

    grouped = defaultdict(lambda: defaultdict(list))
    for row in rows:
        grouped[row["Version"]][row["NodeSet"]].append(row)

    hashes = defaultdict(dict)
    for version, nodesets in grouped.items():
        rounds = max(len(runs) for runs in nodesets.values())
        columns = (
            ["NodeSet", "FileSizeKB", "UAObjectTypeCountIn"]
            + [f"Run{i}Ms" for i in range(1, rounds + 1)]
            + ["AvgMs", "MedianMs", "StdMs", "CvPct"]
            + list(STATIC)
        )
        target = out_dir / f"summary_{version}.csv"
        with target.open("w", newline="", encoding="utf-8") as dest:
            writer = csv.DictWriter(dest, fieldnames=columns, restval="")
            writer.writeheader()
            for nodeset in sorted(nodesets):
                runs = sorted(nodesets[nodeset], key=lambda r: int(r["Round"]))
                times = [int(r["TotalMs"]) for r in runs]
                first = runs[0]
                mean = statistics.mean(times)
                # sample standard deviation (n - 1); 0 if there is only one run
                std = statistics.stdev(times) if len(times) > 1 else 0.0
                cv = std / mean * 100 if mean else 0.0
                out = {
                    "NodeSet": nodeset,
                    "FileSizeKB": first["FileSizeKB"],
                    "UAObjectTypeCountIn": first["UAObjectTypeCountIn"],
                    "AvgMs": f"{mean:.1f}",
                    "MedianMs": f"{statistics.median(times):.1f}",
                    "StdMs": f"{std:.1f}",
                    "CvPct": f"{cv:.1f}",
                }
                for i, t in enumerate(times, 1):
                    out[f"Run{i}Ms"] = t
                for col in STATIC:
                    out[col] = first[col]
                writer.writerow(out)

                run_hashes = {r["OutputSha256"] for r in runs}
                if len(run_hashes) != 1 or "" in run_hashes:
                    print(f"WARNING: unstable or missing output hash: {version} {nodeset}")
                hashes[nodeset][version] = run_hashes
        print(f"Wrote {target}")

    for nodeset, by_version in sorted(hashes.items()):
        if len({frozenset(h) for h in by_version.values()}) > 1:
            print(f"WARNING: baseline and optimized output differ: {nodeset}")


if __name__ == "__main__":
    main()
