#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/broker generateBroker"
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateAppsNoDeps"
mvn -U install -DskipTests
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateApps"

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
mvn exec:java -Dexec.args="--iip.test.stop=30000 --iip.test.brokerPort=$brokerPort" > log &
pidTest=$!
echo "Test started $pidTest"

sleep 30 && pkill -P "$pidTest" && kill "$pidTest"
pkill -P "$pidBroker" && kill "$pidBroker"

echo "Testing for RECEIVED in log"

grep -Fq "RECEIVED" log

