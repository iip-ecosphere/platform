#!/bin/bash
docker run --name plc_container --network host -v ${PWD}/services/:/services -v ${PWD}/resor/:/iip/actual/gen -itd plc
