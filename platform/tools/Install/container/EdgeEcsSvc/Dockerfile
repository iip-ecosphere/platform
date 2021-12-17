FROM openjdk:16-alpine3.13
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY brokerJars/ brokerJars
COPY amqp.sh amqp.sh
COPY src/ src
COPY common/ common
COPY ecsSvcJars/ ecsSvcJars
COPY ecsServiceMgr8.sh ecs8.sh
COPY edgeServiceMgr.image-info.yml image-info.yml
# for in-container testing
COPY SimpleMeshTestingApp/target/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar
# Wrapper script
COPY edgeEcsSvc.wrapper_script.sh wrapper_script.sh
# Installing bash
RUN apk update && apk add bash
# Running the wrapper script
CMD ./wrapper_script.sh

