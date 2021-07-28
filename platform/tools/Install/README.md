# IIP-Ecosphere platform: Install Support

This project contains a dependencies POM to install the basic parts of the platform so that a configuration via [configuration.configuration](../configuration/configuration/README.md) becomes possible as long as there is no UI. Will instantiate the platform server component, the ECS runtime, the service manager and an example service mesh.

The details are described in the platform handbook. For short:
  * Run `mvn install` in `platformDependencies`.
  * Run `mvn package` in the main folder of the package.
  * Modify `src/main/TestInstall.ivml` in particular to reflect your IP addresses.
  * Run `mvn exec:java -Dexec.args="InstallTest src/main/easy gen"`
  * Copy the created artifacts in `gen` (`ecsJars`, `ecs.*`, `serviceMgr`, `serviceMgr.*`, `SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar`) to the target machines.
  * Run `platform` (sh or bat) on the "server" machine.
  * Run `ecs` (sh or bat) on a "device" machine.
  * Run `serviceMgr` (sh or bat) on a "device" machine or below through the Cli inside a container.
  * Run `cli` (sh or bat) on the "server" machine to view resources, start/stop container or start/stop services.