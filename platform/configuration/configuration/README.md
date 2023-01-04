# IIP-Ecosphere platform: Configuration component

IIP-Ecosphere aims at an encompassing and consistent configuration of the whole platform in order to enable model-based platform instantiation, i.e., to include relevant parts and to exclude unwanted parts. This is the point in the platform where optional components such as transport protocols, connectors, service execution managers, container managers, service chains and applications etc. are combined, configured, validated and ultimately turned into instantiated code and installable artifacts.

As configuration technology, we rely on [EASy-Producer](https://sse.uni-hildesheim.de/forschung/projekte/easy-producer/), it's variability modeling language IVML and its instantiation languages VIL/VTL. All languages are described on the EASy-Producer website. We integrate EASy-Producer here through it's Maven artifacts, define the variability model in IVML, the variability instantiation in VTL and perform respective tests.

It is important to understand that the configuration model and the related code generation are evolving. For the actual release, we did not aim at a complete model rather than a model that can serve for initial tests and demonstrators. Thus, for now, advanced capabilities such as the assignment of services to resources, the respective partitioning (strategy) of the generated artifacts, or a mapping of external data types through the model/generation are intentionally missing. In particular data type mapping is currently considered as application-specific code that must be provided as application artifact. Further, services are currently loaded at runtime, which may turn into an IVML/VIL extension in the future.

So far there is no graphical user interface which shall be located one layer above this component, i.e., use the interface the component provides. For initial instantiation, we provide the PlatformInstantiator class which executes the configuration process from command line.

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


## Configuration model

The configuration model is written in the languages of EASy-Producer, namely Integrated Variability Modeling Language (IVML), Variability Instantiation Language (VIL) and Variability Template/Asset Language (VTL). EASy-Producer is open source on [github](https://github.com/SSEHUB/EASyProducer), also the most [recent specifications of IVML, VIL and VTL](https://github.com/SSEHUB/EASyProducer/tree/master/doc/web/docPreview). The [configuration model](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration/src/main/easy) is also explained/documented.

The regression tests are based on IVML models. Some just serve for structural purposes and regression testing within this component. Some are executable and part of the regression tests in [examples](https://github.com/iip-ecosphere/platform/tree/main/platform/examples/examples/README.md). Implementation components stem from [test.configuration.configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/tests/test.configuration.configuration/README.md) with mutual dependencies to this project. For a graphical documentation of the test cases, see [test case slides](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration/src/test/easy/summary.pdf).

## The resources folder

The resources directory contains files that shall be packaged into platform jars or into an application artifact during platform/application instantiation. It is split into

- `devices`: resources for devices such as nameplate information to be packaged with the ECS runtime
- `platform`: resources for all platform components such as central services, monitoring, ECS runtime, service manager
- `software`: resources for software services to be packaged into the service artifacts
- `rtsa`: additional resources for RapidMiner RTSA, e.g., a licensed version replacing our fake RTSA or actual deployments

So far, multiple resources folders may exist. Typically, resources is given and committed with resources that are not IPR protected. In contrast `resources.ipr` is the typical name of a local folder that must not be committed with mirrored files and those replaced that contain IPR content. So far, mirroring is required but replacement of resources.ipr over a basic version in resources would be desirable.

Applications projects containing the configuration model of the application typically do have their own resources folder, usually with subfolders `software` and if required `rtsa`.

## Prerequisites

For running the tests locally, you need a Python 3.9 with all IIP-Python dependencies (at least PyYaml, pyflakes - see platform handbook and [Install Package](https://github.com/iip-ecosphere/platform/tree/main/platform/tools/Install)) installed. On Windows, this can easily be obtained from the Microsoft Store.

Some of the test models include the RapidMiner RTSA integration. As RTSA is an IPR-protected commercial production, we cannot package it with its integration and must integrate its artifacts here. For this purpose, the folder `resources` contains resources that shall be packaged during platform/application installation. The RTSA files committed there contain fake RTSA implementation for testing built by the RTSA integration package. However, if you have a real RTSA at hands, create a similar directory called resources.ipr with the actual files and the instantiation will take it up.

**Hint:** If tests are failing on your side due to a missing Docker installation, you may prevent this by `-Deasy.docker.failOnError=false`.

**For snapshots:** EASy-Producer 1.3.4-SNAPSHOT built on **2023/01/03** or newer is required. Use `mvn -U exec:java ...`