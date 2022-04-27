# IIP-Ecosphere platform examples: KODEX

This example consists of several pieces:
  * An IVML configuration for the application in `src/test/easy/ExampleKODEX.ivml`.
  * An implementation of the Java services used in the application in `src/main/java`
  * Two Maven files, `pom-model.xml` for obtaining the configuration meta-model and for performing the instantiation as well as `pom.xml` for the application itself. 
  
As stated above, directly after obtaining this project, the application will not run and even show compile errors. This is due to the fact that generated parts and even the configuration meta model are missing. We will add them through the following steps (as explained in more details in the Platform Handbook). As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place (see also `build.sh`):

  * Ensure that the Maven platformDependencies are installed (see [install](https://github.com/iip-ecosphere/platform/tree/main/platform/tools/Install))
  * Obtain the actual platform configuration meta-model, which is intentionally not included here: `mvn -f pom-model.xml generate-sources`.
  * Instantiate the application. This creates the interfaces, the generic implementation of the services and data classes as well as the Spring Cloud Stream services, but it does not bind the service implementation against the application (not compilable so far, please note the `generateAppsNoDeps` argument).
    * Windows: `mvn -f pom-model.xml exec:java -Dexec.args="ExampleKODEX src/test/easy gen/KODEX generateAppsNoDeps"`
    * Linux/MacOS: `mvn -f pom-model.xml exec:java -Dexec.args="ExampleKODEX src/test/easy gen/KODEX generateAppsNoDeps"`
  * If you try the example from an IDE, please perform a Maven project refresh.
  * Compile the project with `mvn install -DskipTests`. This makes the service implementations for source and receiver available to the instantiation.
  * Re-instantiate the application as done above. This step binds the service implementation provided by this project to the application (please note the `generateApps` argument).
    * Windows: `mvn -f pom-model.xml exec:java -Dexec.args="ExampleKODEX src/test/easy gen/KODEX generateApps"`
    * Linux/MacOS: `mvn -f pom-model.xml exec:java -Dexec.args="ExampleKODEX src/test/easy gen/KODEX generateApps"`
    
If you want to execute the example in a platform installation, add `gen/KODEX/SimpleKodexTestingApp/target/SimpleKodexTestingApp-0.1.0-SNAPSHOT-bin.jar` to the devices and execute the application (Platform CLI, deployment script, etc. see Platform Handbook for details).
    

## Required Updates

See [Platform configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration) for details on the state of the generation and the required version of EASy-Producer (at least from the day of the last commit of this example). 

## Desirable

Explaining slides, may be a video.
