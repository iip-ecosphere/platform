#!/bin/bash

echo "Oktoflow platform installation (Linux)"
echo "For installing prerequisites, administrator permissions may be required!"

mkdir -p Platform && cd Platform

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
    
    # Check current Python version

    echo "Please enter the path for runnable python3.9 or newer. If python 3.9 or newer then please enter (n). "
    read -p "Runnable python3.9: " PythonPath
    while [ -z "$PythonPath" ]; do
        echo "Please enter the path for runnable python3.9 or newer. If python 3.9 or newer then please enter (n). "
        read -p "Runnable python3.9: " PythonPath
    done

    if ! [[ "$PythonPath" == "N" || "$PythonPath" == "n" ]]; then
        while true; do
            read -p "You have Python $OktPythonVersion installed in $PythonPath, correct (y/n)? " CheckPath
            case $CheckPath in
                [Yy]* ) break;;
                [Nn]* ) read -p "please enter the correct path for runnable python3.9: " PythonPath;;
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
                    case $PythonInstallModeab in
                        [Aa]* ) break;;
                        [Bb]* ) break;;
                        * ) echo "Please answer A or B.";;
                    esac
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
            case $PythonInstallModeab in
                [Aa]* ) break;;
                [Bb]* ) break;;
                * ) echo "Please answer A or B.";;
            esac
        done
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

    if [[ "$Javayn" == "Y" || "$Javayn" == "y" ]]; then
        echo "Installing Java $OktJavaVersion"
    elif [[ "$Javayn" == "E" || "$Javayn" == "e" ]]; then
        echo "Java is already installed with accepted version, Java $JAVA_VERSION"
    fi 
    if [[ "$Mavenyn" == "Y" || "$Mavenyn" == "y" ]]; then
        echo "Installing Maven $OktMvnVersion"
    elif [[ "$Mavenyn" == "E" || "$Mavenyn" == "e" ]]; then
        echo "Maven is already installed with accepted version, Maven $MVN_VERSION"
    fi 
    if [[ "$Dockeryn" == "Y" || "$Dockeryn" == "y" ]]; then
        echo "Installing Maven $OktDockerVersion"
    elif [[ "$Dockeryn" == "E" || "$Dockeryn" == "e" ]]; then
        echo "Docker is already installed with accepted version, Docker $DOCKER_VERSION"
    fi 
    if [[ "$Pythonyn" == "Y" || "$Pythonyn" == "y" ]]; then
        echo "Installing Python $OktPythonVersion"
    elif [[ "$Pythonyn" == "E" || "$Pythonyn" == "e" ]]; then
        echo "Python is already installed with accepted version, Python $PYTHON_VERSION"
    fi 
    if [[ "$Nodeyn" == "Y" || "$Nodeyn" == "y" ]]; then
        echo "Installing Node.js 22.x (latest) version"
    elif [[ "$Nodeyn" == "E" || "$Nodeyn" == "e" ]]; then
        echo "Node.js is already installed with accepted version, Node.js $NODE_VERSION"
    fi 
    if [[ "$Angularyn" == "Y" || "$Angularyn" == "y" ]]; then
        echo "Installing Angular $OktAngularVersion"
    elif [[ "$Angularyn" == "E" || "$Angularyn" == "e" ]]; then
        echo "Angular is already installed with accepted version, Angular $ANG_VERSION"
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
        sudo apt-get update -y
        sudo apt-get install \
            ca-certificates \
            curl \
            gnupg \
            lsb-release -y
    
        sudo mkdir -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    
        echo \
          "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
          $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
        sudo apt-get update -y
    
        sudo apt-get install docker-ce=5:20.10.7~3-0~ubuntu-focal docker-ce-cli=5:20.10.7~3-0~ubuntu-focal containerd.io docker-compose-plugin -y
    
        sudo usermod -aG docker $USER
    else
        case $Dockeryn in
            [Yy]* ) sudo apt-get update -y;
                    sudo apt-get install \
                        ca-certificates \
                        curl \
                        gnupg \
                        lsb-release -y;

                    sudo mkdir -p /etc/apt/keyrings;
                    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg;

                    echo \
                      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
                      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null;

                    sudo apt-get update -y;
                    sudo apt-get install docker-ce=5:20.10.7~3-0~ubuntu-focal docker-ce-cli=5:20.10.7~3-0~ubuntu-focal containerd.io docker-compose-plugin -y;
                    sudo usermod -aG docker $USER;;
            [Nn]* ) break;;
        esac
    fi
    
    # Install Python version 3.9
    
    if [[ "$PythonInstallModeab" == "A" || "$PythonInstallModeab" == "a" ]]; then
        if ! [ -x "$(command -v $PythonPath --version)" ]; then
            sudo apt update -y
            sudo apt install software-properties-common -y
            sudo echo | add-apt-repository ppa:deadsnakes/ppa
            sudo apt update -y
            sudo apt install python3.9 -y
            sudo wget https://bootstrap.pypa.io/get-pip.py
            sudo export IIP_PYTHON=$(which python3.9)
            sudo $IIP_PYTHON get-pip.py
            sudo $IIP_PYTHON -m pip install pyflakes
        else
            case $Pythonyn in
                [Yy]* ) sudo apt update -y;
                        sudo apt install software-properties-common -y;
                        sudo echo | add-apt-repository ppa:deadsnakes/ppa;
                        sudo apt update -y;
                        sudo apt install python3.9 -y;
                        sudo wget https://bootstrap.pypa.io/get-pip.py;
                        sudo export IIP_PYTHON=$(which python3.9)
                        sudo $IIP_PYTHON get-pip.py;
                        sudo $IIP_PYTHON -m pip install pyflakes;;
                [Nn]* ) break;;
            esac
        fi
        echo "export IIP_PYTHON=$(which python3.9)" >> ~/.bashrc
    elif [[ "$PythonInstallModeab" == "B" || "$PythonInstallModeab" == "b" ]]; then
        CurPath=$PWD
        if ! [ -x "$(command -v $PythonPath --version)" ]; then
            sudo apt update -y
            sudo apt install -y build-essential libssl-dev zlib1g-dev \
                libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
                libncurses5-dev libncursesw5-dev xz-utils tk-dev libffi-dev \
                libgdbm-dev liblzma-dev uuid-dev
            sudo mkdir -p $CurPath/PyPaths/sources/python3.9
            cd $CurPath/PyPaths/sources/python3.9
            sudo wget https://www.python.org/ftp/python/3.9.21/Python-3.9.21.tgz
            sudo tar xzf Python-3.9.21.tgz
            cd Python-3.9.21
            sudo ./configure --prefix=$CurPath/PyPaths/python3.9 --enable-optimizations --with-ensurepip=install
            sudo make -j$(nproc)
            sudo make altinstall
            export IIP_PYTHON=$CurPath/PyPaths/python3.9/bin/python3.9
            sudo $IIP_PYTHON -m pip install pyflakes
        else
            case $Pythonyn in
                [Yy]* ) sudo apt update -y;
                        sudo apt install -y build-essential libssl-dev zlib1g-dev \
                            libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
                            libncurses5-dev libncursesw5-dev xz-utils tk-dev libffi-dev \
                            libgdbm-dev liblzma-dev uuid-dev;
                        sudo mkdir -p $CurPath/PyPaths/sources/python3.9;
                        cd $CurPath/PyPaths/sources/python3.9;
                        sudo wget https://www.python.org/ftp/python/3.9.21/Python-3.9.21.tgz;
                        sudo tar xzf Python-3.9.21.tgz;
                        cd Python-3.9.21;
                        sudo ./configure --prefix=$CurPath/PyPaths/python3.9 --enable-optimizations --with-ensurepip=install;
                        sudo make -j$(nproc);
                        sudo make altinstall;
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

    mkdir Install && cd Install
    
    wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/platform/tools/Install/install.tar.gz
    tar xzpvf install.tar.gz
    
    cd platformDependencies/
    python3 -m pip install -r requirements.txt
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
    
    sudo chown -R $USER ../Install
    
elif [ $yn == "n" ] || [ $yn == "N" ]; then 

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

    if [[ "$Nodeyn" == "Y" || "$Nodeyn" == "y" ]]; then
        echo "Installing Node.js 22.x (latest) version"
    elif [[ "$Nodeyn" == "E" || "$Nodeyn" == "e" ]]; then
        echo "Node.js is already installed with accepted version, Node.js $NODE_VERSION"
    fi 
    if [[ "$Angularyn" == "Y" || "$Angularyn" == "y" ]]; then
        echo "Installing Angular $OktAngularVersion"
    elif [[ "$Angularyn" == "E" || "$Angularyn" == "e" ]]; then
        echo "Angular is already installed with accepted version, Angular $ANG_VERSION"
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

    mkdir Install && cd Install
    
    wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/platform/tools/Install/install.tar.gz
    tar xzpvf install.tar.gz

    cd platformDependencies/
    python3 -m pip install -r requirements.txt
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
    
    sudo chown -R $USER ../Install

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
