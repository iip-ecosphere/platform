#!/bin/bash
  
# turn on bash's job control
set -m
echo ">>> Starting AMQP broker..."
./amqp.sh &
echo ">>> Starting local Service Manager..."
./serviceMgr.sh &
while [ ! -f /run/iip-serviceMgr.pid ]
do
  sleep 1
done

fg %1
