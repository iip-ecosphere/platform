#!/bin/bash

#Explanation see README.MD

cd examples.KODEX
rm -rf gen

#build with broker

#mvn -U -P EasyGen generate-sources
#mvn -P EasyGen exec:java@generateBroker
#mvn -P EasyGen exec:java@generateAppsNoDeps
#mvn -U -P App install -DskipTests
#mvn -P EasyGen exec:java@generateApps

mvn -U install

cd ..

#execute and test

cd examples.KODEX.db

DBPort=8585
DBServerPort=9595
sed -i '1 i server.port='$DBPort src/main/resources/application.properties
sleep 3
mvn install -U
mvn exec:java -Dexec.args="$DBServerPort"> DBlog &
pidDB=$!
sleep 3
echo "Using In Memory Database Port: $DBPort"
echo "Using In Memory Database ServerPort: $DBServerPort"
cd ..

cd examples.KODEX

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
mvn -P App exec:java -Dexec.args="--iip.test.stop=30000 --iip.test.brokerPort=$brokerPort" -Diip.app.db.server.port=$DBServerPort > log &
pidTest=$!
echo "Test started $pidTest"

sleep 30 && pkill -9 -P "$pidTest" && kill -9 "$pidTest"
pkill -9 -P "$pidBroker" && kill -9 "$pidBroker"

echo "Testing for RECEIVED in log"

grep -Fq "RECEIVED" log

cd ..
cd examples.KODEX.db
kill -9 "$pidDB"
sed -i '1d' src/main/resources/application.properties

