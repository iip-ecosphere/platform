fullPlatform:
  Containers described here are used by github actions to automatically create the containers on dockerhub. 
  Please handle with care.

EcsRuntime, SimpleMeshTestingApp:
  Generic testing containers.

Edge*: Experimental edge containers by Monika. 
  - EdgeEcsRuntime: Standalone edge ECS runtime with docker.
    /usr/bin/docker run -P --network=host -it iip/edgeecruntime:0.3
  - EdgeServiceMgr: Standalone edge service manager to be started by EdgeEcsRuntime
    /usr/bin/docker run -P --network=host -it iip/edgeservicemgr:0.3
    add file:/device/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar
  - EdgeEcsSvn: Combined ECS runtime with service manager.
    /usr/bin/docker run -P --network=host -it iip/edgeecssvc:0.3
    add file:/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar
  
  Further start:
  /usr/bin/docker run -v /var/run/docker.sock:/var/run/docker.sock -P --network=host --mount type=bind,source="$(pwd)"/container,target=/container -it iip/edgeecruntime:0.3
