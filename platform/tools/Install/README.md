# oktoflow platform: Install Support

This project supports the installation of the oktoflow platform.

The platform can be installed on Windows and on Linux. Please note that special characters like whitespaces in folder names (in particular on Windows) may cause the installation, platform installation or examples to fail. We will explain the individual steps for Ubuntu 20.4.1 Linux installed.

The platform is intended for distributed installation. In this explanation we exemplify such a distribution in terms of two machines, the **server** (IP address 147.172.178.145) and one **device** (IP address 147.172.178.143). Please substitute the IP addresses in the steps below according to your local network setup.

The platform consists of several optional and alternative components, i.e., there is intentionally no single binary. While there are default settings for these components, one basic step is to decide about the desired components as well as the basic technical network information such as the server IP address (server processes may also run on different servers and also independently of each other, which we will not discuss here).

There are some fundamental platform components:
* The central platform communication **broker**. Dependent on the selected transport protocol, the platform instantiation will create a broker installation for you. Alternatively, you may also utilize a protocol-compliant broker of your operating system, e.g., for MQTT you may use Mosquitto.
* The **central server component** containing the platform AAS registry process, the platform AAS server process and the status update server.
* The optional central platform monitoring component, currently based on Prometheus.
* The device components
  * The **ECS-Runtime**, the Edge-Cloud-Server-Runtime, which registers the specific device with the platform. The ECS-Runtime also contains the container manager to start application containers (per default for Docker, i.e., a Docker-out-of-Docker setting).
  * The **ServiceManager**, which starts application services during application startup. If you rely on a containerized setup, each application container contains a server manager. There may be multiple application containers per device.
  * On small devices, multiple containers and even a separate ECS-Runtime and separate service managers may not be an option. Here the **ECSServiceManager** unifies the operations of both parts allowing to run application services in the same container. Then no further application containers are needed.

During the platform instantiation based on your decisions, also the platform and application containers for a containerized setup can be created automatically. As indicated above, this may involve different containers for different types of devices, e.g., smaller ones with only one container and larger ones with multiple containers. All these settings including the application design (i.e., the used services and their interlinking in terms of service meshes) are defined in the platform configuration. This is also true for the basic technical setup, i.e., which IPs shall be used by the devices to address the respective server processes.

For the next steps, we assume that you obtained the installation package and unpacked it. We assume that the folder where we unpacked the installation package is ``/iip/actual``. 

## Installation by script

The installation package contains installation scripts, which automate the manual installation based on the default setup decisions for the platform and your interactive input. For better understanding the platform and for more flexibility, we recommend the manual installation procedure explained below. Please note that support for installing prerequisites such as Docker through the scripts are based on assumptions for Ubuntu Linux and may be limited for Windows. Administrator permissions may be required, at least for installing some prerequisites.

* For **Linux**, the script checks whether Java is installed and, if absent, performs an installation of Java JDK 13. If the installed Java version is less than Java 11, the script asks whether to additionally install Java 13. Next, the script checks the installation of Maven, Docker, and Python: if not installed, the script will install the respective program. If the installed versions do not comply with the recommended versions, the script asks whether to install the recommended versions. Finally, the script asks whether to obtain and start a Docker Private Registry that will be used by the platform to store and distribute generated containers. Each installation step may be skipped or existing versions may be used, which, in turn, may affect the the platform instantiation or execution. Next, to use the management UI of the platform, the script asks for installing Angular version 14.2.11, Node.js version 14 and npm package manager for JavaScript. Further, the script will install the required Python libraries and change the IP address in the platform configuration to your local IP address. Further, if docker is installed, the script will setup the platform to create a containerized version and to deploy the containers in the local docker registry. Finally, the script will instantiate or execute the platform.
* For **Windows**, the same procedure is used as for Linux except for the installation of Docker and the Docker Private Registry. This restriction applies as first the Windows Subsystem for Linux (WSL) must be installed and a (further) unattended installation of Docker on Windows is more complicated and needs extra steps. Nonetheless, the script checks if Docker is installed and then checks the version. If Docker is installed, then the script will ask to obtain and run a Docker Private Registry. After installing the platform, the script may install node.js 16.10.0 and Angular 14.2.11. If you wish to use the management UI of the platform, please refer to the manual installation.

Please note that the actual platform installation may run longer, as a specific platform installation is assembled for you during this process. Being able to customize the platform even in terms of used components is a central aspect of oktoflow, i.e., there are no downloadable binaries, which are created in that step for you.

If you already have installed a different version of Python, the platform installation may grab the wrong installation, miss packages and fail. In that case, please set the environment variable ``IIP_PYTHON`` to the intended Python executable and re-instantiate the platform (in the folder `Platform/Install` execute `mvn install`).

## Manual installation

There are two detailed installation descriptions, one for [Windows](README-win.md) and one for [Linux](README-lionux.md).

## Adjust the platform configuration

The install package contains a example configuration in the folder ``src/main/easy``. As discussed in the platform handbook, a platform configuration consists of several files for technical setup, type definitions, service specifications, application and service mesh definitions. Moreover, there is the configuration meta model which will be obtained and unpacked during the next steps. Please note that the configuration meta model is only updated on explicit request, not automatically.

In general, there are several decisions that you may make now, e.g., the transport protocol, the wire format, the identification approach for the devices, specific properties of the devices that shall be considered during container creation. We will focus on some decisions now:

### Where will the central server be located? (required)

As stated before, we assume that this will be 147.172.178.145, which is already the default IP in the platform configuration. If you want to use any other address, please open ``src/main/easy/TechnicalSetup.ivml``, search for ``platformServer`` (or 147.172.178.145) and adjust the IP there by the IP of your server.

### Do you want the platform to create containers? (optional)

Container creation is currently disabled by default as it requires the respective setup and takes time, in particular during the first instantiation. On further instantiations of the platform, e.g., when defining applications, advanced methods aim at reducing the container creation time.

    containerGeneration = true;
    containerBaseImageMethod = true;
    footprintFolder = "/tmp/footprints";
    platformContainerGeneration = true;
    containerTestingMode = false;

``containerGeneration`` enables container creation with default settings. ``containerBaseImageMethod`` is an advanced container creation method that shall be enabled. The advanced methods apply a file-based fingerprinting, for which fingerprint files must be stored, by default in the ``footprints`` directory in the actual folder - you may adjust that. ``containerTestingMode`` allows to add additional programs to the containers for testing/debugging. By default, only application containers are created, i.e., for the central platform services please also enable ``platformContainerGeneration``. 

To enable these settings, please go into the freeze-part at the end of ``TechnicalSetup.ivml`` and add

    containerGeneration;
    containerBaseImageMethod;
    footprintFolder;
    platformContainerGeneration;
    containerTestingMode;

### Do you want to use the platform management UI? (optional) 

Then we need to enable the Angular UI as follows

    managementUi = AngularManagementUI {
        port = 4200
    };

and further we need to set the parent folder of the configuration model (called the ``modelBaseFolder``), the folder where downloadable artifacts shall be placed (``artifactsFolder``) and the server URI prefix where the artifacts folder will be made available. Depending on your setup, the URI prefix may point to some web server or in the most simple case to the Javascript server that the platform will instantiate for this purpose.

    modelBaseFolder = "/iip/actual";
    artifactsFolder = "/iip/actual/artifacts";
    artifactsUriPrefix = "http://147.172.178.145:4200/download";

To enable these settings, please go into the freeze-part at the end of ``TechnicalSetup.ivml`` and add

    managementUi;
    modelBaseFolder;
    artifactsFolder;
    artifactsUriPrefix;

### Do you want the platform to create system startup scripts for you? (optional)

These startup scripts may for technical reasons require absolute directories. The ``instDir`` states the actual installation directory, the ``javaExe`` the Java executable to be used. Please add then the following lines to the ``TechnicalSetup.ivml``.

    instDir = "/iip/actual";
    javaExe = "/iip/jdk/bin/java";

To enable these settings, please go into the freeze-part at the end of ``TechnicalSetup.ivml`` and add

    instDir;
    javaExe;

### Do you need special setup for the transport protocol? (optional)

One particular setting to be considered is the `` globalHost`` for data transport, the global communication broker. In the install package, this shall be automatically set through the global variable ``platformServer`` to ease the setup for a single server installation, but in your settings also different servers for different central services (AAS, monitoring, broker, etc.) may be set up, which then require a more individualized setup. Currently, the selection of code artifacts is restricted to the Maven servers used for development, i.e., further artifacts cannot be obtained from further repositories, e.g., the future platform service store. This will be targeted by one of the next releases. 


## Instantiate the platform

Instantiate the platform using

    mvn install

This step also obtains and unpacks the respective platform configuration model. Your instantiated platform components will be in the ``gen`` folder.

## Update your installation

If you already instantiated an older (snapshot) version of the platform, please advise mvn to update its artifacts, i.e., 

    mvn -U install

If you also want to update the configuration meta model, which may upgrade and evolve your applications, please specify in addition ``-Dunpack.force=true``.

## Starting the central parts 

### Manually

Start the platform server(s) component through the generated scripts in the gen folder. If you run all processes from the console as explained, this requires a separate shell for platform, ECS Runtime, service manager and UI/CLI as the processes run intentionally endless. Make sure to wait for each script to be done bevore starting the next (usually "Running until ctrl+C").

    broker.sh
    platform.sh
    monitoring.sh
    mgtUi.sh

### Via startup scripts

If you created systemd startup scripts, it is now time to install them, i.e., usually to copy them into ``/ecs/systemd/system``, to refresh the setup and to enable the services depending on your Linux distribution. After enabling, the broker, the platform service, the monitoring service, and, if configured, the management user interface shall be up.

## Deploying and starting platform components on the devices

Depending on your decisions made before, the fundamental platform components must be deployed manually or can be installed in terms of the generated containers.

### Deployment to devices without containers

Copy the created artifacts from `gen` (`broker/*`, `ecsJars/*`, `ecs.sh`, `svcJars/*`, `serviceMgr.sh`, `SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar`) to the devices. For each artifact, the instantiation creates a folder with all dependencies and the respective startup script. 

* Start the broker for local communications
* Start the platform components, i.e., either
    * The ECS runtime (ecs.sh) and the service manager (`serviceMgr.sh`) through their startup scripts.
    * The combined ECS-serviceManger (`ecsServiceMgr.sh`)

Currently, by default, no explicit on/offboarding of devices is needed. 

### Deployment to devices with containers

On the devices, run 

    docker pull 147.172.178.145:5001/allapps/dflt:0.1.0

with the address of your server, the port of the local docker registry and the name of the container (there are also variants for `plcnext` or `bitmotec`, replacing `dflt` in the container name). The platform may also generate scripts for starting the individual containers.

## Deploying applications

Depending on your installation, the application may be distributed/executed by the platform or requires manual actions.

### Plan-based deployment

The platform instantiation copies an example deployment plan as well as the application binaries into the configured `artifacts` folder. The example deployment plan starts the services on the actual computer (`.`, assuming that ECS-Runtime and Service Manager or the combined version are running). The application in this plan is taken from the build directory of the application. In a distributed setup, the application artifacts are downloaded through the specified URI prefix via the web server of the management user interface.

In the application, a source service generates random numbers and passes them on to the sink service. The results are only visible in the service logs, which are stored in temporary folders.

Deployment of the application happens through 

`cli.sh deploy artifacts/deployment.yaml`

undeployment via 

`cli.sh undeploy artifacts/deployment.yaml`

Please observe the logs of the service manager for the service logs.

Similarly, on the management UI you may navigate to `runtime` and then to `deployment plans` and select the plan for deployment. In the the `services` tab, you will find the running services. You can request there, e.g., the log of the receiver service. Further, you will find the running applications in the `instances` tab, where you can request an undeployment of the selected instance.

When writing own deployment plans, consider changing the default device identification provider from MAC addresses to network names. In the log of the ECS-Runtime/Service manager for a device, you will find the actual device ID needed in the the deployment plans, e.g., ``USING id 00FFB9A35D2B from de.iip_ecosphere.platform.support.iip_aas.MacIdProvider``. You can also find this information for available devices in the `resources` tab of the management UI.

### Manual deployment

You can use the CLI to start individual services of the application on a certain resource. However, this requires information on the structure of the application, which is better provided through a deployment plan.

# Further contents
  
Besides the Maven build specifications for the platform dependencies, the test broker and the oktoflow platform components, this Installation bundle also contains build information for two containers, namely an application container including the service manager and a simple application as well as a standalone container including the ECS runtime. Both containers can be build upon the artifacts provided through this installation package.

If you need to update the configuration model before instantiating the platform, use `mvn install -Dunpack.force=true`. You may also adjust the name of the model (user property `-Diip.model=`) the directory the model is located in (user property `-Diip.modelDir=`) or the output directory (user property `-Diip.outputDir=`).

This package contains also programs to optimize specific deployments, e.g., to reduce the overlap between JAR folders.
