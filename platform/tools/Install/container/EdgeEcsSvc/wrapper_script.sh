#!/bin/bash
  
# turn on bash's job control
set -m
echo ">>> Starting broker..."
./broker.sh &
echo ">>> Starting local ECS-Runtime with service manager..."
./ecs8.sh &
while [ ! -f /run/iip-ecsRuntime.pid ]
do
  sleep 1
done

fg %1
