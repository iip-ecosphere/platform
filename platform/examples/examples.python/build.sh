#!/bin/bash

#Explanation see README.MD

#build with broker

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateBroker"
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateAppsNoDeps"
mvn -U install -DskipTests
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateApps"

#execute and test

read lower_port upper_port < /proc/sys/net/ipv4/ip_local_port_range
brokerPort=8883
while :; do
    for (( port = lower_port ; port <= upper_port ; port++ )); do
        if timeout 5 bash -c '</dev/tcp/localhost/$port &>/dev/null'
        then
            brokerPort = $brokerPort
            break
        fi
    done
done
#sh gen/py/broker/broker.sh $brokerPort
#mvn exec:java -Dexec.args="--iip.test.stop=10000 --iip.test.brokerPort=$brokerPort" > log
#grep -Fxq "RECEIVED" log
