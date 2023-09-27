# IIP-Ecosphere platform: Install Support

This project supports the installation of the IIP-Ecosphere platform.

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

The installation package contains installation scripts, which automate the manual installation based on the default setup decisions for the platform and your interactive input. For better understanding the platform and for more flexibility, we recommend the manual installation procedure explained below. Please note that support for installing prerequisites such as Docker through the scripts are based on assumptions for Ubuntu Linux and may be limited for Windows.

* For **Linux**, the script checks whether Java is installed and, if absent, performs an installation of Java JDK 13. If the installed Java version is less than Java 11, the script asks whether to additionally install Java 13. Next, the script checks the installation of Maven, Docker, and Python: if not installed, the script will install the respective program. If the installed versions do not comply with the recommended versions, the script asks whether to install the recommended versions. Finally, the script asks whether to obtain and start a Docker Private Registry that will be used by the platform to store and distribute generated containers. Each installation step may be skipped or existing versions may be used, which, in turn, may affect the the platform instantiation or execution. Next, to use the management UI of the platform, the script asks for installing Angular version 14.2.11, Node.js version 14 and npm package manager for JavaScript. Further, the script will install the required Python libraries and change the IP address in the platform configuration to your local IP address. Further, if docker is installed, the script will setup the platform to create a containerized version and to deploy the containers in the local docker registry. Finally, the script will instantiate or execute the platform.
* For **Windows**, the same procedure is used as for Linux except for the installation of Docker and the Docker Private Registry. This restriction applies as first the Windows Subsystem for Linux (WSL) must be installed and a (further) unattended installation of Docker on Windows is more complicated and needs extra steps. Nonetheless, the script checks if Docker is installed and then checks the version. If Docker is installed, then the script will ask to obtain and run a Docker Private Registry. However, the script does not install Angular version 14.2.11 on windows since it needs more setup steps. If you wish to use the management UI of the platform, please refer to the manual installation. Usually, it is required to run the script with administrator privileges.

## Manual installation

### Prepare the server

On the server, where we will build and instantiate the platform also for the devices, install the programs unzip, Java JDK (version 11 up to 16 due to some limitations of used dependencies), maven (version 3.6.3) and docker (version 20.10.2):

     sudo apt install unzip
     sudo apt install openjdk-13-jdk-headless
     sudo apt install maven
     sudo apt install docker.io

By default, Docker requires root permissions to execute functions. If you want to use docker as “normal” user , execute

     sudo usermod -aG docker $USER

Log out and log back so that your group membership is re-evaluated. 

For distributing containers created by the platform, we need a local Docker registry, in particular if services contain IPR-protected code. We install the registry on the server on port 5001 as follows:

    docker run -d \
      --restart=always \
      --name registry \
      -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 \
      -p 5001:5001 \
      registry:2

In general, we recommend using a distinct server for this and to adjust the settings in the platform configuration as discussed below.

You may check the registry using
    
    curl -sS http://192.168.2.1:5001/v2/_catalog

### Prepare the devices

On devices, the installation may differ depending on the desired degree of containerization. If you want to run the fundamental platform components on bare metal, please install also unzip, JDK and maven on the devices. If you plan to run only containers on the devices, docker is sufficient.

Create or edit ``/etc/docker/daemon.json`` in order to access the local Docker registry installed above, here without TLS certificates. For an empty file, this looks like:

    {
      "insecure-registries" : ["147.172.178.145:5001"]
    }
    
Then execute

    sudo systemctl restart docker

### Prepare Python

Depending on the use of Python in services, the build process for applications may include Python syntax checking and execution of Python unit tests. If you plan to run the platform on bare metal under administrator permissions, e.g., as systemd service, please install the following python dependencies globally, i.e., with administrator permissions.

If Python is utilized, at least Python 3.8.5 with pyflakes and for service execution PyYaml as well as websockets must be installed. 

    python3 -m pip install pyflakes==2.5.0
    python3 -m pip install PyYAML==6.0
    python3 -m pip install websockets==11.0.2
 
Moreover, depending on the utilized platform functions potentially also pyzbar, opencv-python, numpy, and Pillow are required for the data processing function library:

    python3 -m pip install pyzbar==0.1.9
    python3 -m pip install opencv-python==4.5.5.64
    python3 -m pip install numpy==1.20.1
    python3 -m pip install Pillow==9.1.0

### Prepare Angular

For the platform management user interface, Angular 14, express 4.18.1 and cors 2.8.5 are required. 

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
        port = managementUiPort
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

Start the platform server(s) component through the generated scripts in the gen folder. If you run all processes from the console as explained, this requires a separate shell for platform, ECS Runtime, service manager and UI/CLI as the processes run intentionally endless.

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

    docker pull 147.172.178.145:5001/allapps/plcnext:0.1.0

with the address of your server, the port of the local docker registry and the name of the container. [AHMAD schema of container names; above must be the ECS]

AHMAD please add script info here

## Deploying applications

Depending on your installation, the application may be distributed/executed by the platform or requires manual actions.

### Manual deployment

Use the CLI as shown in [the platform installation document](../documentation/INSTALL.md) to add the application artifact from gen/*applicationId*/target to the devices and to start the services. Please note that artifacts and containers are added through their URI, whereby local URIs may differ from system to system, e.g., 
* Windows: `file:///C:/.../SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar`
* Linux: `file:/home/user/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar`
* Service container: `file:/apps/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar`

### Deployment-plan based deployment

The platform instantiation copies the application binaries into the configured artifacts folder, from where it is available through the specified URI prefix and the server of the management user interface or your server installation.

You may now create a deployment plan for your application (assigning services to devices using their respective device identifications) mentioning the application artifact. 
* For UI-based deployment, please place the deployment plan into the artifacts directory. The platform will take it up, display it in the deployments plan sections where you can start the plan.
* For a CLI-based deployment, please use the deploy and undeploy commands with the URI of the plan.


# Further contents
  
Besides the Maven build specifications for the platform dependencies, the test broker and the IIP-Ecosphere platform components, this Installation bundle also contains build information for two containers, namely an application container including the service manager and a simple application as well as a standalone container including the ECS runtime. Both containers can be build upon the artifacts provided through this installation package.

If you need to update the configuration model before instantiating the platform, use `mvn install -Dunpack.force=true`. You may also adjust the name of the model (user property `-Diip.model=`) the directory the model is located in (user property `-Diip.modelDir=`) or the output directory (user property `-Diip.outputDir=`).

This package contains also programs to optimize specific deployments, e.g., to reduce the overlap between JAR folders.
