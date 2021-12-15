FROM openjdk:16-alpine3.13
WORKDIR /device
# App
COPY SimpleMeshTestingApp/target/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar
# Service Manger
COPY svcJars/ svcJars
COPY serviceMgr.sh serviceMgr.sh
# Common
COPY common/ common
# Installing bash
RUN apk update && apk add bash
# Running the wrapper script
CMD ./serviceMgr.sh

