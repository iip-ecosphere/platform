#!/bin/bash

#Explanation see README.MD

rm -rf gen

#build with broker

mavenOpts=""
if [ -f $HOME/easy-maven-settings.xml ]; then
   mavenOpts="-s $HOME/easy-maven-settings.xml"
fi
mvn $mavenOpts install -Dunpack.force=true
#ant -f build-jk.xml

#execution not possible without MIP sensor or massive mocking

