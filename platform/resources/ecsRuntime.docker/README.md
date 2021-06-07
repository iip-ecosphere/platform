# IIP-Ecosphere platform: ECSruntime extension for plain Docker

[Docker](https://www.docker.com/) extension/implementation of the SPI interfaces of the IIP-Ecosphere container management agent (ECSruntime) running on compute resources.

## Container descriptor

The container descriptor is an additional file in a docker image identifying the container. Typically, the file is called `ìmage-info.yml` (see below for a different name). The remainder settings look like

    !!de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerDescriptor
    id: <String>
    name: <String>
    version: <Version>
    dockerImageName: <String>
    dockerImageZipfile: <String>
    downloadDirectory: <String>

The first line indicates the internal descriptor used to represent the date for the manager. `id`is the identifier the container can uniquely addressed via the IIP-Ecosphere interfaces. This identifier is determined by the platform and instantiated into that file. Similarly, the `name` is the descriptive name of the container and `version` is the version number of the container in terms of IIP-ecosphere version numbers. `dockerImageZipfile` is the name of the compressed file including this container descriptor and the docker image (`dockerImageName`). `downloadDirectory`is the directory from where the container is made avilable, i.e., where was downloaded from from the perspective of the container manager.

## Configuration

In addition to the basic AAS settings, this extension allows for the following settings:

    docker:
        dockerHost: <String>
        dockerImageYamlFilename: <String>
        deleteWhenUndeployed: <Boolean>
    
    
The `dockerHost` is an operation system specific string denoting how to connect to docker. For Linux, this is typically `unix:///var/run/docker.sock`. `dockerImageYamlFilename` is the name of the YAML descriptor in the docker file to identify the container (default: `image-info.yml`). `deleteWhenUndeployed` tells the manager whether containers shall be deleted when the are undployed (default: `false`).