# oktoflow platform: Components overview

![Architecture Overview](ArchitectureOverview.png)

The following layers and components are available in the oktoflow platform:
* Managed [Platform dependencies](platformDependencies/README.md) (parent POM)
* [Support Layer](support/README.md) (with links to contained parts)
    * [Asset Administration Shell (AAS) abstraction](support/support.aas.basyx/README.md) with Visitor, communication protocol support and useful recipes for deployment and I/O. Further, extensible identity management and semantic id resolution.
    * [Default Basyx AAS client abstraction](support/support.aas.basxy/README.md) implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * [Default Basyx AAS server abstraction](support/support.aas.basxy.server/README.md) implementation for [Eclipse Basyx](https://www.eclipse.org/basyx/)
    * [AAS support](support/support.iip-aas/README.md) functionality for the oktoflow platform, including extensible mechanisms for uniform device ids.
* Transport Layer
    * [Transport component](transport/README.md) (with links to contained parts)
         * Transport connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * Optional [transport support](transport/transport.spring/README.md) for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream) (with links to contained parts)
         * Transport connector binder for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
         * Transport connector binder for AMQP based on [RabbitMQ](https://www.rabbitmq.com/)
    * External [connectors component](connectors/README.md) (with links to contained parts)
       * Platform connector for OPC UA v1 based on [Eclipse Milo](https://projects.eclipse.org/projects/iot.milo)
       * Platform connector for AAS based on the abstraction in the support layer
       * Platform connector for MQTT v3 and v5 based on [Eclipse Paho](https://www.eclipse.org/paho/)
 * [Services](platform/services/README.md) 
    * Basic platform [service management](services/services/README.md) 
    * Default [service management](services/services.spring/README.md) for Spring Cloud Streams
    * Multi-language [service execution environment](services/services.environment/README.md) for Java and Python (connected through protocols of the Support Layer)
 * [Resource management](resources/README.md) and Monitoring
    * [Edge Cloud Server (ECS) runtime](resources/ecsRuntime/README.md)
    * Default resource management for [Docker](resources/ecsRuntime.docker/README.md)
    * [Device management](resources/deviceMgt/README.md) with MinIO, S3Mock and ThingsBoard integrations.
 * [Security and Data Protection](securityDataProtection/README.md)
    * [KIPROTECT](https://kiprotect.com/) [KODEX](https://heykodex.com/) [integration](securityDataProtection/security.services.kodex/README.md)
 * [Reusable Intelligent Services](reusableIntelligentServices/README.md)
    * Basic AI and data processing [functions](reusableIntelligentServices/kiServices.functions/README.md).
    * [RapidMiner](https://rapidminer.com) Real-Time Scoring Agent (RTSA) for [AI components and processes](reusableIntelligentServices/kiServices.rapidminer.rtsa/README.md).
 * [Configuration](configuration/README.md)
    * IVML platform [configuration](configuration/configuration/README.md)
    * IVML platform [default library](configuration/configuration.defaultLib/README.md)
 * [Management UI](managementUI/README.md)
 * [Tools](tools/README.md) including implementation templates, Python Maven plugins and extended Maven dependency plugin for model updates.

Released components are made available via [Maven Central](https://search.maven.org/search?q=iip-ecosphere) and example installations in terms of Docker Containers via [Docker Hub](https://hub.docker.com/r/iipecosphere/platform).

![oktoflow](oktoflow.png)

__The main page was moved one level up to the new [platform main page](../README.md).__