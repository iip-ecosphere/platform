# IIP-Ecosphere platform examples: RTSA

Utilizes the RTSA platform service in a simple service chain. If compile errors show up, e.g., in Eclipse, this means that the generated code is not yet in place. Run the instantiation/connector generation as described below (generated classes are intentionally not in github, generated example components are intentionally not deployed to Maven).

The application consists of three (micro)-services, which are composed in the configuration model and integrated in a model-based fashion through the platform/application instantiation:
  * A sender service asynchronously ingests data once per second.
  * An AI service based on the configured RTSA platform service processes the data and delivers a "classification".
  * A receiver service emits the finally received (and classified/scored) data on the console.

This example consists of several pieces:
  * An IVML configuration for the application in `src/test/easy/ExampleRTSA.ivml`.
  * An implementation of the Java services used in the application in `src/main/java`
  * A fake implementation of RapidMiner RTSA in `resources`. If you have an RTSA at hands, just overwrite the packaged RTSA as well as the deployment with respective files in `resources`. Please note that the **original RTSA** requires **exactly JDK 8** while the **fake RTSA** runs with **JDK 8 and newer**.
  * A specific starter class for the example `src/main/java` so that the example can run even without a running platform. Please note that the starter class is not part of an usual service implementation.
  * `pom.xml` for the application itself. 
  
As stated above, directly after obtaining this project, the application will not run and even show compile errors. This is due to the fact that generated parts and even the configuration meta model are missing. We will add them through the following steps (as explained in more details in the Platform Handbook). As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place (see also `build.sh`):

  * Ensure that the Maven platformDependencies are installed (see [install](https://github.com/iip-ecosphere/platform/tree/main/platform/tools/Install))
  * Obtain the actual platform configuration meta-model, which is intentionally not included here: `mvn -P EasyGen generate-sources`.
  * Instantiate the application. This creates the interfaces, the generic implementation of the services and data classes as well as the Spring Cloud Stream services, but it does not bind the service implementation against the application (not compilable so far, please note the `generateAppsNoDeps` argument): `mvn -P EasyGen exec:java@generateAppsNoDeps`
  * If you try the example from an IDE, please perform a Maven project refresh. In extreme cases, for the first run, you may even have to restart your IDE here.
  * Compile the project with `mvn -P Example install -DskipTests`. This makes the service implementations for source and receiver available to the instantiation.
  * Re-instantiate the application as done above. This step binds the service implementation provided by this project to the application (please note the `generateApps` argument): `mvn -P EasyGen exec:java@generateApps`
    
If you want to execute the example in a platform installation, add `gen/rtsa/SimpleRTSADemoFlowApp/target/SimpleRTSADemoFlowApp-0.1.0-SNAPSHOT-bin.jar` to the devices and execute the application (Platform CLI, deployment script, etc. see Platform Handbook for details). If you want to execute the application standalone without platform:
    
  * Instantiate a communication broker: `mvn -P EasyGen exec:java@generateBroker`
  * Start the broker (in an own shell, in Linux call `broker.sh`, in Windows `broker.bat` in `gen/broker`)
  * Execute `mvn -P Example exec:java` which runs a customized starter included in this project (`Starter.java`). This starter class is required to run the example (micro-)service based application standalone in one JVM on the actual computer. This requires some additional code to prepare a setup as the platform would do, e.g., unpack RTSA or any other binary service, set the communication ports, switch the services into running state, etc. Most of the code is part of the Spring Cloud Stream manager extension of the platform (as this code depends on Spring related assumptions, we break here the platform architecture rule to not include extension components - this is just for running the example standalone, not for implementing the services). Ultimately, the application shall emit tuples of values received by the Fake RTSA and the receiver service.

Hint: Without advanced service control of the platform, it may be the case that when the (fake) RTSA takes longer to start, the first data items cannot be processed and a `Processing failed: Connection refused: connect` message occurs. And, as usual, ** do not modify generated code **.

## Required Updates

See [Platform configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration) for details on the state of the generation and the required version of EASy-Producer (at least from the day of the last commit of this example). 

## Desirable

Explaining slides, may be a video.
