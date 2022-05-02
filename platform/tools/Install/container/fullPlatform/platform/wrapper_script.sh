#!/bin/bash
  
# turn on bash's job control
set -m
echo ">>> Starting broker..."
./broker/broker.sh &
echo ">>> Starting platform server..."
./platform.sh &
while [ ! -f /run/iip-platform.pid ]
do
  sleep 1
done
echo ">>> Starting local ECS-Runtime..."
./ecs.sh &
while [ ! -f /run/iip-ecsRuntime.pid ]
do
  sleep 1
done
echo ">>> Starting local Service Manager..."
./serviceMgr.sh
while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done

fg %1
