cd %~dp0

mkdir Install
cd Install

curl https://jenkins-2.sse.uni-hildesheim.de/view/IIP-Ecosphere/job/IIP_Install/lastSuccessfulBuild/artifact/install.tar.gz -O install.tar.gz
tar xzpvf install.tar.gz

cd platformDependencies
call mvn install -U
cd..

call mvn package -DskipTests -U
