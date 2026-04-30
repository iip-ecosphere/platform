# oktoflow platform: Install Support (Linux)

## Prerequisites
For the platform to work, ensure that the required software is installed:
- Java JDK (version 17 or higher, due to limitations of certain dependencies)
- Apache Maven (version 3.9.7)
- Python (version 3.9.21)

>Please note that the software versions may have changed. The most current versions are listed in the platform prerequisites documentation: [Prerequisites](https://github.com/iip-ecosphere/platform/blob/main/platform/documentation/PREREQUISITES.md).

```bash
# Install Java JDK 17
sudo apt install openjdk-17-jdk-headless -y

# Install Maven 3.9.7
sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.tar.gz
sudo tar xzpvf apache-maven-3.9.7-bin.tar.gz
sudo ln -s $PWD/apache-maven-3.9.7/bin/mvn /usr/bin/mvn
```

Please also set the environment variable `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/`. The actual path of your Java installation may vary depending on your processor architecture, i.e., it could also end with `java-17-openjdk-i386/`. To be on the safe side, please set also `M2_HOME` to the installation directory of maven and, if you plan to use a different local repository than `~/.m2/repository` then please set `M2_REPO` to that directory as oktoflow may rely at runtime on the libraries stored there.

If Python 3.9 is not installed, please execute:
```bash
# Install Python 3.9 (e,g., for Ubuntu)
sudo apt update -y
sudo apt install software-properties-common -y
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt update 
sudo apt install python3.9 -y
sudo apt install -y python3.9-distutils
```

In case your OS does not support Python 3.9, try installing the source code and compiling it, or consider using a newer Python version.

If pip is not working for Python 3.9, please execute

```bash
sudo apt update -y
sudo wget https://bootstrap.pypa.io/get-pip.py
sudo python3.9 get-pip.py
```

Also install the required python packages

```bash
sudo python3.9 -m pip install numpy==1.20.1
sudo python3.9 -m pip install pickle4
sudo python3.9 -m pip install pyflakes==3.3.2
sudo python3.9 -m pip install PyYAML==6.0
sudo python3.9 -m pip install websockets==11.0.2
```

> Multiple python versions
> If more than one Python version is installed, please ensure that you install the packages into the right version, e.g., by prefixing `python3.9` with the actual path. In this case, please also set the environment variable ``IIP_PYTHON`` to the intended Python executable.
>
> ```bash
> which python3.9
> export IIP_PYTHON=$(which python3.9)
> $IIP_PYTHON -version
> ```

### Verify installation

After installing the required software, verify that the correct versions are being used:

```bash
java -version
mvn -version
$IIP_PYTHON -version
```

## Docker

Docker is not a required prerequisite, but if we want to use Docker containers provided by the platform, or to create Docker images for applications that require Docker.

We recommend using Docker version 28.1.1, please execute

```bash
# Install prerequisites
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release

# Detect OS and codename
REPO_URL="https://download.docker.com/linux"
source /etc/os-release
OS_ID="${ID,,}"
CODENAME="${VERSION_CODENAME:-$(lsb_release -cs)}"

# Add Docker GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL $REPO_URL/$OS_ID/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Add Docker repo
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  $REPO_URL/$OS_ID $CODENAME stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update

# Dynamically find the correct full version string
DOCKER_PKG_VERSION=$(apt-cache madison docker-ce | grep "28.1.1" | head -n1 | awk '{print $3}')

# Install Docker
sudo apt install -y \
  docker-ce="$DOCKER_PKG_VERSION" \
  docker-ce-cli="$DOCKER_PKG_VERSION" \
  containerd.io

# Enable + start service
sudo systemctl enable docker
sudo systemctl start docker
```

By default, Docker requires root permissions to execute functions. If you want to use docker as “normal” user, execute

     sudo usermod -aG docker $USER

### Verify installation

```bash
docker --version
```

Log out and log back so that your group membership is re-evaluated. 
 
For distributing containers created by the platform, we need a local Docker registry, in particular if services contain IPR-protected code. We install the registry on the server on port 5001 as follows:

    docker run -d \
      --restart=always \
      --name registry \
      -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 \
      -p 5001:5001 \
      registry:2

In general, we recommend using a distinct server for this and to adjust the settings in the platform configuration.

You may check if the Docker registry is running with:
    
    curl -sS http://0.0.0.0:5001/v2/_catalog

#### Handling existing installations

If another version of Java or Maven is already installed on your system, you have two options:

1. Remove the existing installation and install the required version.
2. Keep the existing installation and configure your environment to use the required version for the platform.

For example, to use a locally installed Maven version:
```bash
export M2_HOME=/path/to/apache-maven-3.9.7
export PATH=$M2_HOME/bin:$PATH
```

Or the desired Java version:
```bash
export JAVA_HOME=/path/to/jdk-17
export PATH=$JAVA_HOME/bin:$PATH
```

>Note: These environment variable settings apply only to the current shell session. To make them permanent, add them to your ~/.bashrc or ~/.profile.

### Distributed Setup
Distributed setup refers to the execution of the platform components on edge devices such as PLCs, instead of a single central machine. The execution can be directly on the device OS or in containers. 

#### On-device execution
On devices, the installation may differ depending on the desired degree of containerization. If you want to run the fundamental platform components on bare metal, please install also JDK, maven, Python, Python packages and docker on the devices - see [Prerequisites](https://github.com/iip-ecosphere/platform/blob/main/platform/documentation/PREREQUISITES.md) for the current software versions. 

#### Container execution
If you plan to run only containers on the devices, docker is sufficient.

Create or edit ``/etc/docker/daemon.json`` in order to access the local Docker registry installed above, here without TLS certificates ("147.172.178.145" should be changed to the IP address of the machine running the Docker registry). For an empty file, this looks like:

    {
      "insecure-registries" : ["147.172.178.145:5001"]
    }
    
Then execute

    sudo systemctl restart docker

#### Prepare Python

Depending on the use of Python in services, the build process for applications may include Python syntax checking and execution of Python unit tests. If you plan to run the platform on bare metal under administrator permissions, e.g., as systemd service, please install the following Python dependencies globally, i.e., with administrator permissions.

At least pyflakes and for service execution PyYaml as well as websockets must be installed (Please check the tested library for each version of Python [Prerequisites](https://github.com/iip-ecosphere/platform/blob/1ec5cd2c31d1ff26b0862d6d5d0c1dce31172d55/platform/documentation/PREREQUISITES.md))

    sudo python3 -m pip install pyflakes==2.5.0
    sudo python3 -m pip install PyYAML==6.0
    sudo python3 -m pip install websockets==11.0.2
 
Moreover, depending on the utilized platform functions potentially also pyzbar, opencv-python, numpy, and Pillow are required for the data processing function library:

    sudo python3 -m pip install numpy==1.20.1
    sudo python3 -m pip install pyzbar==0.1.9
    sudo python3 -m pip install opencv-python==4.5.5.64
    sudo python3 -m pip install Pillow==9.1.0
    sudo python3 -m pip install pickle4

### Prepare Angular

If you intend to use the platform management user interface, Angular 19, express 19.2.5 and cors 2.8.5 are required on the server. 
Installation of nodejs. Ubuntu 20.4.1 provides older version of Nodejs. We need to first remove it to install v.22.x:

    sudo apt remove -y nodejs
    curl -fsSL https://deb.nodesource.com/setup_22.x | sudo bash -
	sudo apt update
	sudo apt install -y nodejs 
	
	# checking the version
	node --version
    
    sudo npm install -g @angular/cli@19.2.5
    sudo npm install -g express@4.18.1 
    sudo npm install -g cors@2.8.5

