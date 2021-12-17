FROM openjdk:8
ENV IIP_PORT=9000
EXPOSE 9000/TCP
COPY common/ common
COPY ecsJars/ ecsJars
COPY ecs8.sh ecs8.sh

# Docker group 117
USER root
RUN addgroup --system -gid 117 docker
RUN useradd iip && usermod -aG docker iip

# Installing Docker
ARG DEBIAN_FRONTEND=noninteractive
RUN curl -sSL https://get.docker.com/ | sh

USER iip
RUN newgrp docker

# Run ECS
CMD ./ecs8.sh

