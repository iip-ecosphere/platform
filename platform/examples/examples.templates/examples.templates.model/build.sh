#!/bin/bash
#Explanation see README.MD
rm -rf gen
dir=$PWD
#build until interfaces (obtain model, generateAppsNoDeps)
mvn -U generate-sources
#build/deploy service implementation
cd ../examples.templates.impl
mvn install
#return and build full application
cd $dir
mvn install

#execute and test

brokerPort=8883
read LOWERPORT UPPERPORT < /proc/sys/net/ipv4/ip_local_port_range
while :
do
        brokerPort="`shuf -i $LOWERPORT-$UPPERPORT -n 1`"
        ss -lpn | grep -q ":$PORT " || break
done
echo "Using Broker Port: $brokerPort"


cd gen/broker/broker
./broker.sh $brokerPort &
pidBroker=$!
cd $dir

echo "Broker PID $pidBroker"
cd ../examples.templates.impl
mvn -P App exec:java@App -Diip.springStart.args="--iip.test.stop=30000 --iip.test.brokerPort=$brokerPort" > log &
pidTest=$!
echo "Test started $pidTest"

sleep 30 && pkill -9 -P "$pidTest" && kill -9 "$pidTest"
pkill -9 -P "$pidBroker" && kill -9 "$pidBroker"

echo "Testing for Output: in log"

grep -Fq "Output:" log
