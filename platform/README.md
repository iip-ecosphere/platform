# IIP-Ecosphere platform

This is for all code realizing the IIP-Ecosphere platform consisting of:
* Managed [Platform dependencies](https://github.com/iip-ecosphere/platform/tree/main/platform/platformDependencies/README.md) (parent POM)
* [Support Layer](https://github.com/iip-ecosphere/platform/tree/main/platform/support/README.md) (with links to contained parts)
    * Asset Administration Shell (AAS) abstraction with Visitor, communication protocol support and useful recipes for deployment and I/O
    * Default AAS abstration implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * Additional AAS support functionality for the IIP-Ecosphere platform
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
 * Services
    * [Service management](https://github.com/iip-ecosphere/platform/tree/main/platform/services/README.md)
 * Resources and Monitoring
    * [Resource management](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/README.md)
 * Security and Data Protection
 * Reusable Intelligent Services
 * Configuration
    * IVML platform [configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration/README.md)
    * resource optimization
    * adaptation

For detailed documentation and development hints see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md).

For a documentation of the releases of the IIP-Ecosphere platform see [releases overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/RELEASES.md).