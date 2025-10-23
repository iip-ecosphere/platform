# oktoflow platform: configuration.easy

Realization of configuration.interface as oktoflow configuration/generation plugin using EASy-producer. For testing,
this project contains some test implementation services and a three-step build process (interfaces, services, apps).

As configuration technology, we rely on [EASy-Producer](https://sse.uni-hildesheim.de/forschung/projekte/easy-producer/), it's variability modeling language IVML and its instantiation languages VIL/VTL. All languages are described on the EASy-Producer website. We integrate EASy-Producer here through it's Maven artifacts, define the variability model in IVML, the variability instantiation in VTL and perform respective tests.

It is important to understand that the configuration model and the related code generation are evolving, i.e., modeling capabilities, properties, mechanisms and code generation may change over time. A graphical user interface is also evolving in terms of the webbased [management UI](../../managementUI).

The graphical user interface is in [managementUi](../../managementUi) which is located one layer above this component, i.e., uses the interfaces this component provides. For initial instantiation, we provide the PlatformInstantiator class which executes the configuration process from command line.

## Setup

```yaml
easyProducer:
  base: <String>
  genTarget: <String>
  ivmlMetaModelFolder: <String>
  ivmlConfigFolder <String>
  ivmlModelName: <String>
  easyLogLevel: NORMAL|VERBOSE|EXTRA_VERBOSE
serviceArtifactStorage:
  ...
containerImageStorage:
  ...
```

The setup for this component defines the `base` folder for the model, which typically contains the meta-model folder `ivmlMetaModelFolder` (default `model`), the folder where the configuration of the actually installed platform and the running apps is located (`ivmlConfigFolder`, per default undefined, thus in `base`), the name of the top-level IVML file (per default `IIPEcosphere`, but usually the top-level model in `ivmlConfigFolder`) and a writable folder where to locate generated artifacts (`genTarget`). Further, the logging level of the underlying EASy-Producer toolset can be defined (by default `NORMAL`).

The setup also may contain a `serviceArtifactStorage`and a `containerImageStorage` PackageStorageSetup specification from [deviceMgt](../../resources/deviceMgt/README.md), which are currently not used.


## The resources folder

The resources directory contains files that shall be packaged into platform jars or into an application artifact during platform/application instantiation. It is split into

- `devices`: resources for devices such as nameplate information to be packaged with the ECS runtime
- `platform`: resources for all platform components such as central services, monitoring, ECS runtime, service manager
- `software`: resources for software services to be packaged into the service artifacts
- `rtsa`: additional resources for RapidMiner RTSA, e.g., a licensed version replacing our fake RTSA or actual deployments

So far, multiple resources folders may exist. Typically, resources is given and committed with resources that are not IPR protected. In contrast `resources.ipr` is the typical name of a local folder that must not be committed with mirrored files and those replaced that contain IPR content. So far, mirroring is required but replacement of resources.ipr over a basic version in resources would be desirable.

Applications projects containing the configuration model of the application typically do have their own resources folder, usually with subfolders `software` and if required `rtsa`.

## Prerequisites

Tracing for tests is set to `TOP` since version 0.7.0. Tracing can be enabled via the system property `iip.easy.tracing` using the values `ALL`, `FUNC` or `TOP`.

For running the tests locally, you need a Python 3.9 with all required dependencies (see [Prerequisites](../../documentation/PREREQUISITES.md) and [Install Package](../../tools/Install)) installed. 

For running the container tests, you need Docker and LXC (Linux only). To bypass the container creation in either case, use `-Deasy.docker.failOnError=false` to disable failure reporting during Docker instantiator execution or `-Deasy.lxc.failOnError=false` to disable failure reporting for the LXC instantiator. The instantiation process shall then run anyway and produce the related artifacts, e.g., Dockerfiles or LXC templates, but no container images are created/deployed.

Some of the test models include the RapidMiner RTSA integration. As RTSA is an IPR-protected commercial production, we cannot package it with its integration and must integrate its artifacts here. For this purpose, the folder `resources` contains resources that shall be packaged during platform/application installation. The RTSA files committed there contain fake RTSA implementation for testing built by the RTSA integration package. However, if you have a real RTSA at hands, create a similar directory called resources.ipr with the actual files and the instantiation will take it up.

**Hint:** If tests are failing on your side due to a missing Docker installation, you may prevent this by `-Deasy.docker.failOnError=false`.

## Tests

For further steps, test implementation/execution is here while the model is taken from configuration (src/main/easy, src/test/easy) and results are written there into target/gen.

Some tests are executed for code generation in this project, the remainder is executed by the `examples` project there. Since version 0.7.1 the generated code is in `target/gen` rather than `gen`.

Some tests are executed in own JVMs to prevent conflicting dependencies with Maven. To update the persisted dependency list, call `mvn prepare-package`. To build this component without tests, run `mvn install -DdisableJavaTests=true`. 
