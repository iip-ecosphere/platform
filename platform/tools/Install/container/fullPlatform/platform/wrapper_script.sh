#!/bin/bash

# turn on bash's job control
set -m
echo ">>> Starting broker..."
bash broker.sh &
sleep 4;
printf "\n\n>>> Starting platform server...\n"
bash platform.sh &
while [ ! -f /run/iip-platform.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting local ECS-Runtime...\n"
./ecs.sh &
while [ ! -f /run/iip-ecsRuntime.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting local Service Manager...\n"
bash serviceMgr.sh --iip.id=fullPlatform &
while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting Monitoring...\n"
bash monitoring.sh &
while [ ! -f /run/iip-monitoring.pid ]
do
  sleep 1;
done
printf "\n\n>>> Starting UI...\n"
bash mgtUi.sh

fg %1

