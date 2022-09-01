#!/bin/bash
docker run --rm --expose 9020 --env iip.port=9020 --env QPID_WORK=/var/qpidwork --name robot_container -e ROBOT_IP="192.168.2.21" -v ${PWD}/:/home/robot -v /iip/actual:/home/iip/actual -v ${PWD}/gen/hm22/DemonstrationAppHM22/target/:/services --network host --user root -dit robot:latest
#docker run --rm --name robot_container -e ROBOT_IP="192.168.2.21" -v ${PWD}/:/home/robot --network host --user root -dit robot:latest &
