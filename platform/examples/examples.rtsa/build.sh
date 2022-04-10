#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="ExampleRTSA src/test/easy gen/broker generateBroker"
mvn -f pom-model.xml exec:java -Dexec.args="ExampleRTSA src/test/easy gen/rtsa generateAppsNoDeps" -Diip.resources="$PWD/resources"
mvn -U install -DskipTests
mvn -f pom-model.xml exec:java -Dexec.args="ExampleRTSA src/test/easy gen/rtsa generateApps" -Diip.resources="$PWD/resources"

#execute

#brokerPort=8883
#read LOWERPORT UPPERPORT < /proc/sys/net/ipv4/ip_local_port_range
#while :
#do
#        brokerPort="`shuf -i $LOWERPORT-$UPPERPORT -n 1`"
#        ss -lpn | grep -q ":$PORT " || break
#done
#echo "Using Broker Port: $brokerPort"
#
#dir=$PWD
#cd gen/broker/broker
#./broker.sh $brokerPort &
#pidBroker=$!
#cd $dir
#
#mvn exec:java -Dexec.args="--iip.test.stop=10000 --iip.test.brokerPort=$brokerPort" > log &
#pidTest=$!
#
#sleep 60 && kill "$pidTest"
#kill "$pidBroker"
#
#grep -Fxq "RECEIVED" log
