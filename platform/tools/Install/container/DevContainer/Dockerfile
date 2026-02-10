FROM dorowu/ubuntu-desktop-lxde-vnc:latest
WORKDIR /root/platform

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
COPY SimpleMeshInstallTestingApp/target/SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar
# Starter scripts
COPY brokerPl_starter.sh brokerPl_starter.sh
COPY ecs_starter.sh ecs_starter.sh
COPY serviceMgr_starter.sh serviceMgr_starter.sh
COPY monitoring_starter.sh monitoring_starter.sh
COPY ui_starter.sh ui_starter.sh

# meta-model and model
#COPY target/easy target/easy
COPY target/easy /root/Desktop/test
COPY target/template/TechnicalSetup.ivml TechnicalSetup.ivml

RUN chmod 777 *   

# ------ Eclipse ----------------------------------------------------------------------
COPY eclipse/ /opt/eclipse
COPY impl.model /root/Desktop/eclipse-workspace/impl.model 

# ------ installing dependencies ------------------------------------------------------
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
RUN sudo apt-get update
RUN sudo apt-get install openjdk-21-jre -y
RUN sudo apt-get install openjdk-8-jre -y
RUN sudo apt install maven -y
RUN sudo apt install git -y 
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash - && sudo apt-get install -y nodejs
RUN sudo apt install gedit -y

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

# ------ Desktop setup ----------------------------------------------------------------
# Icons
COPY icon.xpm /opt/eclipse
COPY eclipse.desktop /root/Desktop/eclipse.desktop
COPY firefox.desktop /root/Desktop/firefox.desktop

# running platform
COPY platform.conf /etc/supervisor/conf.d/platform.conf
RUN cat /etc/supervisor/conf.d/platform.conf >> /etc/supervisor/conf.d/supervisord.conf

