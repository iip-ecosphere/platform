#!/bin/bash

sleep 7;
mkdir /root/platform/logs
cd /root/platform
echo ">>> Starting broker..."
bash broker.sh >> logs/broker.log &
sleep 4;
printf "\n\n>>> Starting platform server...\n"
bash platform.sh >> logs/pl.log
