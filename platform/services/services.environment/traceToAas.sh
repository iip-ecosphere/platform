#!/bin/bash

# simple script to run the TraceToAas program standalone, e.g., to realize a UI for the AAS
# requirement: project is packaged with Maven so that jars are in target/jars
# command line:
#   --aasServerPort=<int> determines the port of the AAS server, 
#   --aasRegistyPort=<int> the port of the AAS registry server
#   --aasProtocolPort=<int> the port of the AAS implementation/protocol server for implementing functions
#   --aasHost=<String> the host name/IP address of the server, else localhost

java -cp target/*:target/jars/* test.de.iip_ecosphere.platform.services.environment.services.TraceToAasServiceMain $*