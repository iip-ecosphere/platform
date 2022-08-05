#!/bin/bash

#Explanation see README.MD

rm -rf gen

#build with broker

mvn -P EasyGen generate-sources
mvn -P EasyGen exec:java@generateApps
mvn compile

#execute and 'test'

rm -f log
mvn -P App exec:java@generatedConnector -Dexec.args="--skip" > log 
echo "WARNING: Jenkins cannot contact UMATI OPCUA service. Disabling execution!"
grep -Fq "UMATI OPCUA Connector test" log