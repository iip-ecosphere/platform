# Generic Service and Management and Control of the IIP-Ecosphere platform

The basic service and service management interfaces of the IIP-Ecosphere platform. Also provides the AAS of the 
services, including

* Artifacts containing the services.
* Services descriptors for services and their typed connectors (after artifacts are added and before they are removed) including the dynamic service state.
* Relations among the services while services are alive.
* Contributions to the device resources in terms of operations to manage services, e.g., start/stop services.

Contains a startup program for running the service manager in standalone mode, e.g., in a services container. The
setup Requires service management implementation and AAS implementation to be hooked in properly via JSL. Depending on 
the setup, also requires a proper setup of the network manager via JSL. Intended to be called from a separate project 
with adequate dependencies.

## Configuration

The basic YAML configuration of the services management allows for the following AAS (asset administration shell) 
settings, however, dependent on the implementation component, it might be indented and nested into the configuration 
tag of the implementation component:

    aas:
      server:
        schema: <HTTP|HTTPS>
        host: <String>
        port: <int>
        path: <String>
      mode: <REMOTE_DEPLOY|REGISTER>
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
        protocol: <String>

* The `server` defines the setup of the main AAS server, i.e., the (URL) connection schema, the host name, the port number and the endpoint path on that server. The port number may be negative indicating any free (ephemerial) port, but then host shall typically be `localhost`. By default, the schema is `HTTP`, the host is `localhost`, the port  is `8080` and the path is empty.
* The `mode` defines whether a local server shall be powered up (`REGISTER`, host in `server` ignored) and the created AAS shall be registered with the `registry`, or whether the created AAS shall be deployed to `server` remotely and registered with `registry` (`REMOTE_DEPLOY`). Default is `REMOTE_DEPLOY`.
* The `registry` defines the setup of the AAS registry, i.e., the server instance knowing all existing AAS and submodels, their names and uniform resource names. The entries are similar to `server`. By default, the schema  is `HTTP`, the host is `localhost`, the port is `8080` and the path is `registry`, i.e., the default registry is the AAS server, but operating on a specific endpoint path.
* The `implementation` is the server counterpart for dynamic/active AAS providing actual property values and serving AAS operation requests. Similar to the entries above, the implementation server has a schema (just for illustrative purposes), a host name, a port (for negative ports see above) and a protocol (from `AasFactory`, e.g. empty for the default protocol `VAB`). By default, the schema is `TCP`, the host is `localhost`, the port is `9000` and the protocol is empty (i.e., the default protocol of the `AasFactory`). As typically a server instance shall be created and communicated to potential callers, the we turn `127.0.0.1` as the numerical representation of `localhost` into an IP address of the device. As a device may have multiple IP addresses and automatic selection may be desired, we consider `netmask` as a filter to select the desired IP address. `netmask` can either be given as a decimal netmask or as a Java regular expression over IP addresses. If `netmask` is empty, any IP address of the device is selected if available. Further, if the port number is invalid, e.g., negative, we turn it into an ephemeral port.

## Running

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

The command line argument `--iip.port=<int>` allows overriding the AAS implementation server port, i.e., in the Yaml above `aas:implementation:port`.

## Missing
* ECS Monitoring
* AAS discovery, currently we rely on the full IP specification instantiated through the configuration
