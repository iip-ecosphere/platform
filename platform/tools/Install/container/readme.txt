fullPlatform:
Containers described here are used by github actions to automatically create the containers on dockerhub. 
Please handle with care.

EcsRuntime, SimpleMeshTestingApp:
Generic testing containers.

EdgeEcsRuntime, EdgeServiceMgr:
Experimental edge containers by Monika. Preliminary start (to be adjusted):
docker run -v /var/run/docker.sock:/var/run/docker. -P --network=host --mount type=bind,source="$(pwd)"/serviceMgr_buildcontext,target=/serviceMgr_buildcontext -it <ImageName>
