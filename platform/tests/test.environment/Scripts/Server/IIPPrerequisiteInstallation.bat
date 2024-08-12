cd %~dp0
mkdir Setup
cd Setup
curl https://download.oracle.com/java/17/archive/jdk-17.0.10_windows-x64_bin.zip -o openjdk.zip
tar xzpvf openjdk.zip
setx /M JAVA_HOME "%cd%\jdk-17"
SET JAVA_HOME=%cd%\jdk-17
setx /M Path "%Path%;%JAVA_HOME%\bin"
SET Path=%Path%;%JAVA_HOME%\bin
netsh advfirewall firewall add rule name="Java" dir=in action=allow program="%JAVA_HOME%\bin\java.exe" enable=yes

curl https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.zip -o maven.zip
tar xzpvf maven.zip
setx /M MAVEN_HOME "%cd%\apache-maven-3.9.7"
SET MAVEN_HOME=%cd%\apache-maven-3.9.7
setx /M Path "%Path%;%MAVEN_HOME%\bin"
SET Path=%Path%;%MAVEN_HOME%\bin

curl https://desktop.docker.com/win/stable/amd64/65384/Docker%%20Desktop%%20Installer.exe -O DockerDesktopInstaller.exe
rename "Docker%%20Desktop%%20Installer.exe" DockerDesktopInstaller.exe
start /w "" "DockerDesktopInstaller.exe" install --quiet

sudo usermod -aG docker $USER

curl https://www.python.org/ftp/python/3.9.6/python-3.9.6-amd64.exe -O python-3.9.6-amd64.exe
python-3.9.6-amd64.exe /quiet InstallAllUsers=1 PrependPath=1 Include_test=0
pip install pyflakes
