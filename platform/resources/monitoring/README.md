# IIP-Ecosphere platform: central resource monitoring

Component to perform central resource monitoring. This is a generic component, which requires respective extensions, e.g., monitoring.prometheus.

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
      transport:
        host: <String>
        port: <int>
        authenticationKey: <String>
        keystoreKey: <String>
        keyAlias: <String>
        hostnameVerification: <Boolean>
        gatewayPort: <int>
        netmask: <String>

The fields are akin to [EcsRuntime](../ecsRuntime/README.MD).
