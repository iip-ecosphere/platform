#!/bin/bash
#we store the image-info.yml for simplicity in the container -> file-URI
cp EdgeServiceMgr/image-info.yml ../gen/edgeServiceMgr.image-info.yml
docker build -f EdgeEcsSvc/Dockerfile -t iip/edgeecssvc:0.3 ../gen

