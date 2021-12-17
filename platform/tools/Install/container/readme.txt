fullPlatform:
  Containers described here are used by github actions to automatically create the containers on dockerhub. 
  Please handle with care.

EcsRuntime, SimpleMeshTestingApp:
  Required: Local broker and platform running as configured.
  Generic testing containers.

Edge*: Experimental edge containers by Monika.
  Required: broker and platform running as configured  
  - EdgeEcsRuntime: Standalone edge ECS runtime with docker. 
    - For just representing the device without starting container.
      /usr/bin/docker run -P --network=host -it iip/edgeecsruntime:0.3
    - For starting container under control of ECS runtime:
      /usr/bin/docker run -v /var/run/docker.sock:/var/run/docker.sock -P --network=host --mount type=bind,source="$(pwd)"/EdgeServiceMgr,target=/container -it iip/edgeecsruntime:0.3
      add file:/container/image-info.yml
      Then continue below with EdgeServiceMgr... 
  - EdgeServiceMgr: Standalone edge service manager to be started by EdgeEcsRuntime
    /usr/bin/docker run -P --network=host -it iip/edgeservicemgr:0.3
    add file:/device/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar
  - EdgeEcsSvn: Combined ECS runtime with service manager.
    /usr/bin/docker run -P --network=host -it iip/edgeecssvc:0.3
    add file:/SimpleMeshTestingApp-0.1.0-SNAPSHOT.jar