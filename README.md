# IIP-Ecosphere AI-enabled Industry 4.0/IIoT platform 

The virtual IIP-Ecosphere Industry 4.0/IIoT platform aims at demonstrating new approaches to vendor-independent, interoperable and open platform concepts for easy-to-use AI. In particular, it aims at uniform, distributed deployment of services to heterogeneous (edge) devices, an AI toolkit for the industrial production, consistent configuration support ranging from network to applications as well as the use and application of established and upcoming IIoT standards, e.g., asset administration shells (to be used for all platform interfaces).

## News

* Preparing an enhanced demonstrator for [EMO 2023](https://emo-hannover.de/), also planning for an integration with [UMATI](https://umati.org/) and sensor technology form [MIP technology](https://mip-technology.de/de/). There will also be the IIP-Ecosphere [symposium as closing event](https://symposium.iip-ecosphere.de/) for the project on 2023/09/19. 
* Default library for code based on types or devices defined in the Platform configuration meta-model including code for controlling an MIP sensor.
* New application example: MIP connector and MIP AI improvement ping-pong
* The [platform FAQ](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/FAQ.MD) is now in github.
* Early snapshot of [upcoming platform handbook for version 0.6.0](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/PlatformHandbook-0.6.0-SNAPSHOT-V0.1.pdf) now online in github (version as of 2023/08/20). Comments for clarification are welcome.
* Confirmed compatibility: Phoenix Contact PLCnext (OPC UA, EEM integration), Beckhoff IPC (OPC UA), VDW UMATI (OPC UA), Bitmotec (MQTT), Lenze (MQTT), MIP (MQTT) - and improving...

## Public demonstrations

* Public demonstrations on 2023/06/29 in [Hildesheim at the "day of computer science"](https://www.uni-hildesheim.de/fb4/veranstaltungen/tag-der-informatik/) as well as at [Nürnberg Digital "The future of intelligent Manufacturing"](https://nuernberg.digital/programm/details/die-zukunft-der-intelligenten-fertigung.html) on 2023/07/07. 
* Presentation of a federated learning case with two cobots on [HM'23](https://www.iip-ecosphere.de/iip-ecosphere-auf-der-hannover-messe-2023/) in collaboration with Phoenix Contact, Bitmotec/OnLogic, UR including platform-integrated federated learning based on [Flower](https://flower.dev/).
* Presentation of improved robot-based platform demonstrator at [TddT'22, Berlin](https://www.digitale-technologien.de/DT/Redaktion/DE/Veranstaltungen/2022/DT/220829_Tage_der_digitalen_Technologien.html) 
* Presentation of robot-based visual quality inspection app on [HM'22](https://www.hannovermesse.de/de/) (with 3 sources, Python-based AI, AAS-based Angular application).

## Overview

![Architecture Overview](platform/ArchitectureOverview.png)

For detailed links to the individual components, please refer to the [platform](https://github.com/iip-ecosphere/platform/tree/main/platform/README.md) overview. The platform repository also contains a growing set of [examples and demonstration use cases](https://github.com/iip-ecosphere/platform/tree/main/platform/examples/README.md).

For more details on how to develop application-specific services for the IIP-Ecosphere platform, please watch our series of video tutorials on [YouTube](https://www.youtube.com/playlist?list=PL5VSYtiD_PfdxUDXGlX53UsHdQlXcHYK7) or [IIP-Ecosphere](https://www.iip-ecosphere.de/angebote/plattform/).

## Documentation

The platform concepts and realization are developed by the IIP-Ecosphere consortium and are documented in several whitepapers:
  * The IIP-Ecosphere [platform handbook version 0.6.0 preview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/PlatformHandbook-0.6.0-SNAPSHOT-V0.1.pdf) - in development, not formatted/reviewed.
  * The IIP-Ecosphere [platform handbook version 0.5.0](https://doi.org/10.5281/zenodo.7695124) [link may be updated some days after platform releases]
  * The IIP-Ecosphere [platform FAQ](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/FAQ.MD).
  * Service development tutorial [YouTube](https://www.youtube.com/playlist?list=PL5VSYtiD_PfdxUDXGlX53UsHdQlXcHYK7) and [IIP-Ecosphere](https://www.iip-ecosphere.de/angebote/plattform/).
  * The IIP-Ecosphere [platform requirements (functional and quality view)](https://doi.org/10.5281/zenodo.4485774)
  * The IIP-Ecosphere [platform usage view](https://doi.org/10.5281/zenodo.4485801)
  * The IIP-Ecosphere [Industry 4.0 platform overview](https://doi.org/10.5281/zenodo.4485756)

All material is also available from the [IIP-Ecosphere Website](https://www.iip-ecosphere.eu/).

For detailed documentation and development hints see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md). For a documentation of the releases of the IIP-Ecosphere platform see [releases overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/RELEASES.md). For information on using/installing the platform, please consult the [installation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/INSTALL.md). 

## Building the Platform

The platform consists of some core and many alternative components which can be built locally via the provided Maven multi-module POM. However, to save time, we recommend relying on the pre-built snapshot and release binaries in [Maven Central](https://search.maven.org/search?q=iip-ecosphere) from our Continuous Integration server. To speed up this process when changes to the individual modules occur, the build sequence is incremental and not based on the Maven multi-module build. So we recommend that you install Java, Maven and an IDE (supported/working versions see [documentation overview](https://github.com/iip-ecosphere/platform/tree/main/platform/documentation/README.md)) and let Maven resolve the actual binaries for you. For more detailed information on the build dependencies, please refer to the platform handbook.

We've heard about strange problems on Mac OS. So far, we do not have enough information to resolve the potential problems.

To ease the first steps, we provided Demonstration and Development Docker Containers on [Docker Hub](https://hub.docker.com/r/iipecosphere/platform).

![IIP-Ecosphere](platform/logo.png)

Powered by (selection of used tools/frameworks)

<a href="https://www.eclipse.org/basyx"><img src="https://www.eclipse.org/basyx/img/basyxlogo.png" alt="BaSyx" width="200"/></a> 
<a href="https://iot.eclipse.org"><img src="https://iot.eclipse.org/iot-logo-social.png" alt="Eclipse IoT" width="100"/></a> 
<a href="https://www.docker.com"><img src="https://www.docker.com/wp-content/uploads/2022/03/vertical-logo-monochromatic.png" alt="Docker" width="100"/></a> 
<a href="https://prometheus.io"><img src="https://prometheus.io/assets/favicons/android-chrome-192x192.png" alt="Prometheus Monitoring" width="100"/></a>
<a href="https://flower.dev"><img src="https://flower.dev/docs/_static/flower-logo.png" width="100"/></a>
<br/>

<a href="https://spring.io"><img src="https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/img/banner-logo.svg" alt="Spring Cloud Stream" width="200"/></a> 
<a href="https://maven.apache.org"><img src="https://maven.apache.org/images/maven-logo-black-on-white.png" alt="Maven" width="200"/></a> 
<a href="https://sse.uni-hildesheim.de"><img src="https://sse.uni-hildesheim.de/media/_migrated/pics/EASy_Logo_02.png" alt="EASy-Producer" width="100"/></a> 

IIP-Ecosphere would like to thank all utilized technologies and frameworks for the helpful contributions to the Open Source community.

