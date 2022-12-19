#!/bin/bash

# turn on bash's job control
sleep 5;
set -m
mkdir /root/platform/logs
cd /root/platform
echo ">>> Starting broker..."
bash broker.sh >> logs/broker.log &
sleep 4;
printf "\n\n>>> Starting platform server...\n"
bash platform.sh >> logs/pl.log &
while [ ! -f /run/iip-platform.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting local ECS-Runtime...\n"
bash ecs.sh --iip.id=fullPlatform >> logs/ecs.log &
while [ ! -f /run/iip-ecsRuntime.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting local Service Manager...\n"
bash serviceMgr.sh --iip.id=fullPlatform  >> logs/serviceMgr.log &
while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting Monitoring...\n"
bash monitoring.sh  >> logs/monitoring.log &
while [ ! -f /run/iip-monitoring.pid ]
do
  sleep 1;
done
printf "\n\n>>> Starting UI...\n"
bash mgtUi.sh  >> logs/ui.log

fg %1

