FROM openjdk:8
ENV IIP_PORT=9001
COPY svcJars/* /svcJars/
COPY serviceMgr.sh /
COPY SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar /apps/
RUN ["chmod", "+x", "/serviceMgr.sh"]
CMD /serviceMgr.sh --iip.port=$IIP_PORT
