#!/bin/bash
docker run -v /var/run/docker.sock:/var/run/docker.sock -P --network=host --mount type=bind,source="$(pwd)"/SimpleMeshTestingApp,target=/SimpleMeshTestingApp -it iip/ecsruntime:0.2
