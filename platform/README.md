# IIP-Ecosphere platform

This is for all code realizing the IIP-Ecosphere platform consisting of:
* Managed [Platform dependencies](https://github.com/iip-ecosphere/platform/tree/main/platform/platformDependencies/README.md) (parent POM)
* [Support Layer](https://github.com/iip-ecosphere/platform/tree/main/platform/support/README.md) (with links to contained parts)
    * [Asset Administration Shell (AAS) abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support.aas.basyx/README.md) with Visitor, communication protocol support and useful recipes for deployment and I/O
    * [Default Basyx AAS client abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support.aas.basxy/README.md) implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * [Default Basyx AAS server abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support.aas.basxy/README.md) implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * [AAS support](https://github.com/iip-ecosphere/platform/tree/main/platform/support.iip-aas/README.md) functionality for the IIP-Ecosphere platform
* Transport Layer
    * [Transport component](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/README.md) (with links to contained parts)
         * Transport connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Optional [transport support](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/README.md) for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream) (with links to contained parts)
         * Transport connector binder for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector binder for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Platform/Machine [connectors component](https://github.com/iip-ecosphere/platform/tree/main/platform/connectors/README.md) (with links to contained parts)
       * Platform connector for OPC UA v1 based on [Eclipse Milo](https://projects.eclipse.org/projects/iot.milo)
       * Platform connector for AAS based on the abstraction in the support layer
       * Platform connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
 * [Services](https://github.com/iip-ecosphere/platform/tree/main/platform/services/README.md) 
    * Basic platform [service management](https://github.com/iip-ecosphere/platform/tree/main/platform/services/services/README.md) 
    * Default [service management](https://github.com/iip-ecosphere/platform/tree/main/platform/services/services.spring/README.md) for Spring Cloud Streams
    * Multi-language [service execution environment](https://github.com/iip-ecosphere/platform/tree/main/platform/services/services.execution/README.md) for Java and Python (connected through protocols of the Support Layer)
 * [Resource management](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/README.md) and Monitoring
    * [Edge Cloud Server (ECS) runtime](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime/README.md)
    * Default resource management for [Docker](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/ecsRuntime.docker/README.md)
   * Upcoming: Resource management for Kubernetes
 * Security and Data Protection
 * Reusable Intelligent Services
 * Configuration
    * IVML platform [configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration/README.md)
    * resource optimization
    * adaptation

For detailed documentation and development hints see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md).

For a documentation of the releases of the IIP-Ecosphere platform see [releases overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/RELEASES.md).