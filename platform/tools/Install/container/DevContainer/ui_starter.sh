#!/bin/bash

cd /root/platform
while [ ! -f /run/iip-monitoring.pid ]
do
  sleep 1;
done
printf "\n\n>>> Starting UI...\n"
bash mgtUi.sh  >> logs/ui.log
