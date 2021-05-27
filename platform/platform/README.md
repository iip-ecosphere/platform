# IIP-Ecosphere platform

The platform component for configuring and starting platform services.

## Configuration

The basic YAML configuration of the plattform services provide the following settings:

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
      persistence: <INMEMORY|MONGO>
    
The `aas` settings are similar to [ECS (Edge-Cloud-Server) runtime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime/README.md). `aasPersistence` 
