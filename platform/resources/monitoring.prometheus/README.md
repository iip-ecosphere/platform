# IIP-Ecosphere platform: central resource monitoring (Prometheus)

Integration of Prometheus as central resource monitoring component. Receives Meter and status information via Transport  and exports that information to Prometheus. This component ships with Windows/Linux binaries for prometheus that can be started during the platform lifecycle.

## Setup

As usual, this component supports a YAML-based setup:

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
      transport:
        host: <String>
        port: <int>
        password: <String>
        user: <String>
      prometheusServer:
        schema: <HTTP|HTTPS>
        host: <String>
        port: <int>
        running: <Boolean>
      prometheusExporterPort: <int>
        
The fields are akin to [EcsRuntime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime). `prometheusServer` defines the network address where the server shall be running (then `host` is implicitly `localhost`) or where it is installed (default values: schema `HTTP`, host `localhost`, port `9090`). For local prometheus instances, `running` shall be `false` (the default), for installed and running installations this value shall be `true`. `exporterPort` is the local port where an HTTP server will be started and maintained for Prometheus scraping (default is `-1` meaning an emphereral port, may be a concrete port). 

## Missing / Known Problems
- Configuration integration including rules for alerts
- Monitoring AAS
- Changing scraping configuration on a remote machine
- Incompatibility with Apache QPID Broker-J (bug reported)
