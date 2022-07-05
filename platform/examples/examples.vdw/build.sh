#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="VDW src/test/easy gen/vdw generateApps"
mvn compile

#execute and 'test'

rm -f log
mvn exec:java -Dexec.args="--skip" > log 
echo "WARNING: Jenkins cannot contact UMATI OPCUA service. Disabling execution!"
grep -Fq "UMATI OPCUA Connector test" log