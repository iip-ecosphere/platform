# oktoflow platform: Resources Component

Interfaces (SPI) and alternative implementations for managing containers and services on compute resources.

* Generic [ECS (Edge-Cloud-Server) runtime](ecsRuntime/README.md)
    * [Docker-based container manager](ecsRuntime.docker/README.md)
    * [Kubernetes-based container manager](ecsRuntime.kubernetes/README.md) in development!
* [Device management](deviceMgt/README.md) based on the BSc of Dennis Pidun.
     * [S3Mock](deviceMgt.s3mock/README.md) S3-Connector for configuration and binary image storage. Can optionally start a server instance.
     * [MinIO](deviceMgt.minio/README.md) S3-Connector for configuration and binary image storage (AGPL).
     * [In-Memory Device Registry](deviceMgt.basicRegistry/README.md) for devices represented as ECS runtime.
     * [ThingsBoard-Connector](deviceMgt.thingsboard/README.md) for devices represented as ECS runtime. The ThingsBoard UI is currently not further integrated.
* [Central platform monitoring](monitoring/README.md) basic platform component
   * [Prometheus-based platform monitoring](monitoring.prometheus/README.md) The Prometheus UI is currently not further integrated.
