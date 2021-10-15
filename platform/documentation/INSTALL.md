# IIP-Ecosphere platform: Installation

This document briefly summarizes how to install and run the IIP-Ecosphere platform. Further information on use cases or IoT demo applications will follow in the future.

For further information on using the individual parts, please consult the platform handbook on [Zenodo](https://doi.org/10.5281/zenodo.5168946) or the [IIP-Ecosphere Website](https://www.iip-ecosphere.eu/). The links may become active/updated some days after a release.

### Usage scenario

This is a simple trace on how the IIP-Ecosphere platform can be used through the (preliminary) command line interface:

    IIP-Ecosphere, interactive platform command line
    AAS server: http://127.0.0.1:9001
    AAS registry: http://127.0.0.1:9002/registry
    Type "help" for help.
    > resources
    resources> help
     list
     help
     back
    resources> list
     - Resource a005056C00008
       systemdisktotal: 1023887356
       systemmemorytotal: 2147483647
       simplemeterlist: ["system.cpu.count","system.cpu.usage", "system.disk.free", "system.memory.free"…]
       containerSystemName: Docker
       systemmemoryfree: 2147483647
       systemdiskfree: 464061712
       systemmemoryused: 2147483647
       systemdiskusable: 464061712
       systemmemoryusage: 0.5555296172875698
       systemdiskused: 559825644
    resources> back
    > exit

The trace above illustrates a shell-like interaction of the user with the command line interface. The CLI provides commands for resources, containers and services. Resources are added automatically when an ECS runtime is executed. The trace above shows the properties listing for a resource with identifier `a005056C00008`. Containers and services can be managed by stating the respective resource identifier after the command. Before starting containers/services, the respective item must be added to the platform, i.e., an add command for a container image or a service artifact must be specified. The items are stated in terms of URIs, at the moment usually files on the local file system.

### Pre-packaged Docker containers

We offer two pre-packaged Docker containers on Docker Hub (an IIP-Ecosphere hub space is in preparation), 

* the first one containing the platform parts (AAS server, the ECS Runtime, the Service Manager) and a simple example service artifact. 
* the second one with the Command Line Interface shown in the usage scenario above.

To experiment with the containers, use the following commands.
Create a network:

    docker network create --subnet=172.19.0.0/16 platform   

and start the platform parts:

    docker run --network platform --ip 172.19.0.22 -p 9001:9001 -p 9002:9002 -p 8883:8883 dzikaswinia/platform:platform   

and similarly for the CLI

    docker run -i --network platform dzikaswinia/platform:cli
    
The service artifact that is needed to add/start services is available under the following local URI

    file:/device/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar

### Installation from scratch

**Prepare the operating system.** For the next steps in this section, we assume a Ubuntu 20.4.1 Linux installed on two machines (assuming 147.172.178.145 as “server” and 147.172.178.143 as “device”, we will adjust the IP addresses in the fourth step). Install unzip, Java JDK  and maven (version 3.6.3), docker (version 20.10.2):

     ~$ sudo apt install unzip
     ~$ sudo apt install openjdk-13-jdk-headless
     ~$ sudo apt install maven
     ~$ sudo apt install docker.io

On devices, the installation may differ as Java/Maven could be part of the container hosting the ECS runtime or the Service Manger/Services. By default, Docker requires root permissions to execute functions. If you want to use docker as “normal” user , execute

     ~$ sudo usermod -aG docker $USER

Log out and log back so that your group membership is re-evaluated.

**On server and devices:** Obtain the IIP-Ecosphere platform install package. Snapshots can be obtained from SSE Jenkins for [Windows](https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.zip), [Linux](https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz) or from [github](https://downgit.github.io/#/home?url=https://github.com/iip-ecosphere/platform/tree/main/platform/tools/Install). The install package for the actual release can be obtained from [github](https://downgit.github.io/#/home?url=https://github.com/iip-ecosphere/platform/tree/v0.2.0/platform/tools/Install).

**On server and devices:** Unpack the install package. Using Maven, obtain the IIP-Ecosphere platform dependencies first. 

    ~$ unzip Install.zip
    ~$ cd Install/platformDependencies
    ~/Install/platformDependencies$ mvn install
    ~/Install/platformDependencies$ cd ..

This must be done once before other Maven installation steps.

**On the server**, optionally also on devices depending on the device resources: Install a broker. The decision for the broker will influence the configuration changes below. By default, the installation package ships with a basic setup of the AMQP broker Apache Qpid. To obtain the default broker, execute

    ~/Install$ cd broker
    ~/Install/broker$ mvn package
    ~/Install/broker$ cd ..

To run the broker, execute the respective script in the broker directory. The broker is needed on both, the server and the device installations/containers. 
* On the server, the broker acts as global platform broker running by default on port 8883. 
* On the device, the broker handles the local service communication that shall not leave the device. For installing on a device, copy the respective script and the folder brokerJars in broker to the device.

**On server and devices:** Run Maven now on the install package itself 

    ~/Install$ mvn package
    
You may perform the Maven install command, but the package command is sufficient here. This step also obtains and unpacks the respective platform configuration model into `src/main/easy`.

**On the server:** Edit the example configuration file `InstallTest.ivml` in `src/main/easy` so that your local IP address is used. In this release, the devices are not listed in the configuration, i.e., search for `147.172.178.145` and replace this IP by the IP of your server machine. You may perform more changes, but this requires background knowledge on the platform configuration model (cf. Platform Handbook). Currently, the selection of code artifacts is restricted to the Maven servers used for development, i.e., further artifacts cannot be obtained from further repositories, e.g., the future platform service store. This will be targeted by one of the next releases. 

Depending on the Java version, various settings to open the Java module system may be needed. Currently, these settings are fixed in IIPEcosphere.ivml in the variable `javaOpts` and can be adjusted before generation if needed. Pragmatically, you may also alter the generated shell scripts (cf. below). 

**On the server:** Instantiate the platform using 

    ~/Install$ mvn exec:java -Dexec.args="InstallTest src/main/easy gen"

This executes the PlatformInstantiator through Maven, passing it three parameters, namely the name of the model to instantiate (`InstallTest`), the relative folder where the model is located (`src/main/easy`) and the folder where to store the instantiated artifacts (the relative folder `gen`). Please note that this may fail if your modifications to the configuration file are syntactically or semantically incorrect. Alternatively, you can check out the full code from github and run the PlatformInstantiator from your IDE or force maven to copy all dependencies into a folder and run Java manually on the command line. As several files and folders are produced by the instantiation process, also a `README.txt` file is generated, which provides some explanation on the individual files and folders.

**On the server:** Copy the created artifacts in `gen` (`ecsJars/*`, `ecs.sh`, `svcJars/*`, `serviceMgr.sh`, `SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar`) to the respective devices. For each artifact, the instantiation creates a folder with all dependencies and the respective startup script. In future versions of the platform, this step will be taken over by the device management, the automated container creation and the distribution of containers by the platform.

**On server (and depending on your decisions above):** Install and start a protocol broker/server instance complying with the configuration settings. Just as a reminder, the IIP-Ecosphere platform does not ship with a particular broker, e.g., for MQTT or AMQP although the regression tests utilize specific brokers as discussed in the Platform Handbook.

**Start the platform components:**
* On the server: Start the platform server(s) component through the generated `platform.sh/bat` in the `gen` folder.
* On the devices (if desirable also on the server): Start the ECS runtime and, finally, the service manager through the startup scripts `ecs.sh/bat` or `serviceMgr.sh/bat`, respectively. 

**On server or devices:** Run `cli.sh.` An example interactive execution trace was shown above. For deploying services, the created application artifact must be on the device running the service manager. Add the artifact to the service manager via the cli (through a local file URL on that device) and start the services. Please note that artifacts and containers are added through their URI, whereby local URIs may differ from system to system, e.g., 
* Windows: `file:///C:/.../SimpleMeshTestingApp.jar`
* Linux: `file:/home/ecouser/SimpleMeshTestingApp.jar`
* Service container: `file:/apps/SimpleMeshTestingApp.jar`

**On server and devices:** To avoid timeouts, a shutdown shall happen in the opposite sequence, i.e., services, service manager, container manager, platform, brokers. 

If you want to exercise the full cycle, create a Docker container with the service manager and the application artifact first and copy the container to the device running the ECS runtime. It is important to emphasize that these steps shall be automated in future releases. 

Copy the container folder from the installation package to your “device”. Copy/move the platform artifacts also into the container folder and execute there

    ~/Install$ docker build -t iip/simplemesh:0.1 -f SimpleMeshTestingApp/Dockerfile .
     ~/Install$ docker save iip/simplemesh:0.1 | gzip > SimpleMeshTestingApp/simplemesh-0.1.tar.gz
     
For convenience, both commands are available as `createAppContainer.sh` and `saveAppContainer.sh` in the install package. At a glance, the second step may appear superfluous, but it is required for the deployment and execution of the container through the ECS runtime. Please take care that the tag `iip/simplemesh` and the file name `simplemesh-0.1.tar.gz` are the same as in the container descriptor `SimpleMeshTestingApp/image-info.yml`. Add and start the container (similar as described for the services above) through the platform command line interface before starting the services in the container. 

With a running platform server and a running ECS runtime, you may also start the container manually. This would then require setting the AAS implementation server port correctly as stated in the container descriptor, i.e., `--network=host --expose port -e “IIP_PORT=port”`. If feasible, you may use the default port `9000` and use `--expose 9000` or more generically `-P` as parameters. An example script is included in the install package as `runAppContainer.sh`.

If you also want to containerize the ECS runtime (one of the possible edge device installations), ensure that the folder `container/EcsRuntime` is on the “device”. For simplicity and to save resources, we map the `SimpleMeshTestingApp` folder as volume into the ECS container (mount point `/SimpleMeshTestingApp`). 

    ~/Install$ docker build -t iip/ecsruntime:0.2 -f EcsRuntime/Dockerfile .
    ~/Install$ docker run -v /var/run/docker.sock:/var/run/docker.sock -P --network=host --mount type=bind,source="$(pwd)"/SimpleMeshTestingApp,target=/SimpleMeshTestingApp -it iip/ecsruntime:0.2

Akin to the app container, both steps are available as respective scripts in the install package. Before running the ECS container, it is important that the the app container has been created and stored. As administrative operations for installing Docker into the container are executed, Docker may issue certain warnings during the creation of the container. The default port for the ECS Runtime AAS implementation server in this Dockerfile is `9000`.

For a permanent installation, the instantiation process also generates service specifications for Linux/systemd, for both, integrated installation of platform services, ECS runtime and service manager on a single machine/container and additional no-dependencies service specifications for device/container installation. The service descriptors assume an installation of the generated jars (including containing directory, e.g., `plJars`) in a folder denoted by the global variable `$IIP_HOME`. After completion of the startup process, the respective executable creates a file containing the process identification (PID) in the usual system directory `/run`. These PID files are taken into account by the system service specifications.
