cd Files
echo $1 | sudo -S rm -r Install
mkdir -p Install
cd Install

wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz
tar xzpvf install.tar.gz

cd platformDependencies
mvn install -U
cd ..

mvn package -DskipTests -U

localIP=$(hostname -I | cut -d ' ' -f1)

sed -i 's/147.172.178.145/'$localIP'/g' src/main/easy/InstallTest.ivml

if [ $2 = "True" ]; then

  ApplicationLineNumber=$(cat src/main/easy/InstallTest.ivml | grep -n "Application" | cut -d ' ' -f1 | sed 's/:/ /g')
  ((ApplicationLineNumber=ApplicationLineNumber+1))

  sed -i $ApplicationLineNumber' i \ \ \ \ \ \ \ \ createContainer = true,' src/main/easy/InstallTest.ivml

  generationLineNumber=$(cat src/main/easy/InstallTest.ivml | grep -n "generation setup" | cut -d ' ' -f1 | sed 's/:/ /g')
  ((generationLineNumber=generationLineNumber-1))

  sed -i $generationLineNumber' i \ \ \ \ };' src/main/easy/InstallTest.ivml     
  sed -i $generationLineNumber' i \ \ \ \ \ \ \ \ containerType = ContainerType::C1Ecs_C2Svc_App' src/main/easy/InstallTest.ivml
  sed -i $generationLineNumber' i \ \ \ \ EcsDevice device = {' src/main/easy/InstallTest.ivml
  sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/InstallTest.ivml
  sed -i $generationLineNumber' i \ \ \ \ // ---------- Device ------------' src/main/easy/InstallTest.ivml
  sed -i $generationLineNumber' i \ \ \ \ ' src/main/easy/InstallTest.ivml

  echo "Containers Added"

fi

mvn exec:java

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
cp "gen/SimpleMeshTestingApp/target/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar" "DeviceFolder/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar"