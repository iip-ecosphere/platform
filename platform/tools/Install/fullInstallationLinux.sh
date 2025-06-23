#!/bin/bash

set -e

# Installation functions

install_docker_version() {
  DOCKER_SHORT_VERSION="$1"
  if [[ -z "$DOCKER_SHORT_VERSION" ]]; then
    echo "[ERROR] No Docker version provided."
    echo "Usage: install_docker_version <version>, e.g., install_docker_version 20.10.7"
    return 1
  fi

  # Convert to full APT version prefix
  DOCKER_VERSION="5:${DOCKER_SHORT_VERSION}~3-0"
  REPO_URL="https://download.docker.com/linux"

  # Detect OS and codename
  source /etc/os-release
  OS_ID="${ID,,}"
  CODENAME="${VERSION_CODENAME:-$(lsb_release -cs)}"

  echo "[INFO] Detected OS: $OS_ID, Codename: $CODENAME"

  # Compose full package version
  if [[ "$OS_ID" == "debian" || "$OS_ID" == "ubuntu" ]]; then
    DOCKER_PKG_VERSION="${DOCKER_VERSION}~${OS_ID}-${CODENAME}"
  else
    echo "[ERROR] Unsupported OS: $OS_ID"
    return 1
  fi

  sudo apt update
  sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release wget

  sudo mkdir -p /etc/apt/keyrings
  curl -fsSL $REPO_URL/$OS_ID/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] $REPO_URL/$OS_ID \
    $CODENAME stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

  sudo apt update

  if apt-cache madison docker-ce | grep -q "$DOCKER_PKG_VERSION"; then
    echo "[INFO] Found Docker $DOCKER_PKG_VERSION in APT. Installing..."
    sudo apt install -y \
      docker-ce="$DOCKER_PKG_VERSION" \
      docker-ce-cli="$DOCKER_PKG_VERSION" \
      containerd.io
  else
    DOCKER_VERSION="${DOCKER_SHORT_VERSION}~3-0"
    DOCKER_PKG_VERSION="${DOCKER_VERSION}~${OS_ID}-${CODENAME}"

    mkdir -p docker-debs && cd docker-debs

    # Build Debian package URLs using Bullseye as fallback
    BASE_URL="https://download.docker.com/linux/debian/dists/bullseye/pool/stable/amd64"
    echo "[INFO] Downloading .deb packages for Docker $DOCKER_SHORT_VERSION..."
    wget "$BASE_URL/containerd.io_1.4.6-1_amd64.deb"
    wget "$BASE_URL/docker-ce-cli_${DOCKER_VERSION}~debian-bullseye_amd64.deb"
    wget "$BASE_URL/docker-ce_${DOCKER_VERSION}~debian-bullseye_amd64.deb"
    wget "$BASE_URL/docker-ce-rootless-extras_${DOCKER_VERSION}~debian-bullseye_amd64.deb"

    echo "[INFO] Installing downloaded .deb packages..."
    sudo apt install -y ./containerd.io_1.4.6-1_amd64.deb \
                        ./docker-ce-cli_${DOCKER_VERSION}~debian-bullseye_amd64.deb \
                        ./docker-ce_${DOCKER_VERSION}~debian-bullseye_amd64.deb \
                        ./docker-ce-rootless-extras_${DOCKER_VERSION}~debian-bullseye_amd64.deb

    cd ..
    # rm -rf docker-debs  # Optional cleanup
  fi

  sudo systemctl enable docker
  sudo systemctl start docker

  echo "Docker $DOCKER_SHORT_VERSION installation completed."
}

install_python_binary_version() {
    sudo apt update -y
    sudo apt install software-properties-common -y
    sudo echo | add-apt-repository ppa:deadsnakes/ppa
    sudo apt update -y
    sudo apt install python3.9 -y
    sudo wget https://bootstrap.pypa.io/get-pip.py
    export IIP_PYTHON=$(which python3.9)
    sudo $IIP_PYTHON get-pip.py
    echo "Python 3.9 installation completed."
}

install_python_compile_sources_version() {
    PYTHON_SHORT_VERSION="$1"
    sudo apt update -y
    sudo apt install -y build-essential libssl-dev zlib1g-dev \
        libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
        libncurses5-dev libncursesw5-dev xz-utils tk-dev libffi-dev \
        libgdbm-dev liblzma-dev uuid-dev
    sudo mkdir -p $CurPath/PyPaths/sources/python3.9
    cd $CurPath/PyPaths/sources/python3.9
    sudo wget https://www.python.org/ftp/python/${PYTHON_SHORT_VERSION}/Python-${PYTHON_SHORT_VERSION}.tgz
    sudo tar xzf Python-${PYTHON_SHORT_VERSION}.tgz
    cd Python-3.9.21
    sudo ./configure --prefix=$CurPath/PyPaths/python3.9 --enable-optimizations --with-ensurepip=install
    sudo make -j$(nproc)
    sudo make altinstall
    echo "Python $PYTHON_SHORT_VERSION installation completed."
}

install_confirm() {
  local name="$1"
  local yn="$2"
  local version="$3"

  case "$yn" in
    [Yy])
      echo "Installing $name $version"
      ;;
    [Ee])
      echo "$name is already installed with accepted version, $name $version"
      ;;
  esac
}

# End of installation function

# Start of platform installation

echo "Oktoflow platform installation (Linux)"
echo "For installing prerequisites, administrator permissions may be required!"

OktJavaVersion=17
OktMvnVersion=3.9.7
OktPythonVersion=3.9.21
OktDockerVersion=20.10.7
OktNodeVersion=22.14.0
OktNpmVersion=10.9.2
OktAngularVersion=19.2.5

echo "Installing prerequisites Java $OktJavaVersion, Maven version $OktMvnVersion, and Python version $OktPythonVersion"
echo "This action will set and use Environment Variables"
read -p "Do you want to install the prerequisites (skip only if already installed)? (y/n) " yn
if [ $yn == "y" ] || [ $yn == "Y" ]; then 
    
    mkdir -p Setup && cd Setup
    
    # Check current Java version 
    
    if [ -x "$(command -v java -version)" ]; then
        JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' || true)
        if [ $JAVA_VERSION -lt $OktJavaVersion ]; then
            echo "The Java version you have is less than the minimum requirement Java $OktJavaVersion for the IIP-Ecosphere platform"
            echo "Your Java version is "$JAVA_VERSION
            while true; do
                read -p "Do you want to install Java $OktJavaVersion - you should have at least Java $OktJavaVersion? - You can't skip this step; if you skip it the installation will end. (y/n) " Javayn
                case $Javayn in
                    [Yy]* ) break;;
                    [Nn]* ) exit;;
                    * ) echo "Please answer y or n.";;
                esac
            done
        else
            Javayn="E";
        fi    
    else
        Javayn="Y";
    fi
    
    # Check current Maven version
    
    if [ -x "$(command -v mvn -version)" ]; then
        MVN_VERSION=$(mvn -version | grep "Apache Maven" | grep -oP '[[:digit:]]\.[[:digit:]]\.[[:digit:]]')
        if ! [ $MVN_VERSION == $OktMvnVersion ]; then
            echo "You have Maven version $MVN_VERSION, it is recommended to have version $OktMvnVersion"
            while true; do
                read -p "Do you want to install Maven $OktMvnVersion and replace Maven default to $OktMvnVersion? - You might skip this step (y/n) " Mavenyn
                case $Mavenyn in
                    [Yy]* ) break;;
                    [Nn]* ) break;;
                    * ) echo "Please answer y or n.";;
                esac
            done
        else
            Mavenyn="E";
        fi
    else
        Mavenyn="Y";
    fi
    
    # Check current Python version

    echo "Please enter the path for runnable python3.9 or newer. If python 3.9 or newer then please enter (n). "
    read -p "Runnable python3.9 or newer: " PythonPath
    while [ -z "$PythonPath" ]; do
        echo "Please enter the path for runnable python3.9 or newer. If python 3.9 or newer then please enter (n). "
        read -p "Runnable python3.9 or newer: " PythonPath
    done

    if [[ "$PythonPath" == "Y" || "$PythonPath" == "y" ]]; then
        while true; do
            read -p "You have Python installed in $PythonPath, correct (y/n)? " CheckPath
            case $CheckPath in
                [Yy]* ) break;;
                [Nn]* ) read -p "please enter the correct path for runnable python3.9 or newer: " PythonPath;;
                * ) echo "Please answer y or n.";;
            esac
        done
    fi

    if [ -x "$(command -v $PythonPath --version)" ]; then
        PYTHON_VERSION=$($PythonPath --version | grep -oP '[[:digit:]]{1,2}\.[[:digit:]]{1,2}\.[[:digit:]]{1,2}')
        if ! [ $PYTHON_VERSION == $OktPythonVersion ]; then
            echo "You have Python version $PYTHON_VERSION, it is recommended to have version $OktPythonVersion"
            while true; do
                read -p "Do you want to install Python $OktPythonVersion? - You might skip this step (y/n) " Pythonyn
                case $Pythonyn in
                    [Yy]* ) break;;
                    [Nn]* ) break;;
                    * ) echo "Please answer y or n.";;
                esac
            done

            if [[ "$Pythonyn" == "Y" || "$Pythonyn" == "y" ]]; then
                echo "Please choose: (A) Take the most recent 3.9.x (untested), (B) Download source and compile 3.9.21 (It takes more time)."
                while true; do
                    read -p "Please choose how to install python (A/B): " PythonInstallModeab
                    os_info=$(grep "^PRETTY_NAME=" /etc/os-release | cut -d= -f2- | tr -d '"')
                    if [[ "$os_info" == *"Debian GNU/Linux"* && ( "$PythonInstallModeab" == "A" || "$PythonInstallModeab" == "a" ) ]]; then
                        echo "Detected Debian: $os_info. Debian does not support method (A) to install 3.9.x"
                    else
                        case $PythonInstallModeab in
                            [Aa]* ) break;;
                            [Bb]* ) break;;
                            * ) echo "Please answer A or B.";;
                        esac
                    fi
                done
            fi
        else
            Pythonyn="E";
        fi
    else
        Pythonyn="Y";
        echo "Please choose: (A) Take the most recent 3.9.x (untested), (B) Download source and compile 3.9.21 (It takes more time)."
        while true; do
            read -p "Please choose how to install python (A/B): " PythonInstallModeab
            os_info=$(grep "^PRETTY_NAME=" /etc/os-release | cut -d= -f2- | tr -d '"')
            if [[ "$os_info" == *"Debian GNU/Linux"* && ( "$PythonInstallModeab" == "A" || "$PythonInstallModeab" == "a" ) ]]; then
                echo "Detected Debian: $os_info. Debian does not support method (A) to install 3.9.x"
            else
                case $PythonInstallModeab in
                    [Aa]* ) break;;
                    [Bb]* ) break;;
                    * ) echo "Please answer A or B.";;
                esac
            fi
        done
    fi

    while true; do
        read -p "Do you want to install Docker $OktDockerVersion to use containers in the platform? - You might skip this step (y/n) " InstallDockeryn
        case $InstallDockeryn in
            [Yy]* ) break;;
            [Nn]* ) break;;
            * ) echo "Please answer y or n.";;
        esac
    done
    
    if [ $InstallDockeryn == "y" ] || [ $InstallDockeryn == "Y" ]; then 
        # Check current Docker version 

        if [ -x "$(command -v docker --version)" ]; then
            DOCKER_VERSION=$(docker --version | grep -oP '[[:digit:]]{1,2}\.[[:digit:]]{1,2}\.[[:digit:]]{1,2}' | head -1)
            if ! [ $DOCKER_VERSION == $OktDockerVersion ]; then
                echo "You have Docker version $DOCKER_VERSION, it is recommended to have version $OktDockerVersion"
                while true; do
                    read -p "Do you want to install Docker $OktDockerVersion and replace Docker default to $OktDockerVersion? - You might skip this step (y/n) " Dockeryn
                    case $Dockeryn in
                        [Yy]* ) break;;
                        [Nn]* ) break;;
                        * ) echo "Please answer y or n.";;
                    esac
                done
            else
                Dockeryn="E";
            fi
        else
            Dockeryn="Y";
        fi
    else
        Dockeryn="N";
    fi
    
    # Check Docker Private Registry
    
    while true; do
        read -p "Do you want to start Docker Private Registry (You should have Docker installed, be carefull it will restart Docker service) for the platform? - You might skip this step (y/n) " Registryyn
        case $Registryyn in
            [Yy]* ) break;;
            [Nn]* ) break;;
            * ) echo "Please answer y or n.";;
        esac
    done
   
    # Check Node.js 22.x and Angular 19
    
    while true; do
        echo "To use the management UI for the platform you should have angular version $OktAngularVersion installed (with npm and Node.js)"
        read -p "Do you want to install Node.js version 22.x (latest), angular version $OktAngularVersion, and npm package manager for the JavaScript? - You might skip this step (y/n) " Nodeyn 
        case $Nodeyn in
            [Yy]* ) break;;
            [Nn]* ) break;;
            * ) echo "Please answer y or n.";;
        esac
    done   
    
    if [[ "$Nodeyn" == "Y" || "$Nodeyn" == "y" ]]; then
        if [ -x "$(command -v node --version)" ]; then
            NODE_VERSION=$(node --version | grep -oP '[[:digit:]]{1,2}\.[[:digit:]]{1,2}\.[[:digit:]]{1,2}' | head -1)
            if [[ "$NODE_VERSION" == "$OktNodeVersion" ]]; then
                Nodeyn="E";
            elif [[ "$(printf '%s\n' "$NODE_VERSION" "$OktNodeVersion" | sort -V | head -n1)" == "$version_a" ]] && [[ "$version_a" != "$version_b" ]]; then
                echo "You have Node.js version $NODE_VERSION, it is recommended to have at least version $OktNodeVersion"
                while true; do
                    read -p "Do you want to install Node.js 22.x (latest) version? - You might skip this step (y/n) " Nodeyn
                    case $Nodeyn in
                        [Yy]* ) break;;
                        [Nn]* ) break;;
                        * ) echo "Please answer y or n.";;
                    esac
                done
            else
                echo "You have Node.js version $NODE_VERSION, it is newer than $OktNodeVersion. It shall work"
                Nodeyn="E";
            fi
        else
            Nodeyn="Y";
        fi
        
        if [ -x "$(command -v ng --version)" ]; then
           ANG_VERSION=$(ng version | grep 'Angular CLI:' | awk '{print $3}')
           if ! [[ "$ANG_VERSION" == "$OktAngularVersion" ]]; then
              echo "You have Angular version $ANG_VERSION, it is recommended to have version $OktAngularVersion"
              while true; do
                  read -p "Do you want to install Angular $OktAngularVersion? - You might skip this step (y/n) " Angularyn
                  case $Angularyn in
                      [Yy]* ) break;;
                      [Nn]* ) break;;
                      * ) echo "Please answer y or n.";;
                  esac
              done
           else
               Angularyn="E";
           fi
        else
            Angularyn="Y";
        fi
    fi

    install_confirm "Java" "$Javayn" "$OktJavaVersion"
    install_confirm "Maven" "$Mavenyn" "$OktMvnVersion"
    install_confirm "Docker" "$Dockeryn" "$OktDockerVersion"
    install_confirm "Python" "$Pythonyn" "$OktPythonVersion"
    install_confirm "Node.js" "$Nodeyn" "$OktNodeVersion"
    install_confirm "Angular" "$Angularyn" "$OktAngularVersion"

    if [[ "$Registryyn" == "Y" || "$Registryyn" == "y" ]]; then
      echo "Starting Docker Private Registry"
    fi
    echo "Installing the Platform"

    read -p "Press Enter to start the installation..."

    # Install Java version 17
    
    sudo apt-get update
    sudo apt install unzip -y
    sudo apt install jq -y
    
    if ! [ -x "$(command -v java -version)" ]; then
        sudo apt install openjdk-17-jdk-headless -y
    else
        case $Javayn in
            [Yy]* ) sudo apt install openjdk-17-jdk-headless -y; break;;
            [Nn]* ) exit;;
        esac
    fi
    
    # Install Maven version 3.9.7
    
    if ! [ -x "$(command -v mvn -version)" ]; then
        sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.tar.gz
        sudo tar xzpvf apache-maven-3.9.7-bin.tar.gz
    
        sudo ln -s $PWD/apache-maven-3.9.7/bin/mvn /usr/bin/mvn
    else
        case $Mavenyn in
            [Yy]* ) sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.tar.gz;
                    sudo tar xzpvf apache-maven-3.9.7-bin.tar.gz;
                    sudo ln -sf $PWD/apache-maven-3.9.7/bin/mvn /usr/bin/mvn;;
            [Nn]* ) break;;
        esac
    fi
    
    # Install Docker version 20.10.7
    
    if ! [ -x "$(command -v docker --version)" ]; then
        install_docker_version $OktDockerVersion
    
        sudo usermod -aG docker $USER
    else
        case $Dockeryn in
            [Yy]* ) install_docker_version $OktDockerVersion;
                    sudo usermod -aG docker $USER;;
            [Nn]* ) break;;
        esac
    fi
    
    # Install Python version 3.9
    
    if [[ "$PythonInstallModeab" == "A" || "$PythonInstallModeab" == "a" ]]; then
        if ! [ -x "$(command -v $PythonPath --version)" ]; then
            install_python_binary_version
            export IIP_PYTHON=$(which python3.9)
            sudo $IIP_PYTHON -m pip install pyflakes
        else
            case $Pythonyn in
                [Yy]* ) install_python_binary_version;
                        export IIP_PYTHON=$(which python3.9);
                        sudo $IIP_PYTHON -m pip install pyflakes;;
                [Nn]* ) break;;
            esac
        fi
        echo "export IIP_PYTHON=$(which python3.9)" >> ~/.bashrc
    elif [[ "$PythonInstallModeab" == "B" || "$PythonInstallModeab" == "b" ]]; then
        CurPath=$PWD
        if ! [ -x "$(command -v $PythonPath --version)" ]; then
            install_python_compile_sources_version $OktPythonVersion
            export IIP_PYTHON=$CurPath/PyPaths/python3.9/bin/python3.9
            sudo $IIP_PYTHON -m pip install pyflakes
        else
            case $Pythonyn in
                [Yy]* ) install_python_compile_sources_version $OktPythonVersion;
                        export IIP_PYTHON=$CurPath/PyPaths/python3.9/bin/python3.9;
                        sudo $IIP_PYTHON -m pip install pyflakes;;
                [Nn]* ) break;;
            esac
        fi
        echo "export IIP_PYTHON=$CurPath/PyPaths/python3.9/bin/python3.9" >> ~/.bashrc
        cd $CurPath
    fi
    
    # Install Node.js version 22.x.0 and angular version 19
    
    case $Nodeyn in
        [Yy]* ) sudo apt update && apt install -y curl gnupg && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && apt install -y nodejs;;
        [Nn]* ) break;;
    esac

    case $Angularyn in
        [Yy]* ) sudo npm install -g @angular/cli@19.2.5;;
        [Nn]* ) break;;
    esac
    
    sudo apt install curl -y
    
    localIP=$(hostname -I | cut -d ' ' -f1)

    cd ..
    
    cd platformDependencies/
    $IIP_PYTHON -m pip install -r requirements.txt
    cd ..
            
    sed -i 's/147.172.178.145/'$localIP'/g' src/main/easy/TechnicalSetup.ivml
    
    # Run Docker Private Registry
    
    generationLineNumber=$(cat src/main/easy/TechnicalSetup.ivml | grep -n "generation setup" | cut -d ' ' -f1 | sed 's/:/ /g')
    ((generationLineNumber=generationLineNumber-1))

cat >daemon.json <<EOF
{
  "insecure-registries" : ["$localIP:5001"]
}
EOF
                     
    case $Registryyn in
        [Yy]* ) sudo sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ registry = "'$localIP':5001"' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ containerManager = DockerContainerManager {' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ // ---------- Registry ------------' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml;
                
                sudo mv daemon.json /etc/docker/;
                sudo systemctl restart docker;
                sudo docker  run -d \
                     --restart=always \
                     --name registry \
                     -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 \
                     -p 5001:5001 \
                     registry:2;;
        [Nn]* ) sudo rm daemon.json;
    esac
    
    sudo mvn install -Diip.easy.tracing=TOP
    
elif [ $yn == "n" ] || [ $yn == "N" ]; then 

    mkdir -p Setup && cd Setup
    
    echo "Please enter the path for runnable python3.9 or newer."
    read -p "Runnable python3.9: " PythonPath
    while [ -z "$PythonPath" ]; do
        echo "Please enter the path for runnable python3.9 or newer."
        read -p "Runnable python3.9: " PythonPath
    done

    while true; do
        read -p "You have runnable Python in $PythonPath, correct (y/n)? " CheckPath
        case $CheckPath in
            [Yy]* ) break;;
            [Nn]* ) read -p "please enter the correct path for runnable python3.9: " PythonPath;;
            * ) echo "Please answer y or n.";;
        esac
    done
    
    export IIP_PYTHON=$PythonPath
    
    # Check Docker Private Registry
    if [ -x "$(command -v docker --version)" ]; then
       while true; do
           read -p "Do you want to start Docker Private Registry (You should have Docker installed, be carefull it will restart Docker service) for the platform? - You might skip this step (y/n) " Registryyn
           case $Registryyn in
               [Yy]* ) break;;
               [Nn]* ) break;;
               * ) echo "Please answer y or n.";;
           esac
       done
    else
       Registryyn=n
    fi

    # Check Node.js 22.x and Angular 19
    
    while true; do
        echo "To use the management UI for the platform you should have angular version $OktAngularVersion installed (with npm and Node.js)"
        read -p "Do you want to install Node.js version 22.x (latest), angular version $OktAngularVersion, and npm package manager for the JavaScript? - You might skip this step (y/n) " Nodeyn 
        case $Nodeyn in
            [Yy]* ) break;;
            [Nn]* ) break;;
            * ) echo "Please answer y or n.";;
        esac
    done   
    
    if [[ "$Nodeyn" == "Y" || "$Nodeyn" == "y" ]]; then
        if [ -x "$(command -v node --version)" ]; then
            NODE_VERSION=$(node --version | grep -oP '[[:digit:]]{1,2}\.[[:digit:]]{1,2}\.[[:digit:]]{1,2}' | head -1)
            if [[ "$NODE_VERSION" == "$OktNodeVersion" ]]; then
                Nodeyn="E";
            elif [[ "$(printf '%s\n' "$NODE_VERSION" "$OktNodeVersion" | sort -V | head -n1)" == "$version_a" ]] && [[ "$version_a" != "$version_b" ]]; then
                echo "You have Node.js version $NODE_VERSION, it is recommended to have at least version $OktNodeVersion"
                while true; do
                    read -p "Do you want to install Node.js 22.x (latest) version? - You might skip this step (y/n) " Nodeyn
                    case $Nodeyn in
                        [Yy]* ) break;;
                        [Nn]* ) break;;
                        * ) echo "Please answer y or n.";;
                    esac
                done
            else
                echo "You have Node.js version $NODE_VERSION, it is newer than $OktNodeVersion. It shall work"
                Nodeyn="E";
            fi
        else
            Nodeyn="Y";
        fi
        
        if [ -x "$(command -v ng --version)" ]; then
           ANG_VERSION=$(ng version | grep 'Angular CLI:' | awk '{print $3}')
           if ! [[ "$ANG_VERSION" == "$OktAngularVersion" ]]; then
              echo "You have Angular version $ANG_VERSION, it is recommended to have version $OktAngularVersion"
              while true; do
                  read -p "Do you want to install Angular $OktAngularVersion? - You might skip this step (y/n) " Angularyn
                  case $Angularyn in
                      [Yy]* ) break;;
                      [Nn]* ) break;;
                      * ) echo "Please answer y or n.";;
                  esac
              done
           else
               Angularyn="E";
           fi
        else
            Angularyn="Y";
        fi
    fi

    install_confirm "Node.js" "$Nodeyn" "$OktNodeVersion"
    install_confirm "Angular" "$Angularyn" "$OktAngularVersion"

    if [[ "$Registryyn" == "Y" || "$Registryyn" == "y" ]]; then
      echo "Starting Docker Private Registry"
    fi
    echo "Installing the Platform"

    read -p "Press Enter to start the installation..."

    sudo apt-get update
    sudo apt install unzip -y
    sudo apt install jq -y
   
    # Install Node.js version 22.x.0 and angular version 19
    
    case $Nodeyn in
        [Yy]* ) sudo apt update && apt install -y curl gnupg && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && apt install -y nodejs;;
        [Nn]* ) break;;
    esac

    case $Angularyn in
        [Yy]* ) sudo npm install -g @angular/cli@19.2.5;;
        [Nn]* ) break;;
    esac
    
    sudo apt install curl -y
    
    localIP=$(hostname -I | cut -d ' ' -f1)

    cd ..
    
    cd platformDependencies/
    $IIP_PYTHON -m pip install -r requirements.txt
    cd ..

    sed -i 's/147.172.178.145/'$localIP'/g' src/main/easy/TechnicalSetup.ivml

    # Run Docker Private Registry
    
    generationLineNumber=$(cat src/main/easy/TechnicalSetup.ivml | grep -n "generation setup" | cut -d ' ' -f1 | sed 's/:/ /g')
    ((generationLineNumber=generationLineNumber-1))
    
cat >daemon.json <<EOF
{
  "insecure-registries" : ["$localIP:5001"]
}
EOF
                     
    case $Registryyn in
        [Yy]* ) sudo sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ registry = "'$localIP':5001"' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ containerManager = DockerContainerManager {' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ // ---------- Registry ------------' src/main/easy/TechnicalSetup.ivml;
                sudo sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml;
                
                sudo mv daemon.json /etc/docker/;
                sudo systemctl restart docker;
                sudo docker  run -d \
                     --restart=always \
                     --name registry \
                     -e REGISTRY_HTTP_ADDR=0.0.0.0:5001 \
                     -p 5001:5001 \
                     registry:2;;
        [Nn]* ) sudo rm daemon.json;;
    esac
    
    sudo mvn install -Diip.easy.tracing=ALL

else
    echo "Please answer y or n.";
fi

echo "The following commands were created in Platform\Install\gen:"
echo "- broker/broker.sh starts the configured communication broker (cd needed)"
echo "- platform.sh starts the central platform services"
echo "- mgtUi.sh starts the Angular-based management UI (Angular required, http://localhost:4200)"
echo "- per device that shall execute services, either ECS-Runtime and service manager or the combined"
echo "  combined ECS-Runtime-Servicemanager must be executed"
echo "  - ecs.sh starts the ECS-Runtime"
echo "  - serviceMgr.sh starts the service manager"
echo "  - ecsServiceMgr.sh starts the combined ECS-Runtime/Service-Manager"
echo "- cli.sh starts the platform command line interface"
echo "In individual shells, start at least the broker, the central services and the device services, then" 
echo "the included application (cli.sh deploy artifacts/deployment.yaml). On a permanent installation, only" 
echo "accessing the UI or the CLI is needed."
echo "Please consult the installation overview for more information."
