# oktoflow platform: Installation

This document summarizes how to install and run the oktoflow platform. We discuss the usual installation from scratch but also the use of the pre-package example containers on Dockerhub. At the end of this document you will find a usage example. Running it successfully indicates that the platform has been installed and is operating correctly.

> **Important**  
> Before starting the installation, ensure that all required dependencies are installed on your system.  
> A complete and up-to-date list is provided in [Required Software](PREREQUISITES.md).


## Installation from scratch

We recommend a full installation from scratch using the installation package. You can obtain the current platform installation package for [Windows](https://projects.sse.uni-hildesheim.de/oktoflow/install.zip) and for [Linux](https://projects.sse.uni-hildesheim.de/oktoflow/install.tar.gz). 

Create a directory, copy the installation package into that directory and unpack the installation package, e.g., on Linux

    tar xzvf install.tar.gz

All installation steps are described in the [README.md of the installation package](../tools/Install/README.md), which is also contained in the respective archive files. Although the scripts aim at installing the prerequisites, we provide a recent summary of the [required software](PREREQUISITES.md).

## Pre-packaged Docker containers

For a simple startup, we offer two pre-packaged Docker containers on Docker Hub. Please note that these containers are updated infrequently and primarily as part of official release activities. These containers include:
- The main platform components (AAS Server, ECS Runtime, Service Manager) and a simple example service artifact
- An optional container providing the Command Line Interface (CLI), used in the example scenario below

To experiment with the containers, use the following commands.

The custom Docker network ensures that the platform components can communicate using fixed IP addresses. Create a network:

    docker network create --subnet=172.19.0.0/16 platform    

and start the platform parts:

    docker run --network platform --ip 172.19.0.22 -p 9001:9001 -p 9002:9002 -p 8883:8883 -p 9090:9090 -p 9091:9091 -p 4200:4200 iipecosphere/platform:platform_all.latest  

and similarly for the CLI

    docker run -i --network platform iipecosphere/platform:cli.latest
    
The service artifact that is needed to add/start services is available under the following local URI:

    file:/device/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar

Depending on the platform version, the service artifact may be named  
`SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar` or  
`SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar`.

This setup uses port `8883` for the AMQP broker, `9001` for the Platform Asset Administration Shell (AAS) and `9002` for the AAS registry.

## Example scenario
We now discuss a simple scenario on how the platform can be used via the command line interface (CLI). The following output shows a typical interactive session with the CLI:

    oktoflow, interactive platform command line
    AAS server: http://127.0.0.1:9001
    AAS registry: http://127.0.0.1:9002/registry
    Type "help" for help.
    > resources
    resources> help
     list
     help
     back
    resources> list
     - Resource a005056C00008
       systemdisktotal: 1023887356
       systemmemorytotal: 2147483647
       simplemeterlist: ["system.cpu.count","system.cpu.usage", "system.disk.free", "system.memory.free"…]
       containerSystemName: Docker
       systemmemoryfree: 2147483647
       systemdiskfree: 464061712
       systemmemoryused: 2147483647
       systemdiskusable: 464061712
       systemmemoryusage: 0.5555296172875698
       systemdiskused: 559825644
    resources> back
    > exit

The trace above illustrates a shell-like interaction of the user with the CLI. The CLI provides commands for:
- resources 
- containers 
- services
 
 **Resources** are added automatically when an ECS runtime is executed. The trace above shows the properties listing for a resource with identifier `a005056C00008`. 

**Containers** and **services** can be managed by stating the respective resource identifier after the command. Before starting containers/services, the respective item must be added to the platform, i.e., an add command for a container image or a service artifact must be specified. The items are stated in terms of URIs, at the moment usually files on the local file system.

### Typical sequence of CLI commands
For illustration, we now present a one typical sequence of CLI commands. We assume that in the example installation environment `a005056C00008` is a listed resource and `http://localhost/container.tgz` is a valid container artifact with name `myContainer` as indicated by the associated deployment descriptor.

Container `myContainer` has the Service Manager installed and will execute subsequent services. Moreover, we assume that `http://localhost/simpleMesh.jar` is a valid service artifact with name `simpleMesh` as defined by the included service descriptor. One sequence could be:
    
    resources
      list
      ..
    containers a005056C00008
      add http://localhost/container.tgz
      list
      start myContainer
      ..
    services a005056C00008
      add http://localhost/simpleMesh.jar
      listArtifacts
      startAll simpleMesh
      listServices
      stopAll simpleMesh
      listServices
      remove simpleMesh
      ..
    containers a005056C00008
      stop myContainer
      undeploy myContainer
      list
      ..
    exit
      
### Communication with Containers
Please note that accessing, starting, and stopping containers or services requires specifying the corresponding resource identifier. When exiting the CLI, the CLI container will end. The platform container will continue operating until you stop it explicitly, e.g., using `docker ps` to obtain the container identifier of the running container and `docker stop <id>` to ultimately stop the container.

### Deployment Plans
This was a rather simple scenario involving one combined server and compute node. The more nodes are involved, the less this manual approach is feasible. For this purpose, the platform supports so-called deployment plans, which assign services from artifacts to resources. A deployment plan can be set into force with the command `deploy <path/URI>` and stopped with `undeploy <path/URI>`. These are also the basic management operations of the platform user interface. You will find more information on the deployment plans in the documentation of the [CLI](../platform/README.md).


<span style="color:green">Not ready maybe remove´ </span>
## Glossary 

**AAS Server**  
Component providing Asset Administration Shell (AAS) instances and their associated submodels. It exposes standardized interfaces for accessing and managing asset-related information within the platform.

**ECS Runtime**  
Execution environment responsible for managing computational resources and executing containers and services on a node. It registers available resources with the platform and reports their properties.

**Service Manager**  
Component responsible for managing the lifecycle of services, including deployment, start, stop, and removal of service instances.

**Management UI**  
Graphical user interface of the platform used to monitor and manage resources, containers, services, and deployment plans without using the command line interface.

**CLI (Command Line Interface)**  
Text-based interface allowing users to interact with the platform by issuing commands for managing resources, containers, services, and deployment plans.



## Further information

For further information on using the individual parts, please consult the platform handbook linked on the main page. 