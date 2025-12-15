# oktoflow platform: Installation

This document summarizes how to install and run the oktoflow platform. We discuss the usual installation from scratch but also the use of the pre-package example containers on Dockerhub.

## Installation from scratch

We recommend a full installation from scratch using the installation package. You can obtain the actual platform install package for [Windows](https://projects.sse.uni-hildesheim.de/oktoflow/install.zip) and for [Linux](https://projects.sse.uni-hildesheim.de/oktoflow/install.tar.gz). 

Unpack the installation package, e.g., on Linux

    tar xzvf install.tar.gz

All installation steps are described in the [installation package](../tools/Install/README.md), which is also contained in the respective archive files. Although the scripts aim at installing the prerequisites, we provide a recent summary of the [required software](PREREQUISITES.md).

## Pre-packaged Docker containers

For a simple startup, we offer two pre-packaged Docker containers on Docker Hub. Please be aware of that these containers are updated infrequently, at least as part of release activities. These containers are meant

* containing the platform parts (AAS server, the ECS Runtime, the Service Manager) and a simple example service artifact. 
* one with the Command Line Interface shown in the usage scenario above.

To experiment with the containers, use the following commands.
Create a network:

    docker network create --subnet=172.19.0.0/16 platform    

and start the platform parts:

    docker run --network platform --ip 172.19.0.22 -p 9001:9001 -p 9002:9002 -p 8883:8883 -p 9090:9090 -p 9091:9091 -p 4200:4200 iipecosphere/platform:platform_all.latest  

and similarly for the CLI

    docker run -i --network platform iipecosphere/platform:cli.latest
    
The service artifact that is needed to add/start services is available under the following local URI [the name is changing from SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar to SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar depending on your version]

    file:/device/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar

This setup uses port `8883` for the AMQP broker, `9001` for the Platform Asset Administration Shell (AAS) and `9002` for the AAS registry.

We discuss now a simple scenario on how the platform can be used through the command line interface:

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

The trace above illustrates a shell-like interaction of the user with the command line interface. The CLI provides commands for resources, containers and services. Resources are added automatically when an ECS runtime is executed. The trace above shows the properties listing for a resource with identifier `a005056C00008`. Containers and services can be managed by stating the respective resource identifier after the command. Before starting containers/services, the respective item must be added to the platform, i.e., an add command for a container image or a service artifact must be specified. The items are stated in terms of URIs, at the moment usually files on the local file system.

For illustration, we present now a one typical sequence of CLI commands. We assume that in the example installation environment `a005056C00008` is a listed resource and `http://localhost/container.tgz` is a valid container artifact with name `myContainer` as indicated by the associated deployment descriptor. Container `myContainer` has the service manager installed and will executed subsequent services. Moreover, we assume that `http://localhost/simpleMesh.jar` is a valid service artifact with name `simpleMesh` as defined by the included service descriptor. One sequence could be:
    
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
      
Please note that for accessing the containers or the services of a certain resource, we need the respective resource identifier. Similarly, for starting and stopping containers or services. When exiting the CLI, the CLI container will end. The platform container will continue operating until you stop it explicitly, e.g., using `docker ps` to obtain the container identifier of the running container and `docker stop <id>` to ultimately stop the container.

This was a rather simple scenario involving one combined server and compute node. The more nodes are involved, the less this manual approach is feasible. For this purpose, the platform supports so called deployment plans, which assign services from artifacts to resources. A deployment plan can be set into force with the command `deploy <path/URI>` and stopped with `undeploy <path/URI>`. These are also the basic management operations of the platform user interface. You will find more information on the deployment plans in the documentation of the [CLI](../platform/README.md).

## Further information

For further information on using the individual parts, please consult the platform handbook linked on the main page. Please see also 

* [Windows Installation Guide](Platform_Installation_Guide_for_Windows.pdf). 
* [Linux Installation Guide](Platform_Installation_Guide_for_Linux.pdf). 
