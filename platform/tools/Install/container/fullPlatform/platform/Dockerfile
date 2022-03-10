FROM openjdk:16-alpine3.13
WORKDIR /device
# Broker (src contains broker configuration)
COPY brokerJars/ brokerJars
COPY src/ src
COPY amqp.sh amqp.sh
# Platform
COPY plJars/ plJars
COPY platform.sh platform.sh
# App
COPY SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar
# ECS
COPY ecsJars/ ecsJars
COPY ecs.sh ecs.sh
# Service Manger
COPY svcJars/ svcJars
COPY serviceMgr.sh serviceMgr.sh
# Wrapper script
COPY wrapper_script.sh wrapper_script.sh
# Installing bash
RUN apk update && apk add bash
# Running the wrapper script
CMD ./wrapper_script.sh

