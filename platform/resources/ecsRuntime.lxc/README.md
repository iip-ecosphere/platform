# IIP-Ecosphere platform: ECSruntime extension for LXC

** IMPLEMENTATION TEMPLATE - NOT FUNCTIONAL **

LXC extension/implementation of the SPI interfaces of the IIP-Ecosphere container management agent (ECSruntime) running on compute resources.

## Container descriptor

The container descriptor is an additional file for identifying the container. Typically, the file is called `ìmage-info.yml` (see below for a different name). The remainder settings look like

    id: <String>
    name: <String>
    lxcImageAlias: <String>
    lxcZip: <String>
    env: 
      - <String>
    exposedPorts:
      - port: <String>
        protocol: <DEFAULT|TCP|UDP|SCTP>
    networkMode: <host|bridge|none|container:name/id>

The first line indicates the internal descriptor used to represent the date for the manager. `id`is the identifier the container can uniquely addressed via the IIP-Ecosphere interfaces used to start/stop the container. This identifier is determined by the platform and instantiated into that file. Similarly, the `name` is the descriptive name of the container. A container image is added to the container manager via its (download) repository. The repository points to the container descriptor (or the default descriptor name is added if it is just a path, see below). `lxcZip` is the name of the compressed image file in a given location. `lxcImageAlias` is the combination of the name of the base image and the repository used to create the container.

The `env` contains the environment settings to be passed to the container during creation/startup. May be single names to be propagated from the system environment or `<key>=<value>`. `${port}` will be replaced by the dynamic port intended for the AAS implementation server). Exposed ports lists the ports that must be accessible in the container. For service manager containers, `env` typically contains `IIP_PORT=${port}` and the exposed ports `port: ${port} protocol:TCP`.


## Configuration

In addition to the basic AAS settings, this extension allows for the following settings:

    lxc:
        lxcHost: <String>
        lxcPort: <String>
        lxcImageYamlFilename: <String>
        downloadDirectory: <String>
    
The `dockerHost` is an operation system specific string denoting how to connect to docker. For Linux based systems it is `localhost:8443`. The `lxcImageYamlFilename` denotes the name of the YAML container descriptor and is appended to a given URI, when it is added to the container manager and the file name is missing (default: `image-info.yml`). `downloadDirectory`is the directory where descriptors and containers shall be downloaded to by the container manager (default: empty, interpreted as system temporary directory).

## Ubuntu Installation

To be able to run the ecsRuntime LXC some things have to be considered during the installation of LXD.

1. To install LXD packages use snap and do a `sudo snap install lxd`. 
2. After the installation is finished immediately do a `lxd init`. 
You will be prompted to enter some settings. Leave everything default except option `local network-bridge`, set it so `No` and enter the systems network-interface and IP address. Also for `Available over network` set it to `Yes`. 3. Once all settings are made first do `lxc config set core.https_address [::]:8443` and than
`lxc config set core.trust_password <password>` to set LXD up for remote connections. 
4. Last step to generate the needed certificates to use the API do `lxc remote add <remote-server name> https://localhost:8443` where the server name can be anything and the address and port point to localhost. The address part can be changed to set up LXD-Remote-Server and connect to other LXD-Systems.

After all these steps LXD should be ready to go. Check the directory `/home/user/snap/lxd/common/config` for the `client.crt` and `client.key`. Those files are necessary for the runtime to interact with the API.