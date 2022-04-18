#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="VDW src/test/easy gen/vdw generateApps"
mvn compile

#execute and test

if nc opcua.umati.app 4840 < /dev/null; then
   echo "Testing with UMATI"
   mvn exec:java > log 
   grep -Fq "deviceCls: ProfilingMachine" log
else
   echo "UMATI not reachable"
fi