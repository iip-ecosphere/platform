#!/bin/bash
  
# turn on bash's job control
set -m
echo ">>> Starting broker..."
./broker.sh &
echo ">>> Starting platform server..."
echo ">>> Starting local ECS-Runtime/ServiceManager..."
./ecsServiceMgr8.sh &
while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done

#fg %1
