#!/bin/bash
cp -r ../broker/src ../gen
cp ../broker/broker.sh ../gen
chmod u+x ../gen/broker.sh
cp -r ../broker/brokerJars ../gen
cp EdgeServiceMgr/wrapper_script.sh ../gen/edgeServiceMgr.wrapper_script.sh
chmod u+x ../gen/edgeServiceMgr.wrapper_script.sh
docker build -f EdgeServiceMgr/Dockerfile -t iip/edgeservicemgr:0.3 ../gen
