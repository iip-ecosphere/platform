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
      prometheus
        server:
          schema: <HTTP|HTTPS>
          host: <String>
          port: <int>
          running: <Boolean>
        exporter:
          schema: <HTTP|HTTPS>
          host: <String>
          port: <int>
          running: <Boolean>
        alertMgr
          schema: <HTTP|HTTPS>
          host: <String>
          port: <int>
          running: <Boolean>
        scrapeInterval: <int>
        scrapeTimeout: <int>
        evaluationInterval: <int>
        
The fields are akin to [EcsRuntime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime). `prometheusServer` defines the network address where the server shall be running (then `host` is implicitly `localhost`) or where it is installed (default values: schema `HTTP`, host `localhost`, port `9090`). For local prometheus instances, `running` shall be `false` (the default), for installed and running installations this value shall be `true`. Similarly for the platform-sided `exporter` (default for port is `-1` meaning an emphereral port) and the `alertMgr`. `scrapeInterval` defines how often metrics from the `exporter` shall be read and `scrapeTimeout` (smaller than `scrapeInterval`) when a timeout for accessing a scrape point shall be assumed. Similarly, `evaluationInterval` defines the rule evaluation interval (defaults: `scrapeInterval` 1000ms, `scrapeTimeout` 1000ms, `evaluationInterval` 3000ms)

## Lifecycle Profiles

This component defines three lifecycle profiles

- `monitoring.prometheus`: start all parts in a separate JVM
- `prometheus`: start only the platform-supplied prometheus in a separate JVM
- `prometheus.exporter`: start only the platform-side, the exporter and alert manager
## Missing / Known Problems
- Configuration integration including rules for alerts
- Monitoring AAS with monitored values
- Changing scraping configuration on a remote machine
- Incompatibility with Apache QPID Broker-J (bug reported), lifecycle profiles
