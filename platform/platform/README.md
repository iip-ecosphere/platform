# IIP-Ecosphere platform

The platform component for configuring and starting platform services. The platform component consists of the AAS abstraction as well as the abstract services and the abstract ECSruntime components for the AAS clients. Currently, this component just provides a command line interface (CLI) as basic access to platform functionality.

## Configuration

The basic YAML configuration of the platform services (in ``iipecosphere.yml``) provide the following settings:

    aas:
      server:
        schema: <HTTP|HTTPS>
        host: <String>
        port: <int>
        path: <String>
      registry:
        schema: <HTTP|HTTPS>
        host: <String>
        port: <int>
        path: <String>
      implementation:
        schema: <HTTP|HTTPS|TCP|IGNORE>
        host: <String>
        netmask: <String>
        port: <int>
        protocol: <VAB-TCP|>
      persistence: <INMEMORY|MONGO>
    artifactsFolder: <Path>
    artifactsUriPrefix: <Uri>
    
The `aas` settings are similar to [ECS (Edge-Cloud-Server) runtime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime/README.md), while `mode` is ignored/fixed to `REMOTE_DEPLOY`. `aas:persistence` defines the AAS persistence mechanism and may require further software installation, e.g. MongoDB.

`artifactsFolder` denotes a folder where artifacts are located for download by the devices/resources. Artifacts may be service artifacts (artifact including service deployment descriptor) or container image artifacts (usually one per folder, consisting of a container descriptor and an image file). By default, artifacts found in this folder are addressed by
their local file URI. If the folder is empty or does not exist, no artifacts will be added to the artifacts manager/AAS. However, if the folder is part of a web server installation, `artifactsUriPrefix` can be used to turn the relative artifacts path into a webserver URL (shall end with a /, empty by default). Please note that the artifacts folder is a preliminary solution to be replaced by the S3 storage of the device management.

## Running the services

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

## Running the CLI

The command line interface can be started calling it's main class `de.iip_ecosphere.platform.platform.Cli`. The CLI is configured as indicated above and requires running platform services. Currently, the CLI offers mostly low-level commands to test and explore the platform. The more high-level components occur, the more the CLI may change and, if somehow possible, ultimately turn into a Web UI.

Main functionality of the CLI:
* List all available resources on which an ECS-Runtime is being executed. Resources are addressed via their identifier, which is either determined via the `IdProvider` in the service layer, or, if permitted via the command line parameter `--iip.id=<id>`, particularly for testing. The default `IdProvider` creates a device ID based on the (first enumerated) MAC address of the device and allows for overriding the ID via command line. Optionally, an SSH-Server for the device management can be started on a resource given by its id.
* Access to the containers running on a device. After entering that level based on the device id of the target device, containers can be added (via their local or remote URI), started, stopped and removed.
* Access to the services running on a device. After entering that level based on the device id of the target device, service implementation artifacts can be added (via their local or remote URI) or removed. Services determined by their ID can be started or stopped.
* On the global level, a service deployment plan can be executed, resembling all the individual operations mentioned above into a simple plan script (see below). The same script can be used to undeploy previously deployed services.
* Also a snapshot of the actual platform AAS can be taken (currently in terms of the AASX Package Explorer format with fixed name `platform.aasx` stored in the folder where the CLI was started within).

Deploying multiple services, in particular across devices, can be tedious and error-prone using just the low level commands mentioned above. Therefore, the CLI offers the execution of simple deployment plans given as YAML files.

    artifact: <path/URI>
    parallelize: <Boolean>
    onUndeployRemoveArtifact: <Boolean>
    assignments:
      - resource: <ResourceId>
        artifact: <path/URI>
        services:
          - <ServiceId>
          - <ServiceId>
      - ...
    container:
      - resource: <ResourceId>
        containerDesc: <path/URI>

A deployment plan specifies the service implementation `artifact` containing the services, either as local path or as URI. The execution of the plan can be done sequentially (the default) or in parallel (set `parallelize` to `true`). If sequential, the author of the plan has the responsibility to state the resources/services in the sequence that they can be started through the installed/configured service manager. For the Spring Cloud Stream service manager, currently, all pre-requisite services must be started before an upstream service can be started, i.e., resources and services shall be given in their flow sequence from sources to sinks. Service implementation artifacts can be automatically removed from the respective resources upon undeployment (`onUndeployRemoveArtifact`, default is `true`). Within `assignments` the target resources with their respective ids and within the resources the services to be started with their respective service ids must be given. If not otherwise specified by an assignment-specific `artifact` (within `assignments` as shown above), the global `artifact` will be used. As indicated above, multiple services and resources can be given, a single resource can also be mentioned multiple times if required. 

Moreover, if the CLI shall also start containers containing the respective service manager (if adequate providing containing also the service artifacts, i.e., `artifact` would be then a local path/URL within those containers), the containers and their startup sequence can be stated in the `container` section. An entry there names the `resource` id where to start the container and the container descriptor (`containerDesc`) detailing the container to be started. The container image must be located in the same folder as the container descriptor. Upon undeployment, the containers will be stopped in reversed sequence, and, if `onUndeployRemoveArtifact` is enabled, the container will also be removed from the device.

As stated above, managing multiple services may be an error prone task depending on the setup:

* If all services of an artifact shall be executed on the (resource of the) same service manager, just the command `startAll <artifactId>` is required. The service manager binds all services against a local broker (determined by the platform configuration, started by installation) and starts (or stops) them along the service dependencies given in the service deployment descriptor in the service artifact. In the platform configuration `transportProtocol.globalHost` and `serviceManager.brokerHost` must be `localhost`.
* If services shall be executed in a distributed manner, two main setups can occur.
    * Different service managers on different physical resources. Each service manager runs a local broker for local communication while inter-device communication between the services happen via the global broker defined in the platform communication (which must be started explicitly). Local communication happens via the respective local broker determined by the platform configuration (`serviceManager.brokerHost`) and started by installation, while the inter-service configuration runs via `transportProtocol.globalHost`. 
    * A local setup for testing, i.e., the service managers run on the same physical resource. In this case, besides the default ECS runtime and service manager instances, per emulated device a pair of additional ECS runtime and service manager instance are required. As the default device id, e.g., the MAC address, is used by the default instances, the instances of the additional pairs must declare an own device id, e.g., defined per command line (parameter `--iip.id=<deviceId>`). Moreover, both instances of such a pair must define an own port for the AAS implementation server (parameter `--iip.port=<port>`), overriding the port defined in the platform configuration. In the platform configuration `transportProtocol.globalHost` and `serviceManager.brokerHost` shall be `localhost`, but may differ also depending on the setup requirements.

## Missing
- AAS discovery, currently we rely on the full IP specification instantiated through the configuration
- Higher level commands for higher level components