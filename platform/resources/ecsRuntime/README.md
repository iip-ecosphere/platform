# ECS runtime of the IIP-Ecosphere platform

Interfaces (SPI) and basic implementation of container and service management agent running on compute resources.

Contains a startup program for running the ECS runtime. The setup requires service management implementation and 
AAS implementation to be hooked in properly via JSL. Depending on the setup, also requires a proper setup of the 
network manager and a service manager. Intended to be called from a separate project with adequate dependencies.

## Configuration

The basic YAML configuration of the services management (in ``iipecosphere.yml``) allows for the following AAS (asset administration shell) settings:

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
        protocol: <VAB-IIP|>
    monitoringUpdatePeriod: <int>
    transport:
        host: <String>
        port: <int>
        password: <String>
        user: <String>
    netMgr:
        lowPort: <int>
        highPort: <int>

* The `server` defines the setup of the main AAS server, i.e., the (URL) connection schema, the host name, the port number and the endpoint path on that server. By default, the schema is `HTTP`, the host is `localhost`, the port  is `8080` and the path is empty. The port number may be negative indicating any free (ephemerial) port, but then host shall typically be `localhost`.
* The `mode` defines whether a local server shall be powered up (`REGISTER`, host in `server` ignored) and the created AAS shall be registered with the `registry`, or whether the created AAS shall be deployed to `server` remotely and registered with `registry` (`REMOTE_DEPLOY`). Default is `REMOTE_DEPLOY`.
* The `registry` defines the setup of the AAS registry, i.e., the server instance knowing all existing AAS and submodels, their names and uniform resource names. The entries are similar to `server`. By default, the schema  is `HTTP`, the host is `localhost`, the port is `8080` and the path is `registry`, i.e., the default registry is the AAS server, but operating on a specific endpoint path. 
* The `implementation` is the server counterpart for dynamic/active AAS providing actual property values and serving AAS operation requests. Similar to the entries above, the implementation server has a schema (just for illustrative purposes), a host name, a port (see negative ports above) and a protocol (from `AasFactory`, e.g. empty for the default protocol `VAB-IIP`). By default, the schema is `TCP`, the host is `localhost`, the port is `9000` and the protocol is empty (i.e., the default protocol of the `AasFactory`). As typically a server instance shall be created and communicated to potential callers, the we turn `127.0.0.1` as the numerical representation of `localhost` into an IP address of the device. As a device may have multiple IP addresses and automatic selection may be desired, we consider `netmask` as a filter to select the desired IP address. If `netmask` is empty, any IP address of the device is selected if available. `netmask` can either be given as a decimal netmask or as a Java regular expression over IP addresses. Further, if the port number is invalid, e.g., negative, we turn it into an ephemeral port.
* `monitoringUpdatePeriod` defines a period in ms when internal metrics are updated and reported.
* `transport` defines the setup of the central transport server/broker. `password` and `user` are preliminary and may be removed in future versions.
* `netMgr` sets up minimum or maximum port for automated ephemeral port assignment. Default range is 1024-65535 according to RFC 6056.

## Running

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

The command line argument `--iip.port=<int>` allows overriding the AAS implementation server port, i.e., in the Yaml above `aas:implementation:port`.

## Missing
* ECS Monitoring
* AAS discovery, currently we rely on the full IP specification instantiated through the configuration
