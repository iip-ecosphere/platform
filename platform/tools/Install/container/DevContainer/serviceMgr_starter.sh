#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="/root/platform/logs"
mkdir -p "$LOG_DIR"

sleep 20;
printf "\n\n>>> Starting local Service Manager...\n"
bash "$SCRIPT_DIR/serviceMgr.sh" --iip.id=fullPlatform  >> "$LOG_DIR/serviceMgr.log"
