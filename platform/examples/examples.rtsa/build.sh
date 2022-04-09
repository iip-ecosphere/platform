#!/bin/sh

#Explanation see README.MD

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="ExampleRTSA src/test/easy gen/rtsa generateBroker"
mvn -f pom-model.xml exec:java -Dexec.args="ExampleRTSA src/test/easy gen/rtsa generateAppsNoDeps" -Diip.resources="$PWD/resources"
mvn -U install -DskipTests
mvn -f pom-model.xml exec:java -Dexec.args="ExampleRTSA src/test/easy gen/rtsa generateApps" -Diip.resources="$PWD/resources"
#mvn exec:java
