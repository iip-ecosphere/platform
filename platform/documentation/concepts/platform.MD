# oktoflow platform configuration (technical setup)

Also the platform itself can be configured in terms of its technical setup, e.g., regarding the platform-wide global use of communication protocols. As the related components are inserted into the specificially configured platform, the platform instantion process creates a specific platform instance (i.e., except for demonstrations there are no pre-build platform instance binaries).

An example for the technical setup of a platform instance (as used in `examples.python`) looks as shown below:

```
project TechnicalSetup {

    import IIPEcosphere;
    import DataTypes;

    annotate BindingTime bindingTime = BindingTime::compile to .;

    // ------------ component setup ------------------

    serializer = Serializer::Json;
    // serviceManager, containerManager are already defined    
        
    aasServer = {
        schema = AasSchema::HTTP,
        port = 9001,
        host = "127.0.0.1"
    };
    aasRegistryServer = {
        schema = AasSchema::HTTP,
        port = 9002,
        host = "127.0.0.1",
        path = "registry"
    };
    aasImplServer = {
        port = 9003
    };
    aasProtocol = AasProtocolVabTcp{};
    
    // ------------------ transport --------------------------
    
    transportProtocol = TransportProtocolAMQP {
        port = 8883,
        security = { // replaces user/password, requires identityStore.yml in src/test/ or src/main depeding on use
            authenticationKey = "amqp"
        }
    };
    
    serviceProtocol = ServiceProtocolAMQP {};
    
    // ------------------ resources/devicemgt ----------------
    
    deviceRegistry = BasicDeviceRegistry {
    };
    
    deviceMgtStorage = S3MockDeviceMgtStorage {
        host = "localhost",
        port = 8884,
        region = "local"
    };
    
    // just for testing, this instantiates app rather than platform
    deviceMgtStorageServer = S3MockDeviceMgtStorageServer{};
    
    // --------------------- monitoring ---------------------------
    
    // current default: no monitoring configured
    
    // ------------------------- UI -------------------------------
    
    // current default: no UI configured
    
    // ---------- generation setup ------------
    
    sharedInterfaces = true;
    sharedArtifact = "de.iip-ecosphere.platform:apps.ExamplePythonAppInterfaces:" + iipVer; // for testing all-in-one

    // ------------ freezing ------------------
    
    freeze {
        aas;
        aasServer;
        aasRegistryServer;
        aasImplServer;
        aasPersistency;
        aasProtocol;
        serializer;
        transportProtocol;
        serviceManager;
        serviceProtocol;
        containerManager;
        deviceMgtStorageServer;
        deviceMgtStorage;
        deviceRegistry;
        javaModuleOpts;
        javaOpts;
        pidDir;
        sharedInterfaces;
        sharedArtifact;
        platformMonitoring;
        managementUi;
        artifactsFolder;
        artifactsUriPrefix;
        .; // every variable declared in this project
    } but (f|f.bindingTime >= BindingTime.runtimeMon);

}
```

The technical setup is based on the configuration meta model (still `IIPEcosphere`, `DataTypes`) and for, for finally freezing these settings, annotates all variables in this IVML project with their binding time (latest time when the value must be fixed). The core part consists of the AAS (network ) setup, the data transport setup, the device setup, the central monitoring setup (left out here), the management UI setup (also left out here) and, specifically for examples, the setup of the shared interface artifact for apps (to prevent accidental reuse/collisions with a running platform or other all-in-one examples). The final part freezes all required variables (except for those with a binding time at platform startup or later), here listed explicitly as these variables are declared in other IVML projects and cannot be addressed by "." (only variables declared in this project).

## Asset Administration Shells (AAS)

In oktoflow, AAS are used as mechanism for distributed data sharing and communication. Thus, the various server instances of an AAS installation must be set up, started and known to oktoflow. However, the AAS installation may be part of/controlled by oktoflow or compatible instances may be set up separately, e.g., as BaSyx Docker containers. Although desirable, also do to legacy reasons, we currently do not use service/network discovery rather than explicit network addresses.

The technical AAS setup consists of the following global configuration variables

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| aas | `AasImpl` | Technology/plugin realizing the AAS implementation (`AasImpl::BaSyx` or `AasImpl::BaSyx2`) | `AasImpl::BaSyx` |
| aasProtocol | `AasProtocol` | Communication protocol for calling AAS operations | `AasProtocolVabTcp` |
| aasPluginId | `String` | Specific plugin id of AAS implementing plugin to use, e.g., for certain implementation versions; if not specified, the default AAS plugin is used | |
| aasServer | `EndpointAddress` | AAS (repository) server || 
| smServer | `EndpointAddress` | Submodel repository server, required for metamodel v3 (`AasImpl::BaSyx2`) || 
| aasRegistryServer | `EndpointAddress` | AAS registry server || 
| smRegistryServer | `EndpointAddress` | Submodel registry server, required for metamodel v3 (`AasImpl::BaSyx2`) || 
| aasImplServer | `ImplAddress` | oktoflow AAS asset implementation server, in particular providing operation implementations via `aasProtocol` || 
| aasPersistency | `Persistency` | How to store AAS/submodels if at all (`Persistency::INMEMORY` or `Persistency::MONGO`) | `Persistency::INMEMORY` |
| aasAccessControlOrigin | `String` | CORS specification used for all oktoflow managed AAS server instances | `*` |
| aasStartupTimeout | `Integer` | Acceptable timeout in ms until AAS servers are expected to be available at platform start | 120000 |
| aasSemanticIdResolver | `setOf(AasSemanticIdResolver)` | Plugins used to resolve AAS semantic ids in oktoflow | `EclassYamlAasSemanticIdResolver`, `AdminShellYamlAasSemanticIdResolver` |

The technical AAS setup relies on the following configuration types:

### AasProtocol

AAS communication protocol specification.

| Field | Description | Default |
| --- |  --- | --- |
| schema | Protocol schema used for communication, either `AasSchema::HTTP`, `AasSchema::HTTPS`, `AasSchema::TCP` | | 
| supportedBy | set of `AasImpl` constant instances declaring which AAS implementation supports this protocol; used for configuration validation | |

The AAS communication protocol specification are usually applied in terms of pre-defined instances, namely
* `AasProtocolDefault` representing the default communciation protocol defined by the AAS implementation plugin. 
* `AasProtocolVabTcp` representing BaSyx1 Virtual Automation Bus via TCP. 
* `AasProtocolVabHttp` representing BaSyx1 Virtual Automation Bus via HTTP. 
* `AasProtocolRest` representing BaSyx2 REST via HTTP. 
* `AasProtocolRestSecure` representing BaSyx2 REST via HTTPS. 

### AAS network addresses

The basic type is the `EphemeralServerAddress` defining the following fields

| Field | Description | Default |
| --- |  --- | --- |
| schema | Protocol schema used for communication, either `AasSchema::HTTP`, `AasSchema::HTTPS`, `AasSchema::TCP` | | 
| host | Hostname of the server | |
| port | Port on the server | |
| security | SecuritySettings (see [DataTypes](datatypes.md)) | |

`ServerAddress` refines `EphemeralServerAddress` by requiring a valid port number.

`EndpointAddress` refines `EphemeralServerAddress` by adding

| Field | Description | Default |
| --- |  --- | --- |
| path | the URL path on the server | "" |
| serverHost | overwrites an AAS host name provided by the server, e.g., registry endpoint, to cope with multiple IP server addresses (default undefined) | `null` |

`ImplAddress` refines `EphemeralServerAddress` by adding

| Field | Description | Default |
| --- |  --- | --- |
| protocol | the `AasProtocol` to use | `aasProtocol` as defined above |
| schema | Protocol schema used for communication, either `AasSchema::HTTP`, `AasSchema::HTTPS`, `AasSchema::TCP` | `aasProtocol.schema` the schema of  `protocol` | 
| host | the host of the server, "127.0.0.1" is automatically replaced by the actual host IP if possible | `"127.0.0.1"` | 
| netmask | netmask narrowing down the IP the actual host IP, particularly helpful if a device offers multiple networks like Ethernet and WiFi; if not given, take the "first" one | `""` | 
| devicesAsEphemeral | If `true`, AAS container ports are determined on start by service manager / container descriptor. Else use device `aasImplPort` or as fallback `aasImplServer.port`. | `true` | 

## Data Transport

Data transport is responsible for protocol, wire format and (broker) server settings for the global data transport in the platform. 

The transport setup consists of the following global configuration variables

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| serializer | 'Serializer' | Defines the wire format for data transport, either `Serializer::Json` or `Serializer::ProtoBuf` while ProtoBuf is currently not supported by the code generation | `Serializer::Json` |
| transportProtocol | `TransportProtocol` | Defines the transport protocol and network settings for negotiating transport protocol connections | `TransportProtocolAMQP` |

### Transport Protocol

`TransportProtocol` is an abstract type refined by several specific default protocol instances, which shall be refined for setting up a certain server.

| Field | Description | Default |
| --- |  --- | --- |
| type | Type of transport protocol, either `ProtocolType:Mqttv3`, `ProtocolType:Mqttv5`, `ProtocolType:Amqp` pointing to a certain subset of oktoflow transport protocol plugins. | |
| globalHost | Platform global transport (broker) host, ignored for local intra-device communication. | `"localhost"` |
| port | Global communication port for data transport | 8883 |
| localPort | Communication port for local intra-device data transport, by default the same as the global port | `port` |
| localEcsPort | The local communication port for ECSruntime containers | `8889` |
| security | Security settings (see [DataTypes](datatypes.md)) | |
| brokerVer | Maven artifact version of the (instantiated default) broker to use, empty by default indicating no specific version | `""` |
| actionTimeout | Timeout in ms for client-broker actions  | 1000 |
| memLimit | Process memory limit in MBytes, e.g. for JVMs | 440 |
| gatewayPort | Transport-UI gateway communication port used for transferring platform status messages to a UI compliant protocol, e.g., websockets if AAS is not adequate | 10000 |
| netmask | Transport-UI gateway server netmask to determine proper IP address if devices support multiple networks | `""` | 

Refined types of `TransportProtocol` are:
* `TransportProtocolAMQP` setting the `type` to `ProtocolType::Amqp` and `brokerVer` to a certain version of Apache QPID.
* `TransportProtocolMQTTv3` setting the `type` to `ProtocolType::Mqttv3` and `brokerVer` to a certain version of Hivemq.
* `TransportProtocolMQTTv5` setting the `type` to `ProtocolType::Mqttv5` and `brokerVer` to a certain version of Hivemq.

## Data Transport among Services

The data transport among services typically follows the platform transport setup, but may also be specific.

The service transport setup consists of the following global configuration variables

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| serviceProtocol | 'ServiceProtocol' | Defines the service data transport | `ServiceProtocolAMQP` |
| serviceManager | 'ServiceManager' | Defines the service manager | `SpringCloudStream` |

### Service Protocol

`ServiceProtocol` is an abstract type refined by several specific default protocol instances, which shall be refined for setup.

| Field | Description | Default |
| --- |  --- | --- |
| type | Type of transport protocol, either `ProtocolType:Mqttv3`, `ProtocolType:Mqttv5`, `ProtocolType:Amqp` pointing to a certain subset of oktoflow transport protocol plugins. | |

Refined types of `ServiceProtocol` are:
* `ServiceProtocolAMQP` setting the `type` to `ProtocolType::Amqp`.
* `ServiceProtocolMQTTv3` setting the `type` to `ProtocolType::Mqttv3` using the Paho client.
* `ServiceProtocolMQTTv5` setting the `type` to `ProtocolType::Mqttv5` using the Paho client.
* `ServiceProtocolMQTTv3hive` setting the `type` to `ProtocolType::Mqttv3` using the Hivemq client.
* `ServiceProtocolMQTTv5hive` setting the `type` to `ProtocolType::Mqttv5` using the Hivemq client.
* `ServiceProtocolGeneric` setting the `type` to `transportProtocol.type` using the global transport protocol instance.

### Service Manager

`ServiceManager` is an abstract type currently refined by a default (implementation).

| Field | Description | Default |
| --- |  --- | --- |
| memLimit | Process memory limit in MBytes, e.g. for JVMs | 512 |
| monitoringUpdatePeriod | Period among sending two instances of monitoring data | 2000 |
| appPluginsFolder | Optional folder where to store the plugins of apps to be executed by the service manager, if not given the plugins folder of the service manager is used (default) | `null` |
| updateAppPlugins | Shall app plugins be updated on app start. If not enabled, the platform checks whether at least all required plugins are available and, if not, downloads the respective version, either to the `appPluginsFolder` or to the plugins folder of the service manager | false | 

`SpringCloudStream` refines the `ServiceManager` for the use of Spring Cloud Streams (the default) for service execution.

| Field | Description | Default |
| --- |  --- | --- |
| deleteFilesOnExit | delete (temporary) app files on process exit | true |
| deleteArtifacts | delete app artifacts on process exit | true |
| brokerHost | The transport communication broker for device-internal communication, usually localhost. | "localhost" |
| brokerPort | The broker port for device-internal communication. | 8883 |
| waitingTime | Waiting time(out) in ms for distributed app/service operations. | 60000 |
| availabilityRetryDelay | Delay / waiting time in ms for to ask again for the status/availability of a certain service. | 500 |

## Resource and Device Management

The resource management cares for known/usable devices. The setup consists of the following global configuration variables

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| deviceRegistry | `DeviceRegistry` | Defines the device registry approach to be used by the resource management. | `BasicDeviceRegistry` |
| deviceMgtStorage | `DeviceMgtStorage` | Defines the data storage approach  for device management information. | `S3MockDeviceMgtStorage` |
| deviceMgtStorageServer | `DeviceMgtStorageServer` | Defines the server setup for the device management. | `S3MockDeviceMgtStorageServer` | 

### Device Registry

`DeviceRegistry` is an abstract type currently refined by 
* `BasicDeviceRegistry` the basic (in-memory) device registry
* `ThingsBoardDeviceRegistry` the Thingsboard-based device registry

### Device Management Storage

`DeviceMgtStorage` is an abstract type with the following fields 

| Field | Description | Default |
| --- |  --- | --- |
| host | the host name of the device management storage | `"localhost"` |
| port | the communication port of the device management storage | `8884` |
| region | the geographical region | `"local"` |
| authenticationKey | the authentication key form the identity storage (see security settings in [DataTypes](datatypes.md) |  |
| runtimeStorage | the runtime information storage for devices | `{bucket="iip", prefix="ecs", packageDescriptor="runtime.yml", packageFileName="runtime.zip"}`
| configStorage | the configuration/update information storage for devices | `{bucket="iip", prefix="config", packageDescriptor="config.yml", packageFileName="config.zip"}`

The `DeviceMgtStorage` relies on `PackageStorage` with the following fields 

| Field | Description | Default |
| --- |  --- | --- |
| bucket | Bucket name in the storage | |
| prefix | File/descriptor access path prefix | |
| packageDescriptor | Descriptor file name containing information on how to load/process the data. | |
| packageFilename | File name suffix. | |

The concrete types of `DeviceMgtStorage` are
* `MinIoDeviceMgtStorage` using MinIo.
* `S3MockDeviceMgtStorage` using a mock implementation of Amazon S3.

### Device Management Storage Server

`DeviceMgtStorageServer` is an abstract type representing a local device management storage server through the following fields 

| Field | Description | Default |
| --- |  --- | --- |
| port | Network port of the server providing access to the device management storage server. | `8884` |
| path | Hostname or IP of the server providing access to the device management storage server. | `` |
| authenticationKey | the authentication key form the identity storage (see security settings in [DataTypes](datatypes.md) |  |

## Central Platform Monitoring

The central platform monitoring is responsible for collecting, storing and visualizing monitoring messages delivered by the distributed components of a platform instance, in including services, connectors, apps, etc.

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| platformMonitoring | `PlatformMonitoring` | Defines the monitoring approach to be used. | `NoPlatformMonitoring` |

### Platform Monitoring

`PlatformMonitoring` is an abstract type. It is refined by `NoPlatformMonitoring` and `PrometheusPlatformMonitoring`, the latter with the following fields 

| Field | Description | Default |
| --- |  --- | --- |
| schema | The prometheus communication schema, either `PrometheusSchema::HTTP` or `PrometheusSchema::HTTPS` | `PrometheusSchema::HTTPS` |
| host | Hostname or IP of the Prometheus server. | `"127.0.0.1"` |
| port | Network port of the Prometheus server. | `9090` |
| running | Whether the Prometheus Server is already running, i.e., not under platform control. | `false` | 
| exporterPort | Port of the oktoflow-to-Prometheus data exporter, `-1` means ephemerial. | `-1` | 
| exporterHost | Hostname or IP of the oktoflow-to-Prometheus data exporter. | `"127.0.0.1"` |
| exporterRunning | Whether the oktoflow-to-Prometheus data exporter is already running, i.e., not under platform control. | `false` |
| alertMgrPort | Port of the Prometheus alert manager. | `9091` | 
| alertMgrHost | Hostname or IP of the Prometheus alert manager. | `"127.0.0.1"` |
| alertMgrRunning | Whether the Prometheus alert manager is already running, i.e., not under platform control. | `false` |
| scrapeInterval | The interval in ms how often to gather/scrape received data, must be larger than `50` | `1001` |
| scrapeTimeout | The time to wait in ms before causing a timeout while scraping, must be larger than `50` and less than `scrapeInterval` | `1000` |

## Management User Interface

The management UI provides read/write access the the platform's AAS, allowing to configure services or applications as well as to start/stop applications.

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| managementUi | `ManagementUI` | Defines the Platform UI approach to be used. | `NoManagementUI` |

`ManagementUI` is an abstract type. It is refined by `NoManagementUI` and `AngularManagementUI`, the latter with the following fields 

| Field | Description | Default |
| --- |  --- | --- |
| port | Network port of the Angular server (on the host where the UI is started). | `4200` |

## Further global settings

In addition to the setup topics discussed above, oktoflow has a set of further global settings, most pre-defined by default values (usually in own, non-changeable frozen variables, not listed here).

### Java/Process settings

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| javaModuleOpts | `String` | Java module options to be passed to a JVM to be started. | `--add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED` | 
| javaOpts | `String` | JVM options to be passed to a JVM to be started. | `-Dio.netty.tryReflectionSetAccessible=true -Dlog4j2.formatMsgNoLookups=true` | 
| pidDir | `String` | Directory where process information files shall be located; only used on Linux, the temporary folder for Windows by default | `/run` | 
| instDir | `String` | oktoflow installation directory, to be instantiated into OS system services, in particular for Linux | `/opt/iip` | 
| javaExe | `String` | The Java HOME directory, to be instantiated into OS system services, in particular for Linux; intended to be a symlink | `/opt/iip/java` | 

### Python settings

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| platformPython | `refTo(SystemDependency)` | The system dependency indicating the platform Python to be installed, e.g., in containers | `PYTHON313` 

### Folder/Model/Generation settings

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| artifactsFolder | `String` | Folder (if not absolute, interpreted relative to `instDir`) where application artifacts shall be downloaded to | `artifacts` | 
| artifactsUriPrefix | `String` | URL path prefix to be prepended to an artifact name for download  | `""` | 
| uploadFolder | `String` | Folder where to upload user files through the UI, if not given the same as `artifactsFolder` | | 
| modelBaseFolder | `String` | Folder in which the folder for the platform meta model is located, if not absolute interpreted relative to `instFolder` | `.` | 
| metaModelFolder | `String` | Name of the folder containing the platform meta model, if not absolute interpreted relative to `modelBaseFolder` | `src/main/easy` | 
| additionalModelFolders | `setOf(String)` | Name of additional folders with re-usable IVML files required by the the platform configuration, if not absolute interpreted relative to `modelBaseFolder` | | 
| configFolder | `String` | Name of folders where the platform configuration is located, if not absolute interpreted relative to `modelBaseFolder` | `null` | 
| genTarget | `String` | Name of the folder where the platform instantiation shall place generated files | `gen` | 
| modelName | `String` | Name of the top-level IVML project containing the platform configuration | `PlatformConfiguration` | 
| pluginsFolder | `String` | Name of the folder containing oktoflow plugins; if not absolute, interpreted relative to `instDir` | `plugins` |  
| zipBinaries | `Boolean` | Enable creation of ZIPped device platform binaries | `false` |

### Device settings

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| deviceHeartbeatTimeout | `Integer` | Heartbeat timeout for devices in ms, i.e., when is a device considered to be unavailable | `5 * max(containerManager.monitoringUpdatePeriod, serviceManager.monitoringUpdatePeriod)` | 

### Repository settings

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| sharedInterfaces | `Boolean` | Generate application interfaces into application code (default false, i.e., use separate folders) | `false` |
| sharedArtifact | `String` | Maven coordinate of the generated artifact containing the platform service interfaces | `"de.iip-ecosphere.platform:apps.AppInterfaces:" + iipVer` | 
| mavenReleaseRepository | `MavenRepository` | Optional maven release repository settings. If not given, deploy to local repository. ||
| mavenSnapshotRepository | `MavenRepository` | Optional maven release repository settings. If not given, deploy to local repository. ||
| mavenDeployType | `MavenDeployType` | Type of maven deployment indicating additionally required maven build plugins, either `MavenDeployType::MAVEN` or `MavenDeployType::NEXUS` | `MavenDeployType::MAVEN` |

### Container settings

| Variable | Type | Description | Default |
| --- |  --- | --- | --- |
| containerGeneration | `Boolean` | Shall the platform generate application containers | `true` |
| footprintFolder | `String` | Folder where file footprints for container creation shall be stored, if not absolute interpreted relative to `instFolder` | `target/footprints` | 
| platformContainerGeneration | `Boolean` | Shall the platform generate platform containers | `true` |
| centralMemLimit | `Integer` | Memory limit for central platform services, e.g., JVM memory limit in MBytes | `0` |
| monitoringMemLimit | `Integer` | Memory limit for central monitoring service, e.g., JVM memory limit in MBytes | `0` |
| containerBaseImageMethod | `Boolean` | Use container base image method during the container creation | `false` | 
| containerTestingMode | `Boolean` | Add more testing/debugging software into the containers | `false` | 
| forceContainersCreation | `Boolean` | Enforce containers creation regardless of the container's fingerprint | `false` | 
