# IIP-Ecosphere platform: Resources

Interfaces (SPI) and alternative implementations for managing containers and services on compute resources.

* Generic [ECS (Edge-Cloud-Server) runtime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime/README.md)
    * [Docker-based container manager](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime.docker/README.md)
    * [Kubernetes-based container manager](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime.kubernetes/README.md) in development!
* [Device management](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/deviceMgt/README.md) based on the BSc of Dennis Pidun.
     * [S3Mock](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/deviceMgt.s3mock/README.md) S3-Connector for configuration and binary image storage. Can optionally start a server instance.
     * [MinIO](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/deviceMgt.minio/README.md) S3-Connector for configuration and binary image storage (AGPL).
     * [In-Memory Device Registry](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/deviceMgt.basicRegistry/README.md) for devices represented as ECS runtime.
     * [ThingsBoard-Connector](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/deviceMgt.thingsboard/README.md) for devices represented as ECS runtime. The ThingsBoard UI is currently not further integrated.
* [Central platform monitoring](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/monitoring/README.md) basic platform component
   * [Prometheus-based platform monitoring](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/monitoring.prometheus/README.md) The Prometheus UI is currently not further integrated.
