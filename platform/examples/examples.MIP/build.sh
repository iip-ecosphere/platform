#!/bin/bash

#Explanation see README.MD

rm -rf gen

#build with broker

mvn -U install -Dunpack.force=true

#execution not possible without MIP sensor or massive mocking

