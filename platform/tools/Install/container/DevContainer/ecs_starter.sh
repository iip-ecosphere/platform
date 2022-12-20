#!/bin/bash

cd /root/platform
while [ ! -f /run/iip-platform.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting local ECS-Runtime...\n"
bash ecs.sh --iip.id=fullPlatform >> logs/ecs.log
