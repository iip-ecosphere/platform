FROM openjdk:8

# install IIP Ecosphere ECS runtime with default port 9000
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY ecsJars/* /ecsJars/
COPY ecs.sh /
COPY EcsRuntime/image-info.yml /
RUN ["chmod", "+x", "/ecs.sh"]

# install docker
ARG DEBIAN_FRONTEND=noninteractive
RUN curl -sSL https://get.docker.com/ | sh
USER root
# this container sets the socket to group 116 that does not exist...
RUN groupadd -g 116 dockerd
RUN useradd iip && usermod -aG docker iip && usermod -aG dockerd iip
USER iip

# run ECS runtime

CMD /ecs.sh 
