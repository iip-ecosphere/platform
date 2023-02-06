#!/bin/bash

cd /root/platform
sleep 15;
printf "\n\n>>> Starting local Service Manager...\n"
bash serviceMgr.sh --iip.id=fullPlatform  >> logs/serviceMgr.log
