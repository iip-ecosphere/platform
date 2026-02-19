FROM dorowu/ubuntu-desktop-lxde-vnc:latest
WORKDIR /root/platform/gen

# ------ platform components -----------------------------------------------------------
# Broker
COPY broker/brokerJars/ brokerJars
COPY broker/src/ src
COPY broker/broker.sh broker.sh
# Platform
COPY plJars/ plJars
COPY platform.sh platform.sh
# ECS
COPY ecsJars/ ecsJars
COPY ecs.sh ecs.sh
# Service Manger
COPY svcJars/ svcJars
COPY serviceMgr.sh serviceMgr.sh
# Monitoring
COPY monJars/ monJars
COPY monitoring.sh monitoring.sh
# UI
COPY mgtUi/ mgtUi
COPY mgtUi.sh mgtUi.sh
# CLI
COPY cli.sh cli.sh
COPY deployment_plan.yaml artifacts/deployment_plan.yaml
COPY SimpleMeshInstallTestingApp/target/SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar /root/platform/SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar
# Starter scripts
COPY brokerPl_starter.sh brokerPl_starter.sh
COPY ecs_starter.sh ecs_starter.sh
COPY serviceMgr_starter.sh serviceMgr_starter.sh
COPY monitoring_starter.sh monitoring_starter.sh
COPY ui_starter.sh ui_starter.sh

# meta-model and model
COPY target/easy /root/platform/target/easy
COPY target/template/ /root/platform/src/main/easy/

# getting install
RUN mkdir -p /root/platform/Install \
 && wget -O /root/platform/install.tar.gz \
    https://projects.sse.uni-hildesheim.de/oktoflow/install.tar.gz \
 && tar -xzf /root/platform/install.tar.gz -C /root/platform/Install

RUN chmod 777 *   

# ------ Eclipse ----------------------------------------------------------------------
COPY eclipse/ /opt/eclipse
COPY impl.model /root/Desktop/eclipse-workspace/impl.model 

# ------ installing dependencies ------------------------------------------------------
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
RUN sudo apt-get update
RUN sudo apt-get install openjdk-21-jre -y
RUN sudo apt-get install openjdk-8-jre -y
RUN sudo apt install git -y 
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash - && sudo apt-get install -y nodejs
RUN sudo apt install gedit -y
# maven
RUN wget https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.tar.gz
RUN tar xzpvf apache-maven-3.9.7-bin.tar.gz
RUN ln -s $PWD/apache-maven-3.9.7/bin/mvn /usr/bin/mvn
# py dependencies
RUN apt-get update && apt-get install -y python3-pip
RUN python3 -m pip install websockets==11.0.2

# ------ Installing platform dependencies ---------------------------------------------
RUN mvn -f /root/platform/Install/pom.xml -P DepsOnly install


# ------ intalling newer version of firefox -------------------------------------------
RUN apt-get update && apt-get install -y \
    wget \
    bzip2 \
    libgtk-3-0 \
    libdbus-glib-1-2 \
    libasound2 \
    libx11-xcb1 \
    libxcb-dri3-0 \
    libdrm2 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libgbm1 \
    libnss3 \
    libpango1.0-0 \
    libxss1 \
    libxtst6 \
    && rm -rf /var/lib/apt/lists/*
    

# Download latest stable Firefox
RUN apt-get update && apt-get install -y file wget tar xz-utils bzip2 \
    libgtk-3-0 libdbus-glib-1-2 libasound2 libx11-xcb1 libxcb-dri3-0 libdrm2 \
    libxcomposite1 libxdamage1 libxrandr2 libgbm1 libnss3 libpango1.0-0 libxss1 libxtst6 libxtst6 \
    && wget -O /tmp/firefox.tar.xz "https://download.mozilla.org/?product=firefox-latest&os=linux64&lang=en-US" -q \
    && tar -xJf /tmp/firefox.tar.xz -C /opt/ \
    && ln -s /opt/firefox/firefox /usr/local/bin/firefox \
    && rm /tmp/firefox.tar.xz
    
# ------ Installing VSCode -----------------------------------------------------------
# Install dependencies for VSCode
RUN apt-get update && apt-get install -y \
    wget \
    gpg \
    apt-transport-https \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# Import Microsoft GPG key and add repo
RUN wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > /usr/share/keyrings/packages.microsoft.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/packages.microsoft.gpg] https://packages.microsoft.com/repos/code stable main" \
    > /etc/apt/sources.list.d/vscode.list

RUN apt-get update && apt-get install -y code \
    && rm -rf /var/lib/apt/lists/*

# ------ Desktop setup ----------------------------------------------------------------
# Icons
COPY icon.xpm /opt/eclipse
COPY eclipse.desktop /root/Desktop/eclipse.desktop
COPY firefox.desktop /root/Desktop/firefox.desktop
COPY vscode.desktop /root/Desktop/vscode.desktop

# running platform
COPY platform.conf /etc/supervisor/conf.d/platform.conf
RUN cat /etc/supervisor/conf.d/platform.conf >> /etc/supervisor/conf.d/supervisord.conf

