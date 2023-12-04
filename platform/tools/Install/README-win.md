# oktoflow platform: Install Support (Windows)

### Prepare the server

On the server, where we will build and instantiate the platform also for the devices, install the programs unzip, Java JDK (version 11 up to 16 due to some limitations of used dependencies), maven (version 3.6.3) and docker (version 20.10.2):

For Java 13, please execute

     curl https://download.java.net/openjdk/jdk13/ri/openjdk-13+33_windows-x64_bin.zip O openjdk-13+33_windows-x64_bin.zip
     tar xzpvf openjdk-13+33_windows-x64_bin.zip
     setx /M JAVA_HOME "%cd%\jdk-13"
     SET JAVA_HOME=%cd%\jdk-13
     setx /M Path "%Path%;%JAVA_HOME\bin"
     SET Path=%Path%;%JAVA_HOME%\bin

For Maven 3.6.3 is not installed, please execute

     curl https://archive.apache.org/dist/maven/maven3/3.6.3/binaries/apache-maven-3.6.3-bin.zip -O apache-maven-3.6.3-bin.zip
     tar xzpvf apache-maven-3.6.3-bin.zip
     setx /M MAVEN_HOME "%cd%\apache-maven-3.6.3“
     SET MAVEN_HOME=%cd%\apache-maven-3.6.3
     setx /M Path "%Path%;%MAVEN_HOME%\bin“
     SET Path=%Path%;%MAVEN_HOME%\bin
     
For Python 3.9, please execute 

    curl https://www.python.org/ftp/python/3.9.6/python-3.9.6-amd64.exe -O python-3.9.6-amd64.exe
    start /w "" "python-3.9.6-amd64.exe" install

If docker 20.10.7 (recommended version) is not installed, please execute 

    curl https://desktop.docker.com/win/main/amd64/65384/Docker%%20Desktop%%20Installer.exe -O DockerDesktopInstaller.exe
    rename "Docker%%20Desktop%%20Installer.exe" DockerDesktopInstaller.exe
    start /w "" "DockerDesktopInstaller.exe" install
    
Please note that for executing Linux containers on Windows (as the containers created by the platform) the Windows Sub system for Linux (WSL) is required. Please follow the [WSL Installation Instructions](https://ubuntu.com/tutorials/install).

For distributing containers created by the platform, we need a local Docker registry, in particular if services contain IPR-protected code. We install the registry on the server on port 5001 as follows:

    docker run -d \
      --restart=always \
      --name registry \
      -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 \
      -p 5001:5001 \
      registry:2

In general, we recommend using a distinct server for this and to adjust the settings in the platform configuration.

### Prepare the devices

On devices, the installation may differ depending on the desired degree of containerization. If you want to run the fundamental platform components on bare metal, please install also JDK 13, maven, python, python packages and docker on the devices. If you plan to run only containers on the devices, docker is sufficient.

Create or edit the ``daemon.json`` file (usually in ``C:\ProgramData\Docker\config\daemon.json``) in order to access the local Docker registry installed above, here without TLS certificates. For an empty file, this looks like:

    {
      "insecure-registries" : ["147.172.178.145:5001"]
    }
    
Then a restart of docker may be required.

### Prepare Python

Depending on the use of Python in services, the build process for applications may include Python syntax checking and execution of Python unit tests. If you plan to run the platform on bare metal under administrator permissions, e.g., as systemd service, please install the following python dependencies globally, i.e., with administrator permissions.

At least pyflakes and for service execution PyYaml as well as websockets must be installed. 

    python3 -m pip install pyflakes==2.5.0
    python3 -m pip install PyYAML==6.0
    python3 -m pip install websockets==11.0.2
 
Moreover, depending on the utilized platform functions potentially also pyzbar, opencv-python, numpy, and Pillow are required for the data processing function library:

    python3 -m pip install pyzbar==0.1.9
    python3 -m pip install opencv-python==4.5.5.64
    python3 -m pip install numpy==1.20.1
    python3 -m pip install Pillow==9.1.0
    python3 -m  pip install scikit learn==0.23.2
    python3 -m  pip install numpy ==1.20.1
    python3 -m  pip install pickle==4.0

### Prepare Angular

If you intend to use the platform management user interface, Angular 14, express 4.18.1 and cors 2.8.5 are required on the server. 

    curl https://nodejs.org/download/release/v16.10.0/node-v16.10.0-x64.msi -o node-v16.10.0-x64.msi
    node-v16.10.0-x64.msi
    setx /M Path "%Path%;C:\Program Files\nodejs"
    SET Path=%Path%;C:\Program Files\nodejs
    npm install -g @angular/cli@14.2.11
    npm install -g express@4.18.1 
    npm install -g cors@2.8.5
