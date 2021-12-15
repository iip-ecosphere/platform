FROM openjdk:8
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY common/ common
COPY ecsSvcJars/ ecsSvcJars
COPY ecsServiceMgr8.sh ecs8.sh
COPY edgeServiceMgr.image-info.yml image-info.yml

# Run ECS
CMD ./ecs8.sh

