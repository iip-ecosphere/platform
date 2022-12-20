#!/bin/bash

cd /root/platform
while [ ! -f /run/iip-ecsRuntime.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting local Service Manager...\n"
bash serviceMgr.sh --iip.id=fullPlatform  >> logs/serviceMgr.log
