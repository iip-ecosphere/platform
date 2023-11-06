# IIP-Ecosphere platform examples: MIP example

Demonstrates a simple application-specific to test MIP MQTT connector to read data from magnetic scan sensor. 

The application consists of two (micro)-services and one MIP MQTT connector, which are composed in the configuration model and integrated in a model-based fashion through the platform/application instantiation:
  * MIP MQTT connector read the data from MIP sensor software through MQTT broker.
  * Asynchronous  Python service process the data from the MIP MQTT connector and pass it.
  * An asynchronous receiver service emits the received data on the console.

An explaining overview slide is available [here](docs/Examples_MIP.pdf)

This example consists of several pieces:
  * An IVML configurations for the application in `src/main/easy/`.
  * An implementation of the Java services used in the application in `src/main/java`
  * `src/main/python/MipAiPythonService.py` the "AI" service realized in Python. Executing this example requires ***Python installed in your path***. As example, the service states a system dependency to Python 3.9, the minimum required version for the IIP-Ecosphere service environment. No further framework, e.g., numpy or tensorflow is required here (this will be subject to automatic container creation).
  * A Maven assembly descriptor `src/main/assembly/python.xml` for packaging the Python service code into a ZIP (to be deployed, basis for the automated integration).
  * Two Maven profiles, one for obtaining the configuration meta-model / performing the instantiation as well as one for the application itself (executes the assembly descriptor). 
      
Regarding Python code, we make the assumption that the module of the Python Service Environment `iip` and the generated modules `datatypes`, `interfaces`, `serializers` and `services` are visible to Python within the same folder (physically or virtually).
  
As stated above, directly after obtaining this project, the application will not run and even show compile errors. This is due to the fact that generated parts and even the configuration meta model are missing. We will add them through the following steps (as explained in more details in the Platform Handbook). As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place:

  * Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))
  * Ensure that the MQTT broker address is correct in `src\test\easy\AllServicesPartMip.ivml`
  * Ensure that the MQTT broker authentication is correct in `src/test/resources/identityStore.yml`
  * Execute `mvn -U install` This will perform the broker-instantiation, the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
  * To update/upgrade the model, call `mvn -U generate-sources -Dunpack.force=true`.

If you want to execute the example in a platform installation, add `gen/py/SimpleMipApp/target/SimpleMipApp-0.1.0-SNAPSHOT-bin.jar` to the devices and execute the application (Platform CLI, deployment script, etc. see Platform Handbook for details). If you want to execute the application standalone without platform:

  * Start the MQTT broker (You should have running MQTT broker, you might use the following command to run MQTT hivemq docker container `docker run --rm --name hivemq4 -d -p 8080:8080 -p 1883:1883 hivemq/hivemq4`)
  * Start the broker (in an own shell, in Linux call `broker.sh`, in Windows `broker.bat` in `gen/broker`)
  * Execute `mvn -P App exec:java` which executes the example via a starter class. This starter class is required to run the example (micro-)service based application standalone in one JVM on the actual computer. This requires some additional code to prepare a setup as the platform would do, e.g., unpack the Python service code and the IIP-Ecosphere Python service environment, set the communication ports, switch the services into running state, etc. Most of the code is part of the Spring Cloud Stream manager extension of the platform (as this code depends on Spring related assumptions, we break here the platform architecture rule to not include extension components - this is just for running the example standalone, not for implementing the services). Ultimately, the application shall emit tuples of values received by the Fake Python "AI" service and the receiver service.

## Python

Service implementations must follow some rules to be taken up by the service environment:
  * Services are implemented in the ** module "services" **. Well, the name may be changed but this requires changes to the integration process so that a specific command line argument is present when the service environment comes up.
  * Services ** inherit from their generated base classes **, which already provides some basic implementation, e.g., asynchronous ingestion of data.
  * ** Asynchronous ** services use the attached ** ingestor ** (at any time, also no result is possible), ** synchronous services ** directly ** return the result ** (always).
  * Service constructors ** call the parent constructor ** so that metadata and registration with the service environment can happen.
  * Services ** create an instance of themselves (last line) ** to cause the registration with the service environment.
  * The python service code is ** packaged into a ZIP ** file and deployed for integration into a Maven repository. The ZIP shall only contain the services and additional code required to realize the services. Neither the generated code nor the IIP-Ecosphere Python Service environment shall be contained, as the automated integration will compose the full Python code by contributing the latter two parts from the repository. To create the ZIP, we use an assembly descriptor, which is executed during `mvn install`.
  * And, as usual, ** do not modify generated code **.

## Required Updates

See [Platform configuration](../../configuration/configuration) for details on the state of the generation and the required version of EASy-Producer (at least from the day of the last commit of this example). If the configuration meta model shall be updated, add `-Dunpack.force=true`.

## Legacy build approach

The following build steps are still there and replaced by the single build step `mvn install`. These lecacy build steps may be removed in future revisions.
  
  * Obtain the actual platform configuration meta-model and the IIP-Ecosphere Python service environment, which is intentionally not included here: `mvn -P EasyGen generate-sources`
  * Instantiate the application. This creates the interfaces, the generic implementation of the services and data classes as well as the Spring Cloud Stream services, but it does not bind the service implementation against the application (not compilable so far, please note the `generateAppsNoDeps` argument): `mvn -P EasyGen exec:java@generateAppsNoDeps`
  * If you try the example from an IDE, please perform a Maven project refresh. In extreme cases, for the first run, you may even have to restart your IDE here.
  * Compile the project with `mvn -P App install -DskipTests`. This makes the service implementations for source and receiver available to the instantiation.
  * Re-instantiate the application as done above. This step binds the service implementation provided by this project to the application (please note the `generateApps` argument): `mvn -P EasyGen exec:java@generateApps`
  * For execution, instantiate a communication broker: `mvn -P EasyGen exec:java@generateBroker`

In case you have to change the resources folder, e.g., to utilize licensed resources, you should add `-Diip.resources="NewFolderName"` to the commands (default value is `resources`). For example `mvn -P EasyGen exec:java@generateApps -Diip.resources="NewFolderName"`.
