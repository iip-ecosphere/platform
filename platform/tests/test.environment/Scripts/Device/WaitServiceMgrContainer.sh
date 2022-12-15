containerName=$(curl $1 | grep "name:" | rev | cut -d ' ' -f1 | rev)

containerLog=$(docker logs $containerName)
ServiceMgrReady=$(echo "$containerLog" | grep "Startup completed")
while [ -z "$ServiceMgrReady" ]; do
  echo "Waiting serviceMgr to be Ready";
  containerLog=$(docker logs $containerName);
  ServiceMgrReady=$(echo "$containerLog" | grep "Startup completed");
  sleep 3;
done

echo "ServiceMgr is started"
echo "ServiceMgr is Running... Please don't close it"