# oktoflow platform: Install Support (Linux)

### Prepare the server

On the server, where we will build and instantiate the platform also for the devices, install the programs unzip, Java JDK (version 11 up to 16 due to some limitations of used dependencies), maven (version 3.6.3) and docker (version 20.10.2):

     sudo apt install unzip -y
     sudo apt install openjdk-13-jdk-headless -y
     sudo apt install maven -y
     sudo apt install docker.io -y

Please also set the environment variable `JAVA_HOME=/usr/lib/jvm/java-13-openjdk-amd64/`. The actual path of your Java installation may vary due to your processor architecture, i.e., it could also end with `java-13-openjdk-i386/`.

By default, Docker requires root permissions to execute functions. If you want to use docker as “normal” user, execute

     sudo usermod -aG docker $USER

Log out and log back so that your group membership is re-evaluated. 

For distributing containers created by the platform, we need a local Docker registry, in particular if services contain IPR-protected code. We install the registry on the server on port 5001 as follows:

    docker run -d \
      --restart=always \
      --name registry \
      -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 \
      -p 5001:5001 \
      registry:2

In general, we recommend using a distinct server for this and to adjust the settings in the platform configuration.

You may check the registry using
    
    curl -sS http://192.168.2.1:5001/v2/_catalog

If Python 3.9 is not installed, please execute
     
    sudo apt update -y
    sudo apt install software properties common -y
    sudo echo | add apt repository ppa:deadsnakes ppa
    sudo apt update -y
    sudo apt install python3.9 -y
    
Also install the required python packages

    sudo python3 m pip install scikit learn==0.23.2
    sudo python3 m pip install numpy ==1.20.1
    sudo python3 m pip install pickle==4.0
    sudo python3 m pip install pyflakes
    
If more than one python version is installed, please ensure that you install the packages into the right version, e.g., by prefixing `python3` with the actual path. In this case, please also set the environment variable ``IIP_PYTHON`` to the intended Python executable.

### Prepare the devices

On devices, the installation may differ depending on the desired degree of containerization. If you want to run the fundamental platform components on bare metal, please install also JDK 13, maven, python, python packages and docker on the devices. If you plan to run only containers on the devices, docker is sufficient.

Create or edit ``/etc/docker/daemon.json`` in order to access the local Docker registry installed above, here without TLS certificates. For an empty file, this looks like:

    {
      "insecure-registries" : ["147.172.178.145:5001"]
    }
    
Then execute

    sudo systemctl restart docker

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

    sudo curl -sL https://deb.nodesource.com/setup_14.x | sudo bash -;
    sudo apt -y install nodejs;
    sudo apt install npm -y;
    sudo npm install -g @angular/cli@14.2.10;
    sudo npm install -g express@4.18.1 
    sudo npm install -g cors@2.8.5