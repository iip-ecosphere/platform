# IIP-Ecosphere platform

The platform component for configuring and starting platform services. The platform component consists of the AAS abstraction as well as the abstract services and the abstract ECSruntime components for the AAS clients. Currently, this component just provides a command line client as basic interfaces to platform functionality.

## Configuration

The basic YAML configuration of the platform services (in ``iipecosphere.yml``) provide the following settings:

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
        netmask: <String>
        port: <int>
        protocol: <VAB-TCP|>
      persistence: <INMEMORY|MONGO>
    
The `aas` settings are similar to [ECS (Edge-Cloud-Server) runtime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime/README.md), while `mode` is ignored/fixed to `REMOTE_DEPLOY`. `aas:persistence` defines the AAS persistence mechanism and may require further software installation, e.g. MongoDB.

## Running

This component must be bundled with further, e.g., upper layer components which pair themselves via JLS, in particular `LifecycleHandler`. To start this component, please use the functions of the `LifecycleHandler` or the default starter classes defined there.

## Missing
- AAS discovery, currently we rely on the full IP specification instantiated through the configuration