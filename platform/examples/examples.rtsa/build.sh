#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -P EasyGen generate-sources
mvn -P EasyGen exec:java@generateBroker
mvn -P EasyGen exec:java@generateAppsNoDeps
mvn -U -P Example install -DskipTests
mvn -P EasyGen exec:java@generateApps

#execute and test

brokerPort=8883
read LOWERPORT UPPERPORT < /proc/sys/net/ipv4/ip_local_port_range
while :
do
        brokerPort="`shuf -i $LOWERPORT-$UPPERPORT -n 1`"
        ss -lpn | grep -q ":$PORT " || break
done
echo "Using Broker Port: $brokerPort"

dir=$PWD
cd gen/broker/broker
./broker.sh $brokerPort &
pidBroker=$!
cd $dir

echo "Broker PID $pidBroker"
mvn -P Example exec:java -Dexec.args="--iip.test.stop=30000 --iip.test.brokerPort=$brokerPort" > log &
pidTest=$!
echo "Test started $pidTest"

sleep 30 && pkill -9 -P "$pidTest" && kill -9 "$pidTest"
pkill -9 -P "$pidBroker" && kill -9 "$pidBroker"

echo "Testing for RECEIVED in log"

grep -Fq "RECEIVED" log
