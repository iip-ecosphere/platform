#!/bin/bash
docker run --network=host --expose 9002 -e “IIP_PORT=9002” iip/simplemesh:0.1