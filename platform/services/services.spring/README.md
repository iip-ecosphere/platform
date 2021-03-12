# IIP-Ecosphere platform: Service management for Spring Cloud Stream

Alternative service management implementation for services running with [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream). Services are given as JAR file. A JAR file may contain multiple services as well as network-coupled service implementations in other programming languages. The services are defined as usual in Spring Clout Stream as Java Bean methods returning Supplier, Function or Consumer. The linking between the services and their binding properties are defined in an `application.yml`. 

In addition, service deployment properties are given in a separate descriptor, the `deployment.yml` with the following structure. Unless stated explicitly, fields are mandatory.
* Artifact: Unique identifier `id` and `name` of the artifact. An artifact definition encounters the contained `services`.
* Service: Unique identifier `id`, `name`, `version` (numbers separated by dots) and an optional `description` of the service (default is empty). Optional specification whether the service is `deployable` in distributed fashion (default is `true`) and the service kind (see `de.iip_ecosphere.platform.services.ServiceKind`). A service may contain further sub-structures:
* `cmdArg`: Optional list of command line arguments to be passed when the service is started. 
* `instances`: The optional number of service replicas to be launched. Ignored if negative. Default value is `1`.
* resource desires: Optional values for the number of `cpus`, the `memory` and the `disk` space, the latter in bytes. If not given or not positive, internal default values are used, i.e., one CPU and, e.g., automatically determined for a JVM. The deployer may ignore these values if not applicable to the type of process to be started.
* `dependencies`: Optional list of service ids (in individual processes, i.e., not in `ensembleWith` this service depends on, i.e., the listed services shall be started before. As basis, the definition sequence of the services is taken, which is re-ordered to fullfill the depencies according to a best-effort algorithm, This may (later) apply to services on other resources. This is not a list of strings rather than a sub structure as in future other properties like a timeout may be added.
* `ensembleWith`: Optional list of service ids this service forms an ensemble with. The deployer will start this service together with the mentioned service in one process, but not the listed services as individual process. In other words, the services mentioned here are removed from the service list influenced by the dependencies. If `ensembleWith` and `dependencies` mention the same services, `ensembleWith` takes priority.
* `relations`: Optional connection informations for individual channels/topics to connect the service to other local services or to external services via brokers. If no `channel` is given, the following settings apply to all local connections within the resource the service is deployed to. `endpoint` defines the port and host command line arguments defining the broker for the respective channel/topic. Within these arguments, `${port}` and `${host}` are substituted by actual values, respectively. `hostArg` is optional - if not given, localhost is used.
* `process`: Optional structure indicating that the service is not directly implemented in Java rather than in terms of a further process, e.g., in a different programming language. `path` denotes the relative path within the containing artifact where the executable/code is located. `path` and all contained files and folders will be extracted to a local directory, which is set as home directory for the process to be launched via `cmdArg`, a string list of a command line program and its arguments. `streamEndpoint` and `aasEndpoint` denote (akin to `endpoint` for `process` above) communication settings to be appended to the command line arguments, here for the data streaming communication (i.e., the service will delegate the data there and receive input from there) and AAS command communication (typically via VAB).

    id: <String
    name: <String>
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
        dependencies:
          - id: <String>
        ensembleWith:
          - <String>
        relations:
          - channel: <String>
            endpoint:
              portArg: <String, ${port} is substituted by the actual port number>
              hostArg: <String, ${host} is substituted by the actual host name>
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
