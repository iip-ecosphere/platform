FROM dorowu/ubuntu-desktop-lxde-vnc:latest

RUN mkdir -p /root/.config

# ------ platform prerequisite -----------------------------------------------------------

WORKDIR /root/setup

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
RUN python3 -m pip install websockets==11.0.2 pyyaml==6.0 pyflakes==3.2.0

# ------ JAVA_HOME --------------------------------------------------------------------
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

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

# ------ platform operation -----------------------------------------------------------

ARG PLATFORM_PATH="/opt/user/platform"

WORKDIR ${PLATFORM_PATH}/gen

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
COPY SimpleMeshInstallTestingApp/target/SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar ${PLATFORM_PATH}/SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar

# meta-model and model
COPY target/easy ${PLATFORM_PATH}/target/easy
COPY target/template/ ${PLATFORM_PATH}/src/main/easy/

# getting install
RUN mkdir -p ${PLATFORM_PATH}/Install \
 && wget -O ${PLATFORM_PATH}/install.tar.gz \
    https://projects.sse.uni-hildesheim.de/oktoflow/install.tar.gz \
 && tar -xzf ${PLATFORM_PATH}/install.tar.gz -C ${PLATFORM_PATH}/Install  

# ------ Installing platform dependencies ---------------------------------------------
RUN mkdir -p ${PLATFORM_PATH}/artifacts && chmod -f 777 ${PLATFORM_PATH}/artifacts
RUN mvn -f ${PLATFORM_PATH}/Install/pom.xml -P DepsOnly install

# ------ Desktop setup ----------------------------------------------------------------
# Icons
COPY icon.xpm /opt/eclipse
COPY eclipse.desktop /root/Desktop/eclipse.desktop
COPY firefox.desktop /root/Desktop/firefox.desktop
COPY vscode.desktop /root/Desktop/vscode.desktop

# ------ Create user group ----------------------------------------------------------------
RUN groupadd --gid 1001 platform
RUN chown -R root:platform /opt/user/platform && \
    chmod -R g+rwX /opt/user/platform && \
    find /opt/user/platform -type d -exec chmod g+s {} \;

# Starter scripts
COPY brokerPl_starter.sh brokerPl_starter.sh
COPY ecs_starter.sh ecs_starter.sh
COPY serviceMgr_starter.sh serviceMgr_starter.sh
COPY monitoring_starter.sh monitoring_starter.sh
COPY ui_starter.sh ui_starter.sh
COPY setup_desktop.sh setup_desktop.sh
RUN chmod 777 *.sh 

# running platform
COPY platform.conf /etc/supervisor/conf.d/platform.conf
RUN cat /etc/supervisor/conf.d/platform.conf >> /etc/supervisor/conf.d/supervisord.conf

