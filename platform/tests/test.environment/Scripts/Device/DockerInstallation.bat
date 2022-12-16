cd %~dp0
mkdir Setup
cd Setup

curl https://desktop.docker.com/win/stable/amd64/65384/Docker%%20Desktop%%20Installer.exe -O DockerDesktopInstaller.exe
rename "Docker%%20Desktop%%20Installer.exe" DockerDesktopInstaller.exe
start /w "" "DockerDesktopInstaller.exe" install --quiet

sudo usermod -aG docker $USER
