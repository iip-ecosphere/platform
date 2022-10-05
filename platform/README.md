# IIP-Ecosphere platform 

The virtual IIP-Ecosphere Industry 4.0/IIoT platform aims at demonstrating new approaches to vendor-independent, interoperable and open platform concepts for easy-to-use AI. In particular, it aims at uniform, distributed deployment of services to heterogeneous (edge) devices, an AI toolkit for the industrial production, consistent configuration support ranging from network to applications as well as the use and application of established and upcoming IIoT standards, e.g., asset administration shells (to be used for all platform interfaces).

## News

* Developer support: Platform configuration goes AAS, generated implementation templates, transport telegram logging
* Platform release 0.4.0 (2022/09/02)
* AI-based Code for HM'22/TddT'22 as public platform example/regression test.
* Successful presentation of improved robot-based platform demonstrator at [TddT'22, Berlin](https://www.digitale-technologien.de/DT/Redaktion/DE/Veranstaltungen/2022/DT/220829_Tage_der_digitalen_Technologien.html) 
* Successful presentation of robot-based visual quality inspection app on [HM'22](https://www.hannovermesse.de/de/) (with 3 sources, Python-based AI, AAS-based Angular application).

## Documentation

The platform concepts and realization are developed by the IIP-Ecosphere consortium and are documented in several whitepapers:
  * The IIP-Ecosphere [platform handbook](https://doi.org/10.5281/zenodo.7047640) [link may be updated some days after platform releases]
  * The IIP-Ecosphere [platform requirements (functional and quality view)](https://doi.org/10.5281/zenodo.4485774)
  * The IIP-Ecosphere [platform usage view](https://doi.org/10.5281/zenodo.4485801)
  * The IIP-Ecosphere [Industry 4.0 platform overview](https://doi.org/10.5281/zenodo.4485756)

All material is also available from the [IIP-Ecosphere Website](https://www.iip-ecosphere.eu/).

For detailed documentation and development hints see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md). For a documentation of the releases of the IIP-Ecosphere platform see [releases overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/RELEASES.md). For information on using/installing the platform, please consult the [installation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/INSTALL.md). 

We also prepare a set of [examples and demonstration use cases](https://github.com/iip-ecosphere/platform/tree/main/platform/examples/README.md).

## Building the Platform

The platform consists of some core and many alternative components. We use a Continuous Integration server to build the platform upon changes (snapshot builds) and releases. To speed up this process, the build sequence is incremental and (currently) not a Maven module build. So we recommend that you install Java, Maven and an IDE (supported/working versions see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md)) and let Maven resolve the actual binaries for you. Prerequisite is the installation of the actual platform dependencies (see [installation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/INSTALL.md)). For more detailed information on the build dependencies, please refer to the platform handbook.

## Available components

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
    

Released components are made available via [Maven Central](https://search.maven.org/search?q=iip-ecosphere) and example installations in terms of Docker Containers via [Docker Hub](https://hub.docker.com/r/iipecosphere/platform).

![IIP-Ecosphere](logo.png)



Powered by (selection of used tools/frameworks)

<a href="https://www.eclipse.org/basyx"><img src="https://www.eclipse.org/basyx/img/basyxlogo.png" alt="BaSyx" width="200"/></a> 
<a href="https://iot.eclipse.org"><img src="https://iot.eclipse.org/iot-logo-social.png" alt="Eclipse IoT" width="100"/></a> 
<a href="https://www.docker.com"><img src="https://www.docker.com/wp-content/uploads/2022/05/Docker_Temporary_Image_Google_Blue_1080x1080_v1.png" alt="Docker" width="100"/></a> 
<a href="https://prometheus.io"><img src="https://prometheus.io/assets/favicons/android-chrome-192x192.png" alt="Prometheus Monitoring" width="100"/></a>
<br/>

<a href="https://spring.io"><img src="https://spring.io/images/spring-logo-9146a4d3298760c2e7e49595184e1975.svg" alt="Spring Cloud Stream" width="200"/></a> 
<a href="https://maven.apache.org"><img src="https://maven.apache.org/images/maven-logo-black-on-white.png" alt="Maven" width="200"/></a> 
<a href="https://sse.uni-hildesheim.de"><img src="https://sse.uni-hildesheim.de/media/_migrated/pics/EASy_Logo_02.png" alt="EASy-Producer" width="100"/></a> 

IIP-Ecosphere would like to thank all utilized technologies and frameworks for the helpful contributions to the Open Source community.
