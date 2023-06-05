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
        keystore: <File>
        keyPassword: <String>
        keyAlias: <String>
      mode: <REMOTE_DEPLOY|REGISTER>
      accessControlAllowOrigin: <String>
      registry:
        schema: <HTTP|HTTPS>
        host: <String>
        port: <int>
        path: <String>
        keystore: <File>
        keyPassword: <String>
        keyAlias: <String>
      implementation:
        schema: <HTTP|HTTPS|TCP|IGNORE>
        host: <String>
        netmask: <String>
        port: <int>
        protocol: <VAB-TCP|VAB-HTTP|VAB-HTTPS>
        keystore: <File>
        keyPassword: <String>
        keyAlias: <String>
    monitoringUpdatePeriod: <int>
    autoOnOffboarding: <boolean>
    artifactInfixes:
      - <String> 
    transport:
        host: <String>
        port: <int>
        authenticationKey: <String>
        keystoreKey: <String>
        keyAlias: <String>
        hostnameVerification: <Boolean>
        gatewayPort: <int>
        netmask: <String>
    netMgr:
        lowPort: <int>
        highPort: <int>
        netmask: <String>

* The `server` defines the setup of the main AAS server, i.e., the (URL) connection schema, the host name, the port number and the endpoint path on that server. By default, the schema is `HTTP`, the host is `localhost`, the port  is `8080` and the path is empty. The port number may be negative indicating any free (ephemerial) port, but then host shall typically be `localhost`. `keystore`, `keyPassword`, and `keyAlias` are optional TLS settings pointing to the keystore file, the alias of the key to use and the key(store) password. Please note that BaSyx currently requires that the alias is `tomcat`.
* The `mode` defines whether a local server shall be powered up (`REGISTER`, host in `server` ignored) and the created AAS shall be registered with the `registry`, or whether the created AAS shall be deployed to `server` remotely and registered with `registry` (`REMOTE_DEPLOY`). Default is `REMOTE_DEPLOY`. 
* `accessControlAllowOrigin` denotes the allowed origin as URL/address, may be `*` for any
* The `registry` defines the setup of the AAS registry, i.e., the server instance knowing all existing AAS and submodels, their names and uniform resource names. The entries are similar to `server`. By default, the schema  is `HTTP`, the host is `localhost`, the port is `8080` and the path is `registry`, i.e., the default registry is the AAS server, but operating on a specific endpoint path. `keystore`, `keyPassword`, and `keyAlias` are optional TLS settings pointing to the keystore file, the alias of the key to use and the key(store) password. Please note that BaSyx currently ignores TLS on the registry.
* The `implementation` is the server counterpart for dynamic/active AAS providing actual property values and serving AAS operation requests. Similar to the entries above, the implementation server has a schema (just for illustrative purposes), a host name, a port (see negative ports above) and a protocol (from `AasFactory`, e.g. empty for the default protocol `VAB-IIP`). By default, the schema is `TCP`, the host is `localhost`, the port is `9000` and the protocol is empty (i.e., the default protocol of the `AasFactory`). As typically a server instance shall be created and communicated to potential callers, the we turn `127.0.0.1` as the numerical representation of `localhost` into an IP address of the device. As a device may have multiple IP addresses and automatic selection may be desired, we consider `netmask` as a filter to select the desired IP address. If `netmask` is empty, any IP address of the device is selected if available. `netmask` can either be given as a decimal netmask or as a Java regular expression over IP addresses. Further, if the port number is invalid, e.g., negative, we turn it into an ephemeral port. `keystore`, `keyPassword`, and `keyAlias` are optional TLS settings pointing to the keystore file, the alias of the key to use and the key(store) password. Please note that BaSyx currently requires that the alias is `tomcat`.
* `monitoringUpdatePeriod` defines a period in ms when internal metrics are updated and reported.
* `autoOnOffboarding` always registers/removes the device to/from the central registry, failures are not reported. This is meant to be a debugging/development behavior rather than a production feature. Default is `true`, but shall be `false` in production settings.
* `artifactInfixes` list of string file name indexes to be tried before a default artifact is loaded, e.g., to indicate device-specific containers.
* `transport` defines the setup of the central transport server/broker. `authenticationKey` points to an entry in the identityStore providing the authentication information for this protocol. If TLS shall be used, the `keystoreKey` delivers the identity to open the associated keystore and `keyAlias` may point to a dedicated key in that keystore. TLS `hostnameVerification` may be explicitly enabled/disabled. Further, depending on context and utilized protocol, `gatewayPort` may indicate a port on `host` to allow for the transport converters to expose certain parts like platform status changes or AppAAS in terms of messages. If `gatewayPort` is negative, a local server may be created with an IP selected according to `netmask`.
* `netMgr` sets up minimum or maximum port for automated ephemeral port assignment. Default range is 1024-65535 according to RFC 6056. The `netmask` has the same semantics as for `implementation`.

## Device AAS

The ECS runtime may create an AAS with nameplate for the actual device. As currently there is no standard where to get the device AAS from, the ECS runtime contains a device AAS provider returning the address of the AAS to use, its URN and its shortId. There must not be an AAS, but various mechanisms could be used to provide one. Two default mechanisms are

* the Yaml provider, reading (a part of) the generic frame nameplate information from nameplate.yaml or *deviceId*.yaml from the classpath and constructs an AAS from that information. Related images must be located in the classpath and referenced in the YAML file.
* the AASX provider, reading device.aasx or *deviceId*.aasx from the classpath and constructs an AAS from that information. Related images/PDFs must be as usual in the AASX file.

Via the JSL for `DeviceAasProviderDescriptor` the actual approach may be determined. By default, we use a first matching device AAS provider searching either for the Yaml or the AASX file mentioned above. Other approaches may be utilized via JSL.

Structure of a Yaml extended nameplate file:

    manufacturerName: <String>
    manufacturerProductDesignation: <String>
    productImage: <Path on Classpath>
    manufacturerLogo: <Path on Classpath>
    address:
        department: <String>
        street: <String>
        zipCode: <String>
        cityTown: <String>
    services:
        - key: <String>
          port: <Integer>
          host: <String>
          netmask: <String>
          version: <Version>
        - ...

A service declaration has a uniqe `key` to be used from the configuration model. It declares the port and the `host` (e.g., if the device has multiple IPs) of the service. The `host` is optional and if not given the actual IP address is used, taking `netmask` as selector into account. A device service may have a version, e.g., for MQTT, which can be taken into account to dynamically select the matching platform connector implementation. 

## Running

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

The command line argument `--iip.port=<int>` allows overriding the AAS implementation server port, i.e., in the Yaml above `aas:implementation:port`.

The `EcsCmdLineLifecycleDescriptor` is optional and shall only be used by real ECS setups, not in importing projects.

## Missing
* AAS discovery, currently we rely on the full IP specification instantiated through the configuration
