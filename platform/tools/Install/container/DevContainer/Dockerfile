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
COPY SimpleMeshTestingApp/target/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar

# Script starts all platform components
COPY starter.sh /root/Desktop/starter.sh
RUN chmod 777 /root/Desktop/starter.sh
COPY starter.sh starter.sh
RUN chmod 777 starter.sh

# ------ Eclipse ----------------------------------------------------------------------
COPY eclipse/ /opt/eclipse
COPY PyDev10.0.1 /opt/eclipse/dropins/PyDev10.0.1
COPY EASyProducer /opt/eclipse/dropins/EASyProducer

# ------ installing dependencies ------------------------------------------------------
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
RUN sudo apt-get update
RUN sudo apt-get install openjdk-11-jre -y
RUN sudo apt-get install openjdk-8-jre -y
RUN sudo apt install maven -y
RUN sudo apt install git -y 
RUN sudo apt install nodejs npm -y --fix-missing
RUN sudo apt install gedit -y

# ------ Desktop setup ----------------------------------------------------------------
# Icons
COPY eclipse.desktop /root/Desktop/eclipse.desktop
COPY chrome.desktop /root/Desktop/chrome.desktop
# Chrome plugin
COPY mjhpgnbimicffchbodmgfnemoghjakai.json /opt/google/chrome/extensions/mjhpgnbimicffchbodmgfnemoghjakai.json

# running platform
COPY platform.conf /etc/supervisor/conf.d/platform.conf
RUN cat /etc/supervisor/conf.d/platform.conf >> /etc/supervisor/conf.d/supervisord.conf

