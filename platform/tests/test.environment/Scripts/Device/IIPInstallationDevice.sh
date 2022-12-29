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

echo $1 | sudo -S mvn install

sudo chown -R $3 ../Install