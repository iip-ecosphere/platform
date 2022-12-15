echo $1 | sudo -S apt-get update
sudo apt install unzip -y
sudo apt install openjdk-13-jdk-headless -y
sudo apt install maven -y

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

sudo apt install curl -y