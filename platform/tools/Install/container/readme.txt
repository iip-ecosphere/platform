Device: 
Copy the created ecs.sh, ecsJars, svcJars, serviceMgr.sh, ampq.sh amd brokerJars.sh into this folder.
Then containers can be created/started via the contained scripts.

Edge* are experimental container. Preliminary start (to be adjusted):
docker run -v /var/run/docker.sock:/var/run/docker. -P --network=host --mount type=bind,source="$(pwd)"/serviceMgr_buildcontext,target=/serviceMgr_buildcontext -it <ImageName>
