#!/bin/bash
docker run -v /var/run/docker.sock:/var/run/docker.sock -P --network=host -it iip/ecsruntime:0.2
