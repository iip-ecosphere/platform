#!/bin/bash
set -e

# Resolve script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Log directory (create if missing)
LOG_DIR="/root/platform/logs"
mkdir -p "$LOG_DIR"

sleep 20
echo ">>> Starting local ECS-Runtime..."

# Run ECS with absolute paths and optional arguments
exec bash "$SCRIPT_DIR/ecs.sh" --iip.id=fullPlatform >> "$LOG_DIR/ecs.log" 2>&1

