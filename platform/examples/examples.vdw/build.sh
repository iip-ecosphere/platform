#!/bin/bash

#Explanation see README.MD

rm -rf gen

#build with broker

mavenOpts=""
if [ -f $HOME/easy-maven-settings.xml ]; then
   mavenOpts="-s $HOME/easy-maven-settings.xml"
fi
mvn $mavenOpts install -Dunpack.force=true
#ant -f build-jk.xml

#execute and 'test'

rm -f log
mvn -P App exec:java@generatedConnector -Dexec.args="--skip" > log 
echo "WARNING: Jenkins cannot contact UMATI OPCUA service. Disabling execution!"
grep -Fq "UMATI OPCUA Connector test" log