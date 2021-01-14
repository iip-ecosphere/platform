# IIP-Ecosphere platform

This is for all code realizing the IIP-Ecosphere platform. So far present:
* Managed Platform dependencies (parent POM)
* Support Layer
    * Asset Administration Shell (AAS) abstraction with Visitor, communication protocol support and useful recipes for deployment and I/O
    * Default AAS abstration implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * Additional AAS support functionality for the IIP-Ecosphere platform
* Transport Layer
    * Transport component
         * Transport connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Optional transport support for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream)
         * Transport connector binder for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector binder for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Platform/Machine connectors component
       * Platform connector for OPC UA v1 based on [Eclipse Milo](https://projects.eclipse.org/projects/iot.milo)
       * Platform connector for AAS based on the abstraction in the support layer
       * Platform connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
 * Services (starting)

More to come soon with next focus on edge/stream performance, configuration integration and edge deployment. 

For detailed documentation and development hints see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md).