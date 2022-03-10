FROM openjdk:16-alpine3.13
WORKDIR /device
COPY brokerJars/ brokerJars
COPY src/ src
COPY amqp.sh amqp.sh
# App
COPY SimpleMeshTestingApp/target/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar
# Service Manger
COPY svcJars/ svcJars
COPY serviceMgr.sh serviceMgr.sh
# Common
COPY common/ common
# Wrapper script
COPY edgeServiceMgr.wrapper_script.sh wrapper_script.sh
# Installing bash
RUN apk update && apk add bash
# Running the wrapper script
CMD ./wrapper_script.sh

