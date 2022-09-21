# IIP-Ecosphere platform examples: KODEX

This example aims at running the anonymizer/pseudonymizer KODEX as part of an IIP-Ecosphere pipeliner. The example consists of several pieces:
  * An IVML configuration for the application in `src/test/easy/ExampleKODEX.ivml`.
  * An implementation of the Java services used in the application in `src/main/java`
  * `pom.xml` for the application itself. 
  
As stated above, directly after obtaining this project, the application will not run and even show compile errors. This is due to the fact that generated parts and even the configuration meta model are missing. We will add them through the following steps (as explained in more details in the Platform Handbook). As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place (see also `build.sh`):

  * Ensure that the Maven platformDependencies are installed and up to date (see [install](https://github.com/iip-ecosphere/platform/tree/main/platform/tools/Install))
  * Obtain the actual platform configuration meta-model, which is intentionally not included here: `mvn -P EasyGen generate-sources`.
  * Instantiate the application. This creates the interfaces, the generic implementation of the services and data classes as well as the Spring Cloud Stream services, but it does not bind the service implementation against the application (not compilable so far, please note the `generateAppsNoDeps` argument). `mvn -P EasyGen exec:java@generateAppsNoDeps`
  * If you try the example from an IDE, please perform a Maven project refresh. In extreme cases, for the first run, you may even have to restart your IDE here.
  * Compile the project with `mvn -P App install -DskipTests`. This makes the service implementations for source and receiver available to the instantiation.
  * Re-instantiate the application as done above. This step binds the service implementation provided by this project to the application (please note the `generateApps` argument): `mvn -P EasyGen exec:java@generateApps`
    
In case you have to change the resources folder, e.g., to utilize licensed resources, you should add `-Diip.resources="NewFolderName"` to the commands (default value is `resources`). For example `mvn -P EasyGen exec:java@generateApps -Diip.resources="NewFolderName"`.

If you want to execute the example in a platform installation, add `gen/KODEX/SimpleKodexTestingApp/target/SimpleKodexTestingApp-0.1.0-SNAPSHOT-bin.jar` to the devices and execute the application (Platform CLI, deployment script, etc. see Platform Handbook for details). If you want to execute the application standalone without platform:
    
  * Instantiate a communication broker: `mvn -P EasyGen exec:java@generateBroker`
  * Start the broker (in an own shell, in Linux call `broker.sh`, in Windows `broker.bat` in `gen/broker`)
  * Execute `mvn -P App exec:java` which runs a customized starter included in this project (`Starter.java`). This starter class is required to run the example (micro-)service based application standalone in one JVM on the actual computer. This requires some additional code to prepare a setup as the platform would do, e.g., unpack the Python service code and the IIP-Ecosphere Python service environment, set the communication ports, switch the services into running state, etc. Most of the code is part of the Spring Cloud Stream manager extension of the platform (as this code depends on Spring related assumptions, we break here the platform architecture rule to not include extension components - this is just for running the example standalone, not for implementing the services). Ultimately, the application shall emit tuples of values received by the Fake Python "AI" service and the receiver service.

## Required Updates

See [Platform configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration) for details on the state of the generation and the required version of EASy-Producer (at least from the day of the last commit of this example). 

## Desirable

Explaining slides, may be a video.

An explaining overview slide is available [here](https://github.com/iip-ecosphere/platform/examples/examples.KODEX/docs/Examples_KODEX.pdf)
