# ECS runtime of the IIP-Ecosphere platform

Interfaces (SPI) and basic implementation of container and service management agent running on compute resources.

Contains a startup program for running the ECS runtime. The setup requires service management implementation and 
AAS implementation to be hooked in properly via JSL. Depending on the setup, also requires a proper setup of the 
network manager and a service manager. Intended to be called from a separate project with adequate dependencies.

## Configuration

The basic YAML configuration of the services management allows for the following AAS (asset administration shell) 
settings:

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
        port: <int>
        protocol: <VAB-IIP|>

* The `server` defines the setup of the main AAS server, i.e., the (URL) connection schema, the host name, the port number and the endpoint path on that server. By default, the schema is `HTTP`, the host is `localhost`, the port  is `8080` and the path is empty.
* The `registry` defines the setup of the AAS registry, i.e., the server instance knowing all existing AAS and submodels, their names and uniform resource names. The entries are similar to `server`. By default, the schema  is `HTTP`, the host is `localhost`, the port is `8080` and the path is `registry`, i.e., the default registry is the AAS server, but operating on a specific endpoint path.
* The `implementation` is the server counterpart for dynamic/active AAS providing actual property values and serving AAS operation requests. Similar to the entries above, the implementation server has a schema (just for illustrative purposes), a host name, a port and a protocol (from `AasFactory`, e.g. empty for the default protocol `VAB-IIP`). By default, the schema is `TCP`, the host is `localhost`, the port is `9000` and the protocol is empty (i.e., the default protocol of the `AasFactory`). As typically a server instance shall be created and started, usually the hostname is `localhost`.          For convenience, the port number may be invalid and is turned then into an ephemeral port.

## Running

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

## Missing
* ECS Monitoring
* AAS discovery, currently we rely on the full IP specification instantiated through the configuration
