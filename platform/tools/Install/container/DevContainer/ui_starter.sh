#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/root/platform/logs"
mkdir -p "$LOG_DIR"

while [ ! -f /run/iip-monitoring.pid ]
do
  sleep 1;
done
printf "\n\n>>> Starting UI...\n"
bash "$SCRIPT_DIR/mgtUi.sh"  >> "$LOG_DIR/ui.log"
