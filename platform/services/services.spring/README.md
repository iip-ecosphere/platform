# IIP-Ecosphere platform: Service management for Spring Cloud Stream

Alternative service management implementation for services running with [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream). Services are given as JAR file. A JAR file may contain multiple services as well as network-coupled service implementations in other programming languages. The services are defined as usual in Spring Clout Stream as Java Bean methods returning Supplier, Function or Consumer. The linking between the services and their binding properties are defined in an `application.yml`. 

## Artifact Deployment Descriptor

In addition, service deployment properties are given in a separate descriptor, the `deployment.yml` with the following structure. Unless stated explicitly, fields are mandatory.
* Artifact: Unique identifier `id` and `name` of the artifact. An artifact definition encounters the contained `services`. An artifact may declare `types`, i.e., types used by the services for passing data. Java types (in particular primitives, `String` without qualification) and types known to the platform must not be declared and can be referenced by their qualified name. Service specific types must be declared, in particular as record/class like structures with their declared qualified `name` and their contained fields, each consisting of a `name` and a `type`.
* Service: Unique identifier `id`, `name`, `version` (numbers separated by dots) and an optional `description` of the service (default is empty). Optional specification whether the service is `deployable` in distributed fashion (default is `true`) and the service kind (see `de.iip_ecosphere.platform.services.ServiceKind`). A service may contain further sub-structures:
* `cmdArg`: Optional list of command line arguments to be passed when the service is started. 
* `instances`: The optional number of service replicas to be launched. Ignored if negative. Default value is `1`.
* resource desires: Optional values for the number of `cpus`, the `memory` and the `disk` space, the latter in bytes. If not given or not positive, internal default values are used, i.e., one CPU and, e.g., automatically determined for a JVM. The deployer may ignore these values if not applicable to the type of process to be started.
* `parameters`: Parameters of the service that can be modified at runtime, each with `name`, optional `description` and qualified Java `type` name either pointing to a Java (primitive) type, a platform type or a type declared in the `types` section of the containing artifact.
* `ensembleWith`: Optional service id of the service starting this service, i.e., the id of the ensemble leader. The id of the ensemble leader must be defined in the same artifact where it is used, i.e., it must be local. The deployer will start this service together with the mentioned service in one process, but not this service as individual process.
* `relations`: Optional connection informations for individual channels/topics to connect the service to other local services or to external services via brokers. A relation may have an `id` which identifies relations on the same logical channel. Via `id`, relations can implicitly define service dependencies, i.e., outgoing relations must be fulfilled before the declaring service may start. The `channel` is the technical name identifying the relation on the service implementation, i.e., it is implementation dependent (see `application.yml`). If no `channel` is given, the settings of this relations apply to all local connections within the resource the service is deployed to. `endpoint` defines the port and host command line arguments defining the broker for the respective channel/topic. Within these arguments, `${port}` and `${host}` are substituted by actual values, respectively. `hostArg` is optional - if not given, localhost is used. A relation may have an  optional `description`, a qualified Java `type` name either pointing to a Java (primitive) type, a platform type or a type declared in the `types` section of the containing artifact and a `direction` (either `IN` or `OUT`). As default relation with empty channel declares the endpoint information for all internal process local relations, it must neither have a description, a direction or a type. All internal relations must be declared but must not have an endpoint (as they indicate direction and data type). All external relations must provide all data. If `ensembleWith` takes priority over implicit service dependencies stated by `relations`.
* `process`: Optional structure indicating that the service is not directly implemented in Java rather than in terms of a further process, e.g., in a different programming language. `path` denotes the relative path within the containing artifact where the executable/code is located. `path` and all contained files and folders will be extracted to a local directory, which is set as home directory for the process to be launched via `cmdArg`, a string list of a command line program and its arguments. `streamEndpoint` and `aasEndpoint` denote (akin to `endpoint` for `process` above) communication settings to be appended to the command line arguments, here for the data streaming communication (i.e., the service will delegate the data there and receive input from there) and AAS command communication (typically via VAB). The Boolean property `started` indicates whether the underlying
process is already started (default is `false`), e.g., in case of a database.

    id: <String
    name: <String>
    types:
      - name: <QString>
        fields:
          -name: <String>
          -type: <QString>
    services:
      - id: <String>
        name: <String>
        version: <VersionString>
        description: <String>
        deployable: <Boolean>
        kind: <value from de.iip_ecosphere.platform.services.ServiceKind>
        cmdArg: 
          - <String>
        instances: <Integer>
        cpus: <Integer>
        memory: <Integer>
        disk: <Integer>
        parameters:
          - name: <String>
            description: <String>
            type: <QString>
        ensembleWith:
          - <String>
        relations:
          - channel: <String>
            id: <String>
            description: <String>
            endpoint:
              portArg: <String, ${port} is substituted by the actual port number>
              hostArg: <String, ${host} is substituted by the actual host name>
            type: <QString>
            direction: <IN|OUT>
        process:
          path: <String>
          cmdArg:
            - <String>
          streamEndpoint:
            portArg: <String, ${port} is substituted by the actual port number>
            hostArg: <String, ${host} is substituted by the actual host name>
          aasEndpoint:
            portArg: <String, ${port} is substituted by the actual port number>
            hostArg: <String, ${host} is substituted by the actual host name>
          started: false

## Service Manager Configuration

Also the service manager itself can be configured via the ``application.yml``, i.e., with this extension Spring execution concepts are/must be added to the containing component.

Currently, the following properties can be configured:
* `service-mgr` is the IIP-Ecosphere service manager. Service operations such as starting or stopping may not be executed immediately, e.g., as they have to wait for starting up of services or JVMs. The `waitingTime` limits this time and causes called operations to failed if the given time is exceeded (default `30000`). `brokerHost` and `brokerPort` define the communication setup for the locally installed messaging service/broker, e.g., a MQTT broker. `deleteArtifacts` allows the service manager to delete downloaded artifacts.
* `cloud.deployer.local` refers to the underlying mechanism of Spring Cloud Stream. Service artifacts and their working directory may be temporary if not configured or in a given folder. These files may be deleted automatically on exit or remain in the folder. Both settings are helpful for debugging.

    service-mgr:
      waitingTime: <Integer>
      brokerHost: <String>
      brokerPort: <Integer>
      deleteArtifacts: <Boolean>
    cloud:
      deployer:
        local:
          workingDirectoriesRoot: <Folder>
          deleteFilesOnExit: <Boolean>
      
## Missing
* Handling of dependent services
* Handling of implementation processes, i.e., external implementations of services running as processes
* Advanced service operations: update, migrate, switch, clone
* Service monitoring (including implementation processes)