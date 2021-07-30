FROM openjdk:8
ENV IIP_PORT=9001
COPY svcJars/* /serviceMgr/svcJars/
COPY serviceMgr.sh /serviceMgr/
COPY SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar /apps/
CMD serviceMgr.sh $IIP_PORT
