# IIP-Ecosphere platform components overview

![Architecture Overview](ArchitectureOverview.png)

The following layers and components of the IIP-Ecosphere are available in this platform:
* Managed [Platform dependencies](https://github.com/iip-ecosphere/platform/tree/main/platform/platformDependencies/README.md) (parent POM)
* [Support Layer](https://github.com/iip-ecosphere/platform/tree/main/platform/support/README.md) (with links to contained parts)
    * [Asset Administration Shell (AAS) abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support.aas.basyx/README.md) with Visitor, communication protocol support and useful recipes for deployment and I/O. Further, extensible identity management and semantic id resolution.
    * [Default Basyx AAS client abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support.aas.basxy/README.md) implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * [Default Basyx AAS server abstraction](https://github.com/iip-ecosphere/platform/tree/main/platform/support.aas.basxy/README.md) implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * [AAS support](https://github.com/iip-ecosphere/platform/tree/main/platform/support.iip-aas/README.md) functionality for the IIP-Ecosphere platform, including extensible mechanisms for uniform device ids.
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
    * [Device management](https://github.com/iip-ecosphere/platform/tree/main/platform/resources/deviceMgt/README.md) with MinIO, S3Mock and ThingsBoard integrations.
 * [Security and Data Protection](https://github.com/iip-ecosphere/platform/tree/main/platform/securityDataProtection/README.md)
    * [KIPROTECT](https://kiprotect.com/) [KODEX](https://heykodex.com/) [integration](https://github.com/iip-ecosphere/platform/tree/main/platform/securityDataProtection/security.services.kodex/README.md)
 * [Reusable Intelligent Services](https://github.com/iip-ecosphere/platform/tree/main/platform/reusableIntelligentServices/README.md)
    * Basic AI and data processing [functions](https://github.com/iip-ecosphere/platform/tree/main/platform/reusableIntelligentServices/kiServices.functions/README.md).
    * [RapidMiner](https://rapidminer.com) Real-Time Scoring Agent (RTSA) for [AI components and processes](https://github.com/iip-ecosphere/platform/tree/main/platform/reusableIntelligentServices/kiServices.rapidminer.rtsa/README.md).
 * Configuration
    * IVML platform [configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration/README.md)
    * Resource optimization
    * Adaptation
 * [Management UI](https://github.com/iip-ecosphere/platform/tree/main/platform/managementUI/README.md)
 * [Tools](https://github.com/iip-ecosphere/platform/tree/main/platform/tools/README.md) including implementation templates, Python Maven plugins and extended Maven dependency plugin for model updates.

Released components are made available via [Maven Central](https://search.maven.org/search?q=iip-ecosphere) and example installations in terms of Docker Containers via [Docker Hub](https://hub.docker.com/r/iipecosphere/platform).

![IIP-Ecosphere](logo.png)

__The main page was moved one level up to the new [platform main page](../README.md).__