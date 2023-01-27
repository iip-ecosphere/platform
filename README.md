# IIP-Ecosphere AI-enabled Industry 4.0/IIoT platform 

The virtual IIP-Ecosphere Industry 4.0/IIoT platform aims at demonstrating new approaches to vendor-independent, interoperable and open platform concepts for easy-to-use AI. In particular, it aims at uniform, distributed deployment of services to heterogeneous (edge) devices, an AI toolkit for the industrial production, consistent configuration support ranging from network to applications as well as the use and application of established and upcoming IIoT standards, e.g., asset administration shells (to be used for all platform interfaces).

## News

* **New in 2023:** IIP-Ecosphere demonstrators from the [IIP-Ecosphere innovation idea competition](https://www.iip-ecosphere.de/ideenwettbewerb/). Stay tuned - also external components by new IIP-Ecosystem partners may be introduced.
* **January 2023:** IIP-Ecosphere platform now relies on JDK 11 and BaSyx 1.3.0.
* **In preparation:** Development container for service implemenation and (video) material from service integration workshop.
* **In development:** Distributed testing/evaluation environment for platform and applications
* Improved build process integration: Python "compile"/test, platform instantiator and Maven orchestrator build plugins for simplified platform and application build processes
* Improved Python code integration: More Pythonic data type generation and Eclipse Editor support for PyDev.

## Public demonstrations

* Successful presentation of improved robot-based platform demonstrator at [TddT'22, Berlin](https://www.digitale-technologien.de/DT/Redaktion/DE/Veranstaltungen/2022/DT/220829_Tage_der_digitalen_Technologien.html) 
* Successful presentation of robot-based visual quality inspection app on [HM'22](https://www.hannovermesse.de/de/) (with 3 sources, Python-based AI, AAS-based Angular application).

## Overview

![Architecture Overview](platform/ArchitectureOverview.png)

For detailed links to the individual components, please refer to the [platform](https://github.com/iip-ecosphere/platform/tree/main/platform/README.md) overview. The platform repository also contains a growing set of [examples and demonstration use cases](https://github.com/iip-ecosphere/platform/tree/main/platform/examples/README.md).

Further, [compliance tests](https://github.com/iip-ecosphere/platform/tree/main/compliance/README.md) and benchmarks for interoperability are in planning.

## Documentation

The platform concepts and realization are developed by the IIP-Ecosphere consortium and are documented in several whitepapers:
  * The IIP-Ecosphere [platform handbook](https://doi.org/10.5281/zenodo.7047640) [link may be updated some days after platform releases]
  * The IIP-Ecosphere [platform requirements (functional and quality view)](https://doi.org/10.5281/zenodo.4485774)
  * The IIP-Ecosphere [platform usage view](https://doi.org/10.5281/zenodo.4485801)
  * The IIP-Ecosphere [Industry 4.0 platform overview](https://doi.org/10.5281/zenodo.4485756)

All material is also available from the [IIP-Ecosphere Website](https://www.iip-ecosphere.eu/).

For detailed documentation and development hints see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md). For a documentation of the releases of the IIP-Ecosphere platform see [releases overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/RELEASES.md). For information on using/installing the platform, please consult the [installation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/INSTALL.md). 

## Building the Platform

The platform consists of some core and many alternative components which can be built locally via the provided Maven multi-module POM. However, to save time, we recommend relying on the pre-built snapshot and release binaries in [Maven Central](https://search.maven.org/search?q=iip-ecosphere) from our Continuous Integration server. To speed up this process when changes to the individual modules occur, the build sequence is incremental and not based on the Maven multi-module build. So we recommend that you install Java, Maven and an IDE (supported/working versions see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md)) and let Maven resolve the actual binaries for you. For more detailed information on the build dependencies, please refer to the platform handbook.

To ease the first steps, we provided Demonstration and Development Docker Containers on [Docker Hub](https://hub.docker.com/r/iipecosphere/platform).

![IIP-Ecosphere](platform/logo.png)

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

