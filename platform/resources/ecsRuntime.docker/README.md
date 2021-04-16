# IIP-Ecosphere platform: ECSruntime extension for plain Docker

[Docker](https://www.docker.com/) extension/implementation of the SPI interfaces of the IIP-Ecosphere container management agent (ECSruntime) running on compute resources.

## Configuration

In addition to the basic AAS settings, this extension allows for the following settings:

    dockerHost: <String>
    
The `dockerHost` is an operation system specific string denoting how to connect to docker. For Linux, this is typically `unix:///var/run/docker.sock`.