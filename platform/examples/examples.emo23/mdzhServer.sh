#!/bin/bash

mvn -P App exec:java@mdzh-server -Dexec.args="--port=9989 --host=192.168.2.13"