# Service and service management interfaces of the IIP-Ecosphere platform

The basic service and service management interfaces of the IIP-Ecosphere platform. Also provides the AAS of the 
services, including

* The artifacts containing the services.
* The individual services and their typed connectors (after artifacts are added and before they are removed) including 
  the dynamic service state.
* The relations among the services while services are alive.
* Contributions to the device resources in terms of operations to manage services, e.g., start/stop services.

Contains a startup program for running the service manager in standalone mode, e.g., in a services container. The
setup Requires service management implementation and AAS implementation to be hooked in properly via JSL. Depending on 
the setup, also requires a proper setup of the network manager via JSL. Intended to be called from a separate project 
with adequate dependencies.

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
        protocol: <String>

* The `server` defines the setup of the main AAS server, i.e., the (URL) connection schema, the host name, the port number and the endpoint path on that server. The port number may be negative indicating any free (ephemerial) port, but then host shall typically be `localhost`. By default, the schema is `HTTP`, the host is `localhost`, the port  is `8080` and the path is empty.
* The `registry` defines the setup of the AAS registry, i.e., the server instance knowing all existing AAS and submodels, their names and uniform resource names. The entries are similar to `server`. By default, the schema  is `HTTP`, the host is `localhost`, the port is `8080` and the path is `registry`, i.e., the default registry is the AAS server, but operating on a specific endpoint path.
* The `implementation` is the server counterpart for dynamic/active AAS providing actual property values and serving AAS operation requests. Similar to the entries above, the implementation server has a schema (just for illustrative purposes), a host name, a port (for negative ports see above) and a protocol (from `AasFactory`, e.g. empty for the default protocol `VAB`). By default, the schema is `TCP`, the host is `localhost`, the port is `9000` and the protocol is empty (i.e., the default protocol of the `AasFactory`). As typically a server instance shall be created and started, usually the hostname is `localhost`.

## Running

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

## Missing
* ECS Monitoring
* AAS discovery, currently we rely on the full IP specification instantiated through the configuration
