cd Files

if [ $2 == "True" ]; then
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

sudo chown -R $3 ../Install