# IIP-Ecosphere platform: Install Support

This project contains a dependencies POM to install the basic parts of the platform so that a configuration via [configuration.configuration](../configuration/configuration/README.md) becomes possible as long as there is no UI. In essence, this will instantiate the platform server component, the ECS runtime, the service manager and an example service mesh.

The details are described in the platform handbook. Please note that special characters like whitespaces in folder names (in particular on Windows) may cause the installation, platform installation or examples to fail.

  * Modify `src/main/easy/TechnicalSetup.ivml` in particular to reflect your IP addresses.
  * Run `mvn install` in the *main folder* of the install package.
  * Copy the created artifacts in `gen` (`broker`, `ecsJars`, `ecs.*`, `serviceMgr`, `serviceMgr.*`, `SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar`) to the target machines.
  * Start the broker, e.g., using `broker` (sh or bat) on the "server" and the "device" machine(s).
  * Run `platform` (sh or bat) on the "server" machine.
  * Run `ecs` (sh or bat) on a "device" machine.
  * Run `serviceMgr` (sh or bat) on a "device" machine or below through the Cli inside a container.
  * Run `cli` (sh or bat) on the "server" machine to view resources, start/stop container or start/stop services.
  
Besides the Maven build specifications for the platform dependencies, the test broker and the IIP-Ecosphere platform components, this Installation bundle also contains build information for two containers, namely an application container including the service manager and a simple application as well as a standalone container including the ECS runtime. Both containers can be build upon the artifacts provided through this installation package.

If you need to update the configuration model before instantiating the platform, use `mvn install -Dunpack.force=true`. You may also adjust the name of the model (user property `-Diip.model=`) the directory the model is located in (user property `-Diip.modelDir=`) or the output directory (user property `-Diip.outputDir=`).

This package contains also programs to optimize specific deployments, e.g., to reduce the overlap between JAR folders.

**External programs**

* Python 3.9, further dependencies for code generation, communication and potentially the Python-based data processing functions see [requirements.txt](./requirements.txt).
* For the management UI: Angular 13, further dependencies see [dependencies.json](./dependencies.json).

**For snapshots:** See required EASy-Producer version in [configuration integration and the configuration meta model](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration/README.md)