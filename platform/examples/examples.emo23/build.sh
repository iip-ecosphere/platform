#!/bin/bash

#Explanation see README.MD

rm -rf gen

#build with broker

mvn -U install -Dunpack.force=true -DdisablePythonTests=true

#execute and test

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
#echo "Broker PID $pidBroker"
#mvn -P App exec:java@app -Diip.springStart.args="--iip.test.stop=180000 --iip.test.brokerPort=$brokerPort -Diip.app.hm22.mock.callRobot=false" > log &
#pidTest=$!
#echo "Test started $pidTest"
#
#sleep 180 && pkill -9 -P "$pidTest" && kill -9 "$pidTest"
#pkill -9 -P "$pidBroker" && kill -9 "$pidBroker"
#
#echo "Testing for APP: DecisionResultImpl in log"
#
#grep -Fq "APP: DecisionResultImpl" log