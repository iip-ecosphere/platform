echo $1 | sudo -S apt-get update

cd Files
mkdir Install
cd Install

wget https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz
tar xzpvf install.tar.gz

cd platformDependencies
mvn install -U
cd ..

mvn package -DskipTests -U
