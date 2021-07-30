# IIP-Ecosphere platform: ECSruntime extension for plain Docker

[Docker](https://www.docker.com/) extension/implementation of the SPI interfaces of the IIP-Ecosphere container management agent (ECSruntime) running on compute resources.

## Container descriptor

The container descriptor is an additional file in a docker image identifying the container. Typically, the file is called `ìmage-info.yml` (see below for a different name). The remainder settings look like

    id: <String>
    name: <String>
    version: <Version>
    dockerImageName: <String>
    dockerImageZipfile: <String>
    env: 
      - <String>
    exposedPorts:
      - port: <String>
        protocol: <DEFAULT|TCP|UDP|SCTP>
    attachStdIn: <Boolean>
    attachStdOut: <Boolean>
    attachStdErr: <Boolean>
    privileged: <Boolean>
    withTty: <Boolean>
    dood: <Boolean>
    networkMode: <host|bridge|none|container:name/id>

The first line indicates the internal descriptor used to represent the date for the manager. `id`is the identifier the container can uniquely addressed via the IIP-Ecosphere interfaces. This identifier is determined by the platform and instantiated into that file. Similarly, the `name` is the descriptive name of the container and `version` is the version number of the container in terms of IIP-Ecosphere version numbers (shall be the same as given in `-t` when creating the image). A container image is added to the container manager via its (download) URI. The URI points to the container descriptor (or the default descriptor name is added if it is just a path, see below). `dockerImageZipfile` is the name of the compressed container file in the same URI location as the descriptor. `dockerImageName` is the name (as used with the `-t` option of Docker when creating the container) of the image in `dockerImageZipfile` used to address/start the container.

The `env` contains the environment settings to be passed to the container during creation/startup. May be single names to be propagated from the system environment or `<key>=<value>`. `${port}` will be replaced by the dynamic port intended for the AAS implementation server). Exposed ports lists the ports that must be accessible in the container. For service manager containers, `env` typically contains `IIP_PORT=${port}` and the exposed ports `port: ${port} protocol:TCP`.

`attachStdIn`, `attachStdOut`, `attachStdErr` attach the respective streams. `withTty` opens a terminal and `privileged` runs the container in privileged mode. `dood` allows the container to control the outside docker (docker-out-of-docker), i.e., maps the `dockerHost` between container and host (see below) and enables `attachStdId`, `withTty` and `privileged`. The `networkMode` directly defines the Docker network mode.


## Configuration

In addition to the basic AAS settings, this extension allows for the following settings:

    docker:
        dockerHost: <String>
        dockerImageYamlFilename: <String>
        deleteWhenUndeployed: <Boolean>
        downloadDirectory: <String>
    
The `dockerHost` is an operation system specific string denoting how to connect to docker. For Linux, this is typically `unix:///var/run/docker.sock`. `dockerImageYamlFilename` is the default name of the YAML container descriptor, which is appended to a given URI, when it is added to the container manager and the file name is missing (default: `image-info.yml`). `deleteWhenUndeployed` tells the manager whether containers shall be deleted when the are undployed (default: `false`). `downloadDirectory`is the directory where descriptors and containers shall be downloaded to by the container manager (default: empty, interpreted as system temporary directory).