FROM openjdk:16-alpine3.13
WORKDIR /device
# Broker
COPY amqp.sh amqp.sh
COPY src/ src
# Platform
COPY plJars/ plJars
COPY platform/ platform
COPY platform.sh platform.sh
# App
COPY SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar
# ECS
COPY ecsRuntime/ ecsRuntime
COPY ecsJars/ ecsJars
COPY ecs.sh ecs.sh
# Service Manger
COPY serviceMgr/ serviceMgr
COPY svcJars/ svcJars
COPY serviceMgr.sh serviceMgr.sh
# Wrapper script
COPY wrapper_script.sh wrapper_script.sh
# Installing bash
RUN apk update && apk add bash
# Running the wrapper script
CMD ./wrapper_script.sh

