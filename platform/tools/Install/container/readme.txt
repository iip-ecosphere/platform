fullPlatform:
  Containers described here are used by github actions to automatically create the containers on dockerhub. 
  Please handle with care.

EcsRuntime, SimpleMeshTestingApp:
  Generic testing containers.

Edge*: Experimental edge containers by Monika. 
  - EdgeEcsRuntime: Standalone edge ECS runtime with docker.
  - EdgeServiceMgr: Standalone edge service manager to be started by EdgeEcsRuntime
  - EdgeEcsSvn: Combined ECS runtime with service manager.
  
  Preliminary start (to be adjusted):
  /usr/bin/docker run -v /var/run/docker.sock:/var/run/docker. -P --network=host --mount type=bind,source="$(pwd)"/serviceMgr_buildcontext,target=/serviceMgr_buildcontext -it <ImageName>
