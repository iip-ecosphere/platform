#!/bin/sh

mvn -U -f pom-model.xml generate-sources
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateAppsNoDeps"
mvn -U install -DskipTests
mvn -f pom-model.xml exec:java -Dexec.args="ExamplePython src/test/easy gen/py generateApps
#mvn exec:java
