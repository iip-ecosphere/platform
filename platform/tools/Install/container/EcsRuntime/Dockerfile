FROM openjdk:8
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY ecsJars/* /ecs/ecsRuntime/
COPY ecs.sh /ecs/
CMD ecs.sh 
