# IIP-Ecosphere platform: Install Support

This project contains a dependencies POM to install the basic parts of the platform so that a configuration via [configuration.configuration](../configuration/configuration/README.md) becomes possible as long as there is no UI. In essence, this will instantiate the platform server component, the ECS runtime, the service manager and an example service mesh.

The details are described in the platform handbook. For short:
  * Run `mvn install` in `platformDependencies`.
  * If you want to use the AMQP test broker, run `mvn package` in `broker`.
  * Run `mvn package` in the main folder of the package.
  * Modify `src/main/TestInstall.ivml` in particular to reflect your IP addresses.
  * Run `mvn exec:java -Dexec.args="InstallTest src/main/easy gen"`
  * If you want to use the AMQP test broker, copy the files in `broker/brokerJars` as well as the start script for AMQP to the target machines.
  * Copy the created artifacts in `gen` (`ecsJars`, `ecs.*`, `serviceMgr`, `serviceMgr.*`, `SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar`) to the target machines.
  * Start the broker, e.g., using `amqp` (sh or bat) on the "server" and the "device" machine.
  * Run `platform` (sh or bat) on the "server" machine.
  * Run `ecs` (sh or bat) on a "device" machine.
  * Run `serviceMgr` (sh or bat) on a "device" machine or below through the Cli inside a container.
  * Run `cli` (sh or bat) on the "server" machine to view resources, start/stop container or start/stop services.
  
Besides the Maven build specifications for the platform dependencies, the test broker and the IIP-Ecosphere platform components, this Installation bundle also contains build information for two containers, namely an application container including the service manager and a simple application as well as a standalone container including the ECS runtime. Both containers can be build upon the artifacts provided through this installation package. 

This package contains also programs to optimize specific deployments, e.g., to reduce the overlap between JAR folders.