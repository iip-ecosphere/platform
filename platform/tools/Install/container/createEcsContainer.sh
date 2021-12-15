#!/bin/bash
#we store the image-info.yml for simplicity in the container -> file-URI
cp SimpleMeshTestingApp/image-info.yml ../gen/ecsRuntime.image-info.yml
docker build -f EcsRuntime/Dockerfile -t iip/ecsruntime:0.3 ../gen
