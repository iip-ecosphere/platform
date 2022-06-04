#!/bin/bash
cp -r ../broker/src ../gen
cp ../broker/broker.sh ../gen
chmod u+x ../gen/broker.sh
cp -r ../broker/brokerJars ../gen
cp EdgeEcsSvc/wrapper_script.sh ../gen/edgeEcsSvc.wrapper_script.sh
chmod u+x ../gen/edgeEcsSvc.wrapper_script.sh
#we store the image-info.yml for simplicity in the container -> file-URI
cp EdgeServiceMgr/image-info.yml ../gen/edgeServiceMgr.image-info.yml
docker build -f EdgeEcsSvc/Dockerfile -t iip/edgeecssvc:0.3 ../gen
