#!/bin/bash

# first argument must be file name

#java -cp target/*:target/mdzhJars/* test.de.iip_ecosphere.platform.examples.hm22.RunCarsAas 9998 192.168.2.1
mvn -P App exec:java@mdzh-server -Dexec.args="9998 192.168.2.1"