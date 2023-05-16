FROM openjdk:16-alpine3.13
WORKDIR /device

# Broker (src contains broker configuration)
COPY broker/brokerJars/ brokerJars
COPY broker/src/ src
COPY broker/broker.sh broker.sh
# Platform
COPY plJars/ plJars
COPY platform.sh platform.sh
# App
COPY SimpleMeshInstallApp/target/SimpleMeshInstallApp-0.1.0-SNAPSHOT-bin.jar SimpleMeshInstallTestingApp-0.1.0-SNAPSHOT-bin.jar
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
# Wrapper script
COPY wrapper_script.sh wrapper_script.sh

COPY deployment_plan.yaml artifacts/deployment_plan.yaml

# Installing bash
RUN apk update && apk add bash
RUN apk add nodejs npm
# Creating a symlink from nodejs to node (for mgtUI.sh)
RUN ln -s /usr/bin/node /usr/bin/nodejs

# Running the wrapper script
CMD ./wrapper_script.sh

