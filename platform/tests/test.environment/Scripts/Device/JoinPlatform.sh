cd Files

setsid ./ecs.sh --iip.id=$2 &> ecs.log &

ecsReady=$(cat ecs.log | grep "Startup completed")
while [ -z "$ecsReady" ]; do
  echo "Waiting ecs to be Ready";
  ecsReady=$(cat ecs.log | grep "Startup completed");
  sleep 3;
done

echo "Ecs is started"

setsid ./serviceMgr.sh --iip.id=$2 &> serviceMgr.log &

serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed")
while [ -z "$serviceMgrReady" ]; do
  echo "Waiting serviceMgr to be Ready";
  serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed");
  sleep 3;
done

echo "ServiceMgr is started"
echo "ServiceMgr and Ecs are Running... Please don't close it"

ecsPPID=$(ps -Ao pid,command | grep ecs.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
serviceMgrPPID=$(ps -Ao pid,command | grep serviceMgr.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)

ecsPID=$(pgrep -laP $ecsPPID | cut -d ' ' -f1)
serviceMgrPID=$(pgrep -laP $serviceMgrPPID | cut -d ' ' -f1)

echo "$ecsPID  ecsPID" > ecsSvcProcessesIDs.info
echo "$serviceMgrPID  serviceMgrPID" >> ecsSvcProcessesIDs.info
