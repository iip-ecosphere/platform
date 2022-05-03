REM @echo off
REM simple script to run the TraceToAas program standalone, e.g., to realize a UI for the AAS
REM requirement: project is packaged with Maven so that jars are in target/jars
REM command line: 
REM  --aasServerPort=<int> determines the port of the AAS server, 
REM  --aasRegistyPort=<int> the port of the AAS registry server
REM  --aasProtocolPort=<int> the port of the AAS implementation/protocol server for implementing functions

java -cp target/*;target/jars/* test.de.iip_ecosphere.platform.services.environment.services.TraceToAasServiceMain %*