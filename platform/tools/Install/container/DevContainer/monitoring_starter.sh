#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/root/platform/logs"
mkdir -p "$LOG_DIR"

while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting Monitoring...\n"
bash "$SCRIPT_DIR/monitoring.sh"  >> "$LOG_DIR/monitoring.log"
