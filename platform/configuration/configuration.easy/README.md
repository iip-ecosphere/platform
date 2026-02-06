# oktoflow platform: configuration.easy

Realization of configuration.interface as oktoflow configuration/generation plugin using EASy-producer as well as the IVML-based configuration model. For testing,
this project contains some test implementation services and a three-step build process (interfaces, services, apps).

As configuration technology, we rely on [EASy-Producer](https://sse.uni-hildesheim.de/forschung/projekte/easy-producer/), it's variability modeling language IVML and its instantiation languages VIL/VTL. All languages are described on the EASy-Producer website. We integrate EASy-Producer here through it's Maven artifacts, define the variability model in IVML, the variability instantiation in VTL and perform respective tests. This component realizes the technology integration and the configuration model. 

It is important to understand that the configuration model and the related code generation are evolving, i.e., modeling capabilities, properties, mechanisms and code generation may change over time. A graphical user interface is also evolving in terms of the webbased [management UI](../../managementUI).

The graphical user interface is in [managementUi](../../managementUi) which is located one layer above this component, i.e., uses the interfaces this component provides as well as the `simpleMesh` testing application defined in this component. The default configuration plugin is [EASy-Producer](../configuration.easy). This plugin relies on its model being located in `src/main/easy` and `src/test/easy`. Alternative implementations may demand similar folders, for which, however, the assembly packaging must be adjusted.

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

## AAS configuration format

oktoflow maps the actual platform/app configuration into an AAS as backend for the management UI. SM means SubModel, SMEC means SubModelElementCollection, SMEL means SubModelElementList, * indicates a potentially unlimited repetition of the element, ? indicates an optional element.
JSON is taken up by [oktoflow2grafana](https://github.com/iip-ecosphere/oktoflow2grafana).

- SM `Configuration` URN `iri:urn:::AAS:::iipEcosphere#CFG`
    - OPERATION setGraph
    - OPERATION deleteVariable
    - OPERATION changeValues
    - OPERATION genInterfacesAsync
    - OPERATION removeImports
    - OPERATION getGraph
    - OPERATION addImports
    - OPERATION getVariableName
    - OPERATION deleteGraph
    - OPERATION renameVariable
    - OPERATION instantiateTemplate
    - OPERATION getTemplates
    - OPERATION createConstantVariable
    - OPERATION getOpenTemplateVariables
    - OPERATION createVariable
    - OPERATION genAppsNoDepsAsync
    - OPERATION genAppsAsync
    - OPERATION getUnusedProjectNames
    - SMEC* _typeName_ (e.g., metamodel type `Server`)
        - SMEL _instanceVariableName_ (e.g., `MyServer`)
            - SMEC* _fieldName_ (fields with values, only if variable is of compound type)
                - PROPERTY `varValue`: value of variable, corresponding primitive type, where available including semanticId
                - PROPERTY `metaVariable`, Type `String`, IVML variable name, may differ from SMEC idShort due to AAS name conventions
                - PROPERTY `metaState`, Type `String`, IVML variable state, e.g., `FROZEN`
                - PROPERTY `metaTemplate`, Type `Boolean`, whether field is in app template
                - PROPERTY? `metaDisplayName`, Type `String`, if given, supersedes `metaVariable` by a human-readable display name
                - PROPERTY `metaType`, Type `String`, IVML type of field
                - PROPERTY `metaTypeKind`, Type `Integer`, only on top-level kind of type
                    - `1`: primitive
                    - `2`: enum
                    - `3`: container
                    - `4`: constraint
                    - `9`: derived/typedef
                    - `10`: compound
                - PROPERTY? `metaConstant`, Type `Boolean`, whether variable is a constant
            - PROPERTY `varValue`: value of variable, corresponding primitive type, where available including semanticId
            - PROPERTY `metaVariable`, Type `String`, IVML variable name, may differ from SMEC idShort due to AAS name conventions
            - PROPERTY `metaState`, Type `String`, IVML variable state, e.g., `FROZEN`
            - PROPERTY `metaTemplate`, Type `Boolean`, whether field is in app template
            - PROPERTY? `metaDisplayName`, Type `String`, if given, supersedes `metaVariable` by a human-readable display name
            - PROPERTY `metaType`, Type `String`, IVML type of field
            - PROPERTY `metaTypeKind`, Type `Integer`, only on top-level kind of type
                - `1`: primitive
                - `2`: enum
                - `3`: container
                - `4`: constraint
                - `9`: derived/typedef
                - `10`: compound
            - PROPERTY? `metaConstant`, Type `Boolean`, whether variable is a constant
    - SMEC `meta` (type definitions, all types referred above shall be listed here)
        - SMEL* _typeName_ (sorted in sequence that UI shall use for display)
            - SMEC* _fieldName_ 
                 - PROPERTY `name`, Type `String`, name of field, may differ from SMEC idShort due to AAS name conventions
                 - PROPERTY `uiGroup`, Type `Integer`, group for displaying multiple fields, i.e.,
                     - `0`: invisible
                     - `100`: mandatory group 1, if needed `1`..`98` denote specific positions with this group and `99` add the field always to the front of the group
                     - `-100`: optional group 1, if needed `-1`..`-98` denote specific positions with this group and `-99` add the field always to the front of the group
                     - `200`: mandatory group 2, if needed `101`..`198` denote specific positions with this group and `199` add the field always to the front of the group
                     - `-200`: optional group 1, if needed `-101`..`-198` denote specific positions with this group and `-199` add the field always to the front of the group
                 - PROPERTY? `metaDisplayName`, Type `String`, if given, supersedes `metaVariable` by a human-readable display name
                 - PROPERTY? `metaDefault`, Type `String`, if given, the IVML expression denoting the default value of this field
                 - PROPERTY? `metaRequired`, Type `Boolean`, is the value of this field mandatory?
                 - PROPERTY `type`, Type `String`, IVML type, may be primitive or composed, shall be listed for reference in `meta`
                 - PROPERTY? `metaConstant`, Type `Boolean`, whether field is a constant
            - PROPERTY? `metaAbstract`, Type `Boolean`, whether the type is abstract and cannot be instantiated, only if `metaTypeKind` is `10` (compound)
            - PROPERTY `metaRefines`, the parent type(s), only if `metaTypeKind` is `10` (compound), may be empty
            - PROPERTY `metaProject`, defining IVML project
              

## Configuration meta-model

The configuration meta-model and its instantiation are written in the languages of EASy-Producer, namely Integrated Variability Modeling Language (IVML), Variability Instantiation Language (VIL) and Variability Template/Asset Language (VTL). EASy-Producer is open source on [github](https://github.com/SSEHUB/EASyProducer), also the most [recent specifications of IVML, VIL and VTL](https://github.com/SSEHUB/EASyProducer/tree/master/doc/web/docPreview). The [configuration model](/src/main/easy) is also explained/documented.

The regression tests are based on IVML models. Some just serve for structural purposes and regression testing within this component. Some are executable and part of the regression tests in [examples](../../examples/README.md). Implementation components stem from [test.configuration.configuration](../../tests/test.configuration.configuration/README.md), which is built through the ANT build file within this project. Tests that involve the components for the [management UI](../../managmementUI) and [platform services](../../platform) are defined here, packaged as test-easy artifact and executed in [examples](../../examples/examples). For a graphical documentation of the test cases, see [test case slides](src/test/easy/summary.pdf).

## Docker base images for Python (Using Digest)

Sometimes, the base images used to create container images for the platform and applications gets updated (outside of the platform), which might cause errors during container image creation. To avoid that, we used the digest of the base images to always get the exact same image (Digest is a cryptographic hash, specifically a SHA256 hash, that uniquely identifies the contents of a container image).

To get the digest for an image (e.g. `python:3.8.20-slim-bullseye`) use the command:
```
docker inspect --format='{{index .RepoDigests 0}}' python:3.8.20-slim-bullseye
```
The result should be something like 
```
Result: python@sha256:e191a71397fd61fbddb6712cd43ef9a2c17df0b5e7ba67607128554cd6bff267
```
Then update the FROM in the dockerfile to 
```
FROM python@sha256:e191a71397fd61fbddb6712cd43ef9a2c17df0b5e7ba67607128554cd6bff267
```
To find the Python version from a specific SHA256 hash - this command will pull the image
```
docker run --rm python@sha256:e191a71397fd61fbddb6712cd43ef9a2c17df0b5e7ba67607128554cd6bff267 python --version
```
The result should be something like 
```
Python 3.8.20
```

## Meta-model extensions

The platform meta-model ships with various extensions. Some are loaded into the meta-model by default, others must be imported explicity.

* PhoenixContact devices
* Bitmotec devices
* Federated learning based on Flower
* KIProtect KODEX
* Rapidminer RTSA
* Example connector models for MIP magentic sensors and NovoAI AVA.

In addition, the meta-model contains two specialized type models for OPC UA (companion specifications) and AAS IDTA specifications. The configuration project contains specialized parsers/model-translators for OPC XML to IVML as well as IDTA (PDF/AASX) to IVML. 

The IDTA tools are meant to be proof-of-context implementations as the specifications/formats allow for many variations. The IDTA tools also contain a program to structurally compare models as well as a program to load AASX files via multiple versions of BaSyx.

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

Some tests are executed in own JVMs to prevent conflicting dependencies with Maven. To update the persisted dependency list, call `mvn prepare-package`. 

To build this component without tests, e.g., to just package a changed model for local tests, run `mvn install -DdisableJavaTests=true`. However, as this model is only one of multiple potential models, subsequently the build process of `configuration` must be executed to collect all (alternative) models and to effectively deploy them.

**Hint:** To shield Maven from the dependencies of EASy-Producer, usually the instantiation is executed in a separated process. However, as the execution relies on the Jar files of this project, changes to the code require mvn install before the process-based execution takes up the new code.