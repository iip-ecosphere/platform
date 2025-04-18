echo $1 | sudo -S apt-get update
sudo apt install unzip -y
sudo apt install jq -y
sudo apt install openjdk-17-jdk-headless -y
#sudo apt install maven -y
echo $1 | sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.tar.gz
echo $1 | sudo tar xzpvf apache-maven-3.9.7-bin.tar.gz

echo $1 | sudo ln -s $PWD/apache-maven-3.9.7/bin/mvn /usr/bin/mvn

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

sudo apt update -y

sudo apt install software-properties-common -y

sudo echo | add-apt-repository ppa:deadsnakes/ppa 

sudo apt update -y
sudo apt install python3.9 -y

sudo apt-get install python3-pip -y

sudo pip install pyflakes

sudo apt install npm -y

sudo apt install curl -y
