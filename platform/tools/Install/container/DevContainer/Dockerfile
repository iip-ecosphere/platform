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
RUN sudo apt install nodejs npm -y --fix-missing
RUN sudo apt install gedit -y

# ------ Desktop setup ----------------------------------------------------------------
# Icons
COPY icon.xpm /opt/eclipse
COPY eclipse.desktop /root/Desktop/eclipse.desktop
COPY chrome.desktop /root/Desktop/chrome.desktop
# Chrome plugin
COPY mjhpgnbimicffchbodmgfnemoghjakai.json /opt/google/chrome/extensions/mjhpgnbimicffchbodmgfnemoghjakai.json

# running platform
COPY platform.conf /etc/supervisor/conf.d/platform.conf
RUN cat /etc/supervisor/conf.d/platform.conf >> /etc/supervisor/conf.d/supervisord.conf

