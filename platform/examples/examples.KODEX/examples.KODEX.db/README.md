# IIP-Ecosphere platform examples: In-Memory H2 Database

This example aims at running the In-Memory H2 Database as part of KODEX example in an IIP-Ecosphere pipeliner. 
  
To run the In-Memory H2 Database, you may add the argument `-U` to update snapshots if parts are already in place:

  * The database by default has a port number (8080) on localhost, to change it add (server.port="DBPORT") in src/main/resources/application.properties
  * Compile the project with `mvn install`.
  * Run the In-Memory H2 Database Server by `mvn -P App exec:java`.
  * The database server by default has a port number (9595) on localhost, to change it pass `-Dexec.args="DBSERVERPORT"` with the previous command (to be `mvn -P App exec:java -Dexec.args="DBSERVERPORT"`).

## Required Updates

See [Platform configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration) for details on the state of the generation and the required version of EASy-Producer (at least from the day of the last commit of this example). 

## Desirable

Explaining slides, may be a video.
