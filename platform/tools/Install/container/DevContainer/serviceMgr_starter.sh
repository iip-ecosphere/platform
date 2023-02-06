#!/bin/bash

cd /root/platform
sleep 20;
printf "\n\n>>> Starting local Service Manager...\n"
bash serviceMgr.sh --iip.id=fullPlatform  >> logs/serviceMgr.log
