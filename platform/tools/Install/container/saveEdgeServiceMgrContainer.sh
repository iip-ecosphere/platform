#!/bin/bash
docker save iip/edgeservicemgr:0.3 | gzip > EdgeEcsRuntime/edgeservicemgr-0.3.tar.gz
