#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="VDW src/test/easy gen/vdw generateApps"
mvn compile

#execute and 'test'

mvn exec:java -Dexec.args="--skip" > log 
grep -Fq "UMATI OPCUA Connector test" log