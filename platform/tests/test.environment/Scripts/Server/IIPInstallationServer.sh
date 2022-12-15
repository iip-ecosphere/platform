cd Files

if [ $7 == "True" ]; then
    echo $1 | sudo -S rm -r Install
    mkdir -p Install
    cd Install

    wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz
    tar xzpvf install.tar.gz
else
    cd Install
fi

cd platformDependencies
echo $1 | sudo -S mvn install -U
cd ..

echo $1 | sudo -S mvn package -U

if [ $7 == "True" ]; then
    localIP=$(hostname -I | cut -d ' ' -f1)
    artifactsFolder=$6

    sed -i 's/147.172.178.145/'$localIP'/g' src/main/easy/InstallTest.ivml
    artifactsLineNumber=$(cat src/main/easy/InstallTest.ivml | grep -n "String platformServer" | cut -d ' ' -f1 | sed 's/:/ /g')
    ((artifactsLineNumber=artifactsLineNumber+1))
    sed -i $artifactsLineNumber' i \ \ \ \ artifactsUriPrefix = "http://'$localIP':4200/download";' src/main/easy/InstallTest.ivml
    sed -i $artifactsLineNumber' i \ \ \ \ artifactsFolder = "'$artifactsFolder'";' src/main/easy/InstallTest.ivml

    if [ $2 != "Non" ]; then

      applicationLineNumber=$(cat src/main/easy/InstallTest.ivml | grep -n "Application" | cut -d ' ' -f1 | sed 's/:/ /g')
      ((applicationLineNumber=applicationLineNumber+1))

      sed -i $applicationLineNumber' i \ \ \ \ \ \ \ \ createContainer = true,' src/main/easy/InstallTest.ivml

      localPortLineNumber=$(cat src/main/easy/InstallTest.ivml | grep -n "transportProtocol = " | cut -d ' ' -f1 | sed 's/:/ /g')
      ((localPortLineNumber=localPortLineNumber+1))

      sed -i $localPortLineNumber' i \ \ \ \ \ \ \ \ localPort = 8888,' src/main/easy/InstallTest.ivml

      generationLineNumber=$(cat src/main/easy/InstallTest.ivml | grep -n "generation setup" | cut -d ' ' -f1 | sed 's/:/ /g')
      ((generationLineNumber=generationLineNumber-1))

      sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/InstallTest.ivml     
      sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ containerType = ContainerType::'$2 src/main/easy/InstallTest.ivml
      sed -i $generationLineNumber' i \ \ \ \ EcsDevice device = {' src/main/easy/InstallTest.ivml
      sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/InstallTest.ivml
      if [ $3 != "Non" ]; then
        sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/InstallTest.ivml     
        sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ registry = "'$3'"' src/main/easy/InstallTest.ivml
        sed -i $generationLineNumber' i \ \ \ \ containerManager = DockerContainerManager {' src/main/easy/InstallTest.ivml
        sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/InstallTest.ivml
      fi
      sed -i $generationLineNumber' i \ \ \ \ // ---------- Device ------------' src/main/easy/InstallTest.ivml
      sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/InstallTest.ivml

      echo "Containers Added"

    fi
fi

if [ $4 == "True" ]; then
    echo $1 | sudo -S mvn exec:java -Diip.easy.tracing=ALL
else
    echo $1 | sudo -S mvn exec:java
fi

echo $1 | sudo -S find . -name "*bin.jar" -exec cp {} $artifactsFolder \;
sudo chown -R $5 ../Install

mkdir -p DeviceFolder/
mkdir DeviceFolder/ecsJars/
mkdir DeviceFolder/broker/
mkdir DeviceFolder/svcJars/
mkdir DeviceFolder/ecsSvcJars/
cp -r "gen/ecsJars/"* "DeviceFolder/ecsJars/"
cp -r "gen/broker/"* "DeviceFolder/broker/"
cp -r "gen/svcJars/"* "DeviceFolder/svcJars/"
cp -r "gen/ecsSvcJars/"* "DeviceFolder/ecsSvcJars/"
cp "gen/ecs.sh" "DeviceFolder/ecs.sh"
cp "gen/ecs.bat" "DeviceFolder/ecs.bat"
cp "gen/serviceMgr.sh" "DeviceFolder/serviceMgr.sh"
cp "gen/serviceMgr.bat" "DeviceFolder/serviceMgr.bat"
cp "gen/ecsServiceMgr.sh" "DeviceFolder/ecsServiceMgr.sh"
cp "gen/ecsServiceMgr.bat" "DeviceFolder/ecsServiceMgr.bat"
cd DeviceFolder
tar -czvf DeviceFolder.tar.gz *
echo $1 | sudo -S cp "DeviceFolder.tar.gz"* $artifactsFolder
