#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/root/platform/logs"

mkdir -p "$LOG_DIR"


# Install VSCode Python extension if missing
code --install-extension ms-python.python --no-sandbox --user-data-dir=/root/.vscode || true

sleep 7
echo ">>> Starting broker..."
bash "$SCRIPT_DIR/broker.sh" >> "$LOG_DIR/broker.log" 2>&1 &

sleep 4
echo ">>> Starting platform server..."
bash "$SCRIPT_DIR/platform.sh" >> "$LOG_DIR/pl.log" 2>&1

