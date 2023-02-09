# IIP-Ecosphere platform: Service management for Spring Cloud Stream

Alternative service management implementation for services running with [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream). Services are given as JAR file. A JAR file may contain multiple services as well as network-coupled service implementations in other programming languages. The services are defined as usual in Spring Clout Stream as Java Bean methods returning Supplier, Function or Consumer. The linking between the services and their binding properties are defined in an `application.yml`. 

## Service Artifact Deployment Descriptor

In addition, service deployment properties are given in a separate descriptor, the `deployment.yml` with the following structure. Unless stated explicitly, fields are mandatory.
* `artifact` characterizes the artifact itself as well as global definitions of elements within the artifact: 
    * Unique identifier `id` and `name` of the artifact. 
    * `version` of the artifact as dot-separated numbers. 
    * An artifact definition enumerates the contained `services` (see below).
    * An artifact may declare `types`, i.e., types used by the services for passing data. Java types (in particular primitives, `String` without qualification) and types known to the platform must not be declared and can be referenced by their qualified name. Service specific types must be declared, in particular as record/class like structures with their declared qualified `name` and their contained fields, each consisting of a `name` and a `type`.
* `services` consisting of individual service entries, each with: 
    * Unique identifier `id` (may include application id/application instance id postfixed after @), `applicationId` the id of the application this service is part of (may be empty for legacy/the "default" application), `name`, `version` (numbers separated by dots) and an optional `description` of the service (default is empty). 
    * Optional specification whether the service is `deployable` in distributed manner (default is `true`).
    Optional specification whether the service is `topLevel` or nested, e.g., family member (default is `true`).
    * The Service `kind` (see `de.iip_ecosphere.platform.services.ServiceKind`). 
    * The optional network management key 'netMgtKey' of a server process the service is relying on. May be empty for one.
    * `cmdArg`: Optional list of command line arguments to be passed when the service is started. Within this list, `${port}` is substituted with the service command port and `${protocol}` with the command protocol, one of the AAS protocols. Further `${tmp}` is replaced by the temporary folder and `${user}` by the user home directory.
    * `instances`: The optional number of service replicas to be launched. Ignored if negative. Default value is `1`.
    * resource desires: Optional values for the number of `cpus`, the `memory` and the `disk` space, the latter in bytes. If not given or not positive, internal default values are used, i.e., one CPU and, e.g., automatically determined for a JVM. The deployer may ignore these values if not applicable to the type of process to be started.
    * `parameters`: Parameters of the service that can be modified at runtime, each with `name`, optional `description` and qualified Java `type` name either pointing to a Java (primitive) type, a platform type or a type declared in the `types` section of the containing artifact.
    * `ensembleWith`: Optional service id of the service starting this service, i.e., the id of the ensemble leader. The id of the ensemble leader must be defined in the same artifact where it is used, i.e., it must be local. The deployer will start this service together with the mentioned service in one process, but not this service as individual process.
    * `relations`: Optional connection informations for individual channels/topics to connect the service to other local services or to external services via brokers. 
      * A relation may have an `id` which identifies relations on the same logical channel. Via `id`, relations can implicitly define service dependencies, i.e., outgoing relations must be fulfilled before the declaring service may start. 
      * The `channel` is the technical name identifying the relation on the service implementation, i.e., it is implementation dependent (see `application.yml`). If no `channel` is given, the settings of this relations apply to all local connections within the resource the service is deployed to. 
      * `endpoint` defines the port and host command line arguments defining the broker for the respective channel/topic. Within these arguments, `${port}` and `${host}` are substituted by actual values, respectively. `hostArg` is optional - if not given, localhost is used. Multiple command line arguments can be given separated by whitespaces, double quotes are considered to escape strings/keep strings together.
      * A relation may have an  optional `description`, one or multiple (comma-separated) qualified Java `type` names either pointing to Java (primitive) types, platform types or types declared in the `types` section of the containing artifact and a `direction` (either `IN` or `OUT`). The `function` denotes the implementing bean spring function (may be left empty if it shall not exhibited, e.g., in asynchronous connections). The `service` denotes the service id of the opposite service in the relation.
      * As the default relation with empty channel declares the endpoint information for all internal process local relations, it must neither have a description, a direction or a type. All internal relations must be declared but must not have an endpoint (as they indicate direction and data type). All external relations must provide all data. `ensembleWith` takes priority over implicit service dependencies stated by `relations`.
    * `process`: Optional structure indicating that the service is not directly implemented in Java rather than in terms of a further process, e.g., in a different programming language. A process may already be `started` or it might be started implicitly if realized as a platform service. In this case, the process information can be used to configure the ports of a running service or the home path/artifacts to be extracted for a service to be started implicitly.
      * `homePath` path in which the process shall be executed. If given, `${tmp}` will be replaced by the system temporary directory, `${user}` by the user home directory path. If not given, a temporary one will be created. (default is empty)
      * `artifacts` is a list of files (with path if needed, a leading slash is added if not present) to be extracted from the containing service artifact. These files provide the service implementation. Artifacts are searched within the main folder of the service artifact and within `BOOT-INF/classes`.
      * `executable` denotes the program to execute, i.e., either a system command (potentially substituted due to the overall setup described below) or a relative path within the `artifact` where the executable/code is located. All files and folders contained in `artifact` will be extracted to a local directory, which is set as home directory for the process.
      * `executablePath` additional path to be prepended before `executable`. If given, `${tmp}` will be replaced by the system temporary directory, `${user}` by the user home directory path. If not given, a temporary one will be created. (default is empty). 
      * `cmdArg` provide the arguments of `executable` in terms of a string list. As above, ${port} and ${protocol} are substituted with the command port/protocol, `${tmp}` is replaced by the temporary folder and `${user}` by the user home directory.
      * `streamEndpoint` and `aasEndpoint` denote (akin to `endpoint` above) communication settings to be appended to the command line arguments, here for the data streaming communication (i.e., the service will delegate the data there and receive input from there) and AAS command communication (typically via VAB). 
      * The Boolean property `started` indicates whether the underlying process is already started (default is `false`), e.g., in case of a database.
      *  `waitTime` defines a time to wait until the process is supposed to be ready before continuing service start activities (default `0`, positive values denote milliseconds to wait).
* `servers`: An optional list of server instance specifications to be started/stopped with the referencing application. A server has an `id` (denoting its network management key), a `host` name (may be superseded by a deployment plan) and a `port`. The remaining fields are similar to `process`. The `netMgrKey` of a service may refer to one of the servers declared here indicating a shared lifecycle.

The descriptor structure looks as given below (lists are indicates by a single element):

    id: <String>
    name: <String>
    version: <Version>
    types:
      - name: <QString>
        fields:
          -name: <String>
          -type: <QString>
    services:
      - id: <String>
        applicationId: <String>
        name: <String>
        version: <VersionString>
        description: <String>
        deployable: <Boolean>
        topLevel: <Boolean>
        kind: <value from de.iip_ecosphere.platform.services.ServiceKind>
        netMgtKey: <String>
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
        ensembleWith: <String>
        relations:
          - channel: <String>
            id: <String>
            description: <String>
            endpoint:
              portArg: <String, ${port} is substituted by the actual port number>
              hostArg: <String, ${host} is substituted by the actual host name>
            type: <QString>
            direction: <IN|OUT>
            function: <String>
            service: <String>
        process:
          homePath: <String>
          artifacts: 
            - <String>
          executable: <String>
          executablePath: <String>
          cmdArg:
            - <String>
          streamEndpoint:
            portArg: <String, ${port} is substituted by the actual port number>
            hostArg: <String, ${host} is substituted by the actual host name>
          aasEndpoint:
            portArg: <String, ${port} is substituted by the actual port number>
            hostArg: <String, ${host} is substituted by the actual host name>
          started: <boolean>
          waitTime: <Integer>
    servers:
      - id: <String>
        host: <String>
        port: <Integer> 
        homePath: <String>
        artifacts: 
            - <String>
        executable: <String>
        executablePath: <String>
        cmdArg:
          - <String>
        started: <boolean>
        waitTime: <Integer>

## Service Manager Configuration

Also the service manager itself can be configured via the ``iipecosphere.yml``, i.e., with this extension Spring execution concepts are/must be added to the containing component.

In addition to the basic AAS settings, the following properties can be configured:
* `service-mgr` is the IIP-Ecosphere service manager. Service operations such as starting or stopping may not be executed immediately, e.g., as they have to wait for starting up of services or JVMs. 
  * The `waitingTime` limits this time and causes called operations to failed if the given time is exceeded (default `60000` ms).
  * `availabilityRetryDelay` denotes the time to wait between two subsequent service availability request (default `500` ms).
  * `brokerHost` and `brokerPort` define the communication setup for the locally installed messaging service/broker that serves for local service communication, e.g., a MQTT broker. 
  * `deleteArtifacts` allows the service manager to delete downloaded artifacts. 
  * The `serviceProtocol` defines the AAS implementation protocol to be used for administrative communication with the services (default is the empty string denoting the default AAS protocol).
  * The `executables` define operating system command mappings for the `executable` used in the `process` structure of the service descriptor. If the `executable` is one of the keys listed in `executables`, the corresponding value is used instead of the value given in `executable`. This is intended that a service manager configuration can override operating system defaults if needed, e.g., because a system Python version cannot be upgrated for some reason and a newer/local version must be used due to service dependencies. Then the local version can be given as value for the key `python` here and the `executable` just mentions `python` which is substituted accordingly.
  * `javaOpts` additional Java options to be appended to the command line of service JVMs to be started, default: `–Dlog4j2.formatMsgNoLookups=True` due to  [CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228).
  * Please note that global service settings settings such as `aas` or `netMgr` occur here in the scope of `service-mgr`.
  * `sharedLibs` may point to a folder where shared service libraries are located (default `/shared` also within containers)
  * `downloadDir` the folder where to download service artifacts to
* `cloud.deployer.local` refers to the underlying mechanism of Spring Cloud Stream. Service artifacts and their working directory may be temporary if not configured or in a given folder. These files may be deleted automatically on exit or remain in the folder. Both settings are helpful for debugging.

The configuration structure is as shown below (the `executables` mapping is indicated by a single key-value pair):

    service-mgr:
      waitingTime: <Integer>
      availabilityRetryDelay: <Integer>
      brokerHost: <String>
      brokerPort: <Integer>
      deleteArtifacts: <Boolean>
      serviceProtocol: <|VAB-TCP|...>
      executables:
        - <String>: <String>
        - ...
      aas: ...
      netMgr: ...
      sharedLibs: <File>
      downloadDir: <File>
      javaOpts:
        - <String>
        - ...
    cloud:
      deployer:
        local:
          workingDirectoriesRoot: <Folder>
          deleteFilesOnExit: <Boolean>
      
## Service start options
One of the service start commands can take a map of options. The Spring Cloud Service manager implements the following options:
* `ensemble`: Change the ensemble/leader assignments at startup. Existing assignments from the artifact deployment descriptor can be overridden for top-level services. Unwanted assignments must be overridden too. The value of the option is a map in JSON format, i.e., `{"ens":"leader",...}` assigning the service with id `leader` as ensemble leader to the service with id `ens`. If leader cannot be resolved, e.g., an empty string, the leader assignment is cleared.

## Hint
Service startup on slow machines may fail due to Spring timeouts. Might be some deployer settings could help then.      
      
## Missing
* Handling of dependent services
* Testing of implementation processes, e.g., with Python.
* Advanced service operations: update, migrate, switch, clone
* Service monitoring (including implementation processes)