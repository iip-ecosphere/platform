#!/bin/bash
  
# turn on bash's job control
set -m
./amqp.sh &
./platform.sh &
./ecs.sh &
./serviceMgr.sh
fg %1
