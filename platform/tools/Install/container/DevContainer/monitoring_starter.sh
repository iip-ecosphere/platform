#!/bin/bash

cd /root/platform
while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done
printf "\n\n>>> Starting Monitoring...\n"
bash monitoring.sh  >> logs/monitoring.log
