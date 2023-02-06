#!/bin/bash

cd /root/platform
sleep 20;
printf "\n\n>>> Starting local ECS-Runtime...\n"
bash ecs.sh --iip.id=fullPlatform >> logs/ecs.log
