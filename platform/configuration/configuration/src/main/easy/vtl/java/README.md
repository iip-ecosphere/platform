# IIP-Ecosphere platform: Variability Instantiation Templates for Java

The Java templates create interfaces, serializers and glue code for the integration with the service execution engine (default: Spring Cloud Stream). Moreover, the templates create the instantiated Java platform components, e.g., the Service Manager, the ECS-Runtime, the combined ECS-Runtime/Service Manager.

* Generic, reusable templates
    * `JavaBasics` : Basic functions for names, e.g., how to turn an arbitrary name into a Java identifier.
    * `JavaMapping`: Mapping of types and components to Java types and Maven artifact names.
    * `DataOperationBasics`: Generic and extensible translation of operation expression trees (from `Connectors.ivml`) to Java. Used for both connector types, channel connectors against parsers/formatters and model connectors with model paths.
    * `JavaLogbackXml`: Logging setup for all components.
    * `JavaYamlTest`: Generation of generic tests whether generated Yaml files can be read at all.
    * `MeshBasics`: Template functionality to query or traverse configured application meshes.
* Plattform (server) component
    * `PlatformYaml`: Creates the YAML file for setting up the platform server component, e.g., AAS location. 
    * `PlatformMvn`: Selects the Maven artifacts for the platform server component and packages them with the files from `PlatformYaml` and `JavaLogbackXml`.
* ESC-Runtime
    * `EscRuntimeDockerContainerManagerYaml`: Creates the YAML file for setting up the ESC-Runtime component, here using Docker as container manager (alternatives may follow). 
    * `EscRuntimeMvn`: Selects the Maven artifacts for the ECS-Runtime platform component and packages them with the files from `EscRuntimeDockerContainerManagerYaml` and `JavaLogbackXml`.
* Service Manager
     * `ServiceControlSpringCloudStreamYaml`: Creates the YAML file for setting up the service manager with spring cloud stream as service execution engine (alternatives may follow).
     * `ServiceControlMvn`: Selects the Maven artifacts for the ECS-Runtime platform component and packages them with the files from `ServiceControlSpringCloudStreamYaml` and `JavaLogbackXml`.
* Combined ESC-Runtime/Service Manager
    * `EscServiceControlSpringCloudStreamYaml` : Combines the YAML setup of the ESC-Runtime and the Service Manager into a single program.
    * `EscServiceControlMvn`: Combines `ServiceControlMvn` and `EscRuntimeMvn`.
* IoT Applications (generic)
    * `JavaType`: Creates the interface/implementation for a class representing data to be processed.
    * `JavaConnector`: Creates connector code fragments to be used within service glue code executed by the service execution enginer.
    * `JavaConnectorSerializer`: Creates connector specific serializers used by `JavaConnector`. Relies on `DataOperationBasics`.
    * `JavaJsonSerializer`: Creates the JSON wire format transport mechanism.
    *  In shared platform interface mode:
        * `JavaServiceInterface`: Generates the API for developing custom services against.
        * `JavaServiceStub`: Generates a stub-implementation of `JavaServiceInterface` to link against generic protocols, e.g., against a Python service running in the Python service environment (currently unused).
    *  In non-shared platform interface mode:
        * `JavaMeshElementInterface`: Generates the API for developing custom services against.
        * `JavaMeshElementStub`: Generates a stub-implementation of `JavaServiceInterface` to link against generic protocols, e.g., against a Python service running in the Python service environment (currently unused).
    * `JavaServiceBaseImpl`: Creates an abstract default implementation of the generated service interface, which, e.g., cares for service parameterization. 
    * `JavaServices`: Creates Java service loader entries, e.g., for the Network manager or the Lifecycle Descriptors.
    * `JavaInterfaceAssembly`: Creates a Maven assembly descriptor to package the generated interfaces in order to program custom services against.
    * `AppMvn`: Creates the Maven build process for an individual application.
    * `AppAnt`: Creates an ANT build file to allow for Maven artifact deployment during Continuous Integration.
* IoT Applications (Spring Cloud Stream)
    * `SpringCloudStreamBasics`: Generic, reusable functionality for generating service glue code for spring cloud services.
    * `JavaSpringCloudStreamMeshElement`: Generates the glue code for a certain mesh element.
    * `JavaSpringCloudStreamStarter`: Generates the default starter class to be executed when a service packaged into a service artifact is started.
    * `JavaSpringCloudStreamYaml`: Generates the Spring `application.yml` as technical description to execute Spring Cloud Stream services.
    * `SpringCloudStreamDeploymentDescriptor`: Generates the YAML service artifact deployment descriptor for the Spring Cloud Stream Service manager.
    * `JavaSpringCloudStreamAssembly`: Generates a Maven assembly packaging the alternative ZIP service artifact.

The scripts may be executed multiple times, e.g., for configured data types, services, connectors or mesh elements.
