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

artifactsFolder=$6
localIP=$(hostname -I | cut -d ' ' -f1)

if [ $7 == "True" ]; then

    sed -i 's/147.172.178.145/'$localIP'/g' src/main/easy/TechnicalSetup.ivml
    artifactsLineNumber=$(cat src/main/easy/TechnicalSetup.ivml | grep -n "String platformServer" | cut -d ' ' -f1 | sed 's/:/ /g')
    ((artifactsLineNumber=artifactsLineNumber+1))
    sed -i $artifactsLineNumber' i \ \ \ \ artifactsUriPrefix = "http://'$localIP':4200/download";' src/main/easy/TechnicalSetup.ivml
    sed -i $artifactsLineNumber' i \ \ \ \ artifactsFolder = "'$artifactsFolder'";' src/main/easy/TechnicalSetup.ivml

    if [ $2 != "Non" ]; then

      sed -i '/Application /a \ \ \ \ \ \ \ \ createContainer = true,' src/main/easy/ApplicationPartMyApp.ivml

      sed -i '/transportProtocol = /a \ \ \ \ \ \ \ \ localPort = 8888,' src/main/easy/TechnicalSetup.ivml

      generationLineNumber=$(cat src/main/easy/TechnicalSetup.ivml | grep -n "generation setup" | cut -d ' ' -f1 | sed 's/:/ /g')
      ((generationLineNumber=generationLineNumber-1))

      sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/TechnicalSetup.ivml     
      sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ containerType = ContainerType::'$2 src/main/easy/TechnicalSetup.ivml
      sed -i $generationLineNumber' i \ \ \ \ EcsDevice device = {' src/main/easy/TechnicalSetup.ivml
      sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml
      if [ $3 != "Non" ]; then
        sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/TechnicalSetup.ivml     
        sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ registry = "'$3'"' src/main/easy/TechnicalSetup.ivml
        sed -i $generationLineNumber' i \ \ \ \ containerManager = DockerContainerManager {' src/main/easy/TechnicalSetup.ivml
        sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml
      fi
      sed -i $generationLineNumber' i \ \ \ \ // ---------- Device ------------' src/main/easy/TechnicalSetup.ivml
      sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/TechnicalSetup.ivml

      echo "Containers Added"

    fi
fi

if [ $4 == "True" ]; then
    echo $1 | sudo -S mvn install -U -Diip.easy.tracing=ALL
else
    echo $1 | sudo -S mvn install -U 
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