mkdir -p Platform && cd Platform

read -p "Do you want to install the prerequisites (Java 13, Maven version 3.6.3, Docker version 20.10.7, and Python version 3.9)? (y/n) " yn
if [ $yn == "y" ] || [ $yn == "Y" ]; then 
    
    # Check current Java version 
    
    if [ -x "$(command -v java -version)" ]; then
        JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' || true)
        JavaLimit=11
        if [ $JAVA_VERSION -lt $JavaLimit ]; then
            echo "The Java version you have is less than the minimum requirement Java $JavaLimit for the IIP-Ecosphere platform"
            echo "Your Java version is "$JAVA_VERSION
            while true; do
                read -p "Do you want to install Java 13 - you should have at least Java $JAVA_VERSION? - You can't skip this step; if you skip it the installation will end. (y/n) " Javayn
                case $Javayn in
                    [Yy]* ) break;;
                    [Nn]* ) exit;;
                    * ) echo "Please answer y or n.";;
                esac
            done
        fi    
    fi
    
    # Check current Maven version
    
    if [ -x "$(command -v mvn -version)" ]; then
        MVN_VERSION=$(mvn -version | grep "Apache Maven" | grep -oP '[[:digit:]]\.[[:digit:]]\.[[:digit:]]')
        RecommendMvn=3.6.3
        if ! [ $MVN_VERSION == $RecommendMvn ]; then
            echo "You have Maven version $MVN_VERSION, it is recommended to have version $RecommendMvn"
            while true; do
                read -p "Do you want to install Maven $RecommendMvn and replace Maven default to $RecommendMvn? - You might skip this step (y/n) " Mavenyn
                case $Mavenyn in
                    [Yy]* ) break;;
                    [Nn]* ) break;;
                    * ) echo "Please answer y or n.";;
                esac
            done
        fi
    fi
    
    # Check current Docker version 


    if [ -x "$(command -v docker --version)" ]; then
        DOCKER_VERSION=$(docker --version | grep -oP '[[:digit:]]{1,2}\.[[:digit:]]{1,2}\.[[:digit:]]{1,2}' | head -1)
        RecommendDocker=20.10.7
        if ! [ $DOCKER_VERSION == $RecommendDocker ]; then
            echo "You have Docker version $DOCKER_VERSION, it is recommended to have version $RecommendDocker"
            while true; do
                read -p "Do you want to install Docker $RecommendDocker and replace Docker default to $RecommendDocker? - You might skip this step (y/n) " Dockeryn
                case $Dockeryn in
                    [Yy]* ) break;;
                    [Nn]* ) break;;
                    * ) echo "Please answer y or n.";;
                esac
            done
        fi
    fi
    
    # Check current Python version
    
    if [ -x "$(command -v python3 --version)" ]; then
        PYTHON_VERSION=$(python3 --version | grep -oP '[[:digit:]]{1,2}\.[[:digit:]]{1,2}')
        RecommendPython=3.9
        if ! [ $PYTHON_VERSION == $RecommendPython ]; then
            echo "You have Python version $PYTHON_VERSION, it is recommended to have version $RecommendPython"
            while true; do
                read -p "Do you want to install Python $RecommendPython and replace Python default to $RecommendPython? - You might skip this step (y/n) " Pythonyn
                case $Pythonyn in
                    [Yy]* ) break;;
                    [Nn]* ) break;;
                    * ) echo "Please answer y or n.";;
                esac
            done
        fi
    fi
    
    # Check Docker Private Registry
    
    while true; do
        read -p "Do you want to start Docker Private Registry (You should have Docker installed, be carefull it will restart Docker service) for the platform? - You might skip this step (y/n)" Registryyn
        case $Registryyn in
            [Yy]* ) break;;
            [Nn]* ) break;;
            * ) echo "Please answer y or n.";;
        esac
    done
   
    # Check Angular 13
    
    while true; do
        echo "To use the management UI for the platform you should have angular version 13 installed (with npm and Node.js)"
        read -p "Do you want to install Node.js version 14, angular version 13, and npm package manager for the JavaScript? - You might skip this step (y/n)" Angularyn
        case $Angularyn in
            [Yy]* ) break;;
            [Nn]* ) break;;
            * ) echo "Please answer y or n.";;
        esac
    done   
    
    # Install Java version 13 
    
    sudo apt-get update
    sudo apt install unzip -y
    sudo apt install jq -y
    
    if ! [ -x "$(command -v java -version)" ]; then
        sudo apt install openjdk-13-jdk-headless -y
    else
        case $Javayn in
            [Yy]* ) sudo apt install openjdk-13-jdk-headless -y; break;;
            [Nn]* ) exit;;
        esac
    fi
    
    # Install Maven version 3.6.3
    
    if ! [ -x "$(command -v mvn -version)" ]; then
        sudo wget https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
        sudo tar xzpvf apache-maven-3.6.3-bin.tar.gz
    
        sudo ln -s $PWD/apache-maven-3.6.3/bin/mvn /usr/bin/mvn
    else
        case $Mavenyn in
            [Yy]* ) sudo wget https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz;
                    sudo tar xzpvf apache-maven-3.6.3-bin.tar.gz;
                    sudo ln -sf $PWD/apache-maven-3.6.3/bin/mvn /usr/bin/mvn;;
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
    
    if ! [ -x "$(command -v python3 --version)" ]; then
        sudo apt update -y
        sudo apt install software-properties-common -y
        sudo echo | add-apt-repository ppa:deadsnakes/ppa
        sudo apt update -y
        sudo apt install python3.9 -y
        ln -sf python3.9 /usr/bin/python3
        sudo apt remove python3-apt -y
        sudo apt autoremove -y
        sudo apt autoclean -y
        sudo apt install python3-apt -y
        sudo apt-get install python3-pip -y
        sudo pip install pyflakes
    
    else
        case $Pythonyn in
            [Yy]* ) sudo apt update -y;
                    sudo sudo apt install software-properties-common -y;
                    sudo echo | add-apt-repository ppa:deadsnakes/ppa;
                    sudo apt update -y;
                    sudo apt install python3.9 -y;
                    ln -sf python3.9 /usr/bin/python3;
                    sudo apt remove python3-apt -y
                    sudo apt autoremove -y
                    sudo apt autoclean -y
                    sudo apt install python3-apt -y
                    sudo apt-get install python3-pip -y;
                    sudo pip install pyflakes;;
            [Nn]* ) break;;
        esac
    fi
    
    # Install nodejs version 14 and angular version 13
    
    case $Angularyn in
        [Yy]* ) sudo curl -sL https://deb.nodesource.com/setup_14.x | sudo bash -;
                sudo apt -y install nodejs;
                sudo apt install npm -y;
                sudo npm install -g @angular/cli@14.2.11;;
        [Nn]* ) break;;
    esac

    sudo apt install curl -y
    
    localIP=$(hostname -I | cut -d ' ' -f1)

    mkdir Install && cd Install
    
    wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz
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
    
    sudo mvn install -Diip.easy.tracing=ALL
    
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
    
    sudo apt-get update
    sudo apt install unzip -y
    sudo apt install jq -y
    
    # Install nodejs version 14 and angular version 13
    
    case $Angularyn in
        [Yy]* ) sudo curl -sL https://deb.nodesource.com/setup_14.x | sudo bash -;
                sudo apt -y install nodejs;
                sudo apt install npm -y;
                sudo npm install -g @angular/cli@14.2.11;;
        [Nn]* ) break;;
    esac
    
    sudo apt install curl -y
    
    localIP=$(hostname -I | cut -d ' ' -f1)

    mkdir Install && cd Install
    
    wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz
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