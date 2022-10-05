FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get -y update \
    && apt-get -y install \
    jq \
    git \
    vim \
    nano \
    curl \
    wget \
    cmake \
    unzip \
    tmux \
    net-tools \
    iputils-ping \
    ffmpeg curl \
    python3-pip \
    libzbar0 \
    locales lsb-release \ 
    software-properties-common \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

#============ PYTHON ===============
ADD requirements.txt /home/requirements.txt
RUN pip install --no-cache-dir -r /home/requirements.txt

#=============JAVA script============
RUN apt-get -y update && apt-get -y install openjdk-8-jdk

#=============platform=======

ENV QPID_WORK=/tmp/qpidwork
ENV iip.port=9020
EXPOSE 9020

#copy broker from dict one level above the plattform project
#COPY resprep/broker/brokerJars brokerJars
#COPY resprep/broker/src src
#COPY resprep/broker/broker.sh broker.sh
#RUN chmod +x broker.sh

#COPY resprep/ecsJars /ecsJars #Getting the ecs runtime and making it startable!
#COPY resprep/ecs.sh /ecs.sh
#The service jars and starter for java 8 systems
#COPY resprep/ecsSvcJars /ecsSvcJars
#COPY resprep/ecsServiceMgr8.sh /ecsServiceMgr8.sh
#RUN chmod +x ecsServiceMgr8.sh
#COPY resprep/ecsServiceMgr8.service /ecsServiceMgr8.service 

COPY resprep/wrapper_script.sh wrapper_script.sh
RUN chmod +x wrapper_script.sh
#CMD ./wrapper_script.sh
