FROM openjdk:8
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY common/ common
COPY ecsJars/ ecsJars
COPY ecs8.sh ecs8.sh
COPY edgeServiceMgr.image-info.yml image-info.yml

# Installing Docker
ARG DEBIAN_FRONTEND=noninteractive
RUN curl -sSL https://get.docker.com/ | sh
USER root
# Docker group 116
RUN groupadd -g 116 dockerd
RUN useradd iip && usermod -aG docker iip && usermod -aG dockerd iip
USER iip

# Run ECS
CMD ./ecs8.sh

