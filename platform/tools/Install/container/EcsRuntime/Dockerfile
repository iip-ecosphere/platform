FROM openjdk:8

# install IIP Ecosphere ECS runtime with default port 9000
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY ecsJars/* /ecsJars/
COPY ecs.sh /
RUN ["chmod", "+x", "/ecs.sh"]

# install docker
ARG DEBIAN_FRONTEND=noninteractive
RUN curl -sSL https://get.docker.com/ | sh
USER root
RUN useradd iip && usermod -aG docker iip
USER iip

# run ECS runtime

CMD /ecs.sh 
