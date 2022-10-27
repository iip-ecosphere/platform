cd Files/Install/gen/broker

setsid ./broker.sh &> broker.log &

brokerReady=$(cat broker.log | grep "Qpid Broker Ready")
while [ -z "$brokerReady" ]; do
  echo "Waiting broker to be Ready";
  brokerReady=$(cat broker.log | grep "Qpid Broker Ready");
  sleep 3;
done

echo "Broker is started"

cd ..

setsid ./platform.sh &> platform.log &

platformReady=$(cat platform.log | grep "Startup completed")
while [ -z "$platformReady" ]; do
  echo "Waiting platform to be Ready";
  platformReady=$(cat platform.log | grep "Startup completed");
  sleep 3;
done

echo "Platform is started"

setsid ./ecs.sh --iip.id=$1 &> ecs.log &

ecsReady=$(cat ecs.log | grep "Startup completed")
while [ -z "$ecsReady" ]; do
  echo "Waiting ecs to be Ready";
  ecsReady=$(cat ecs.log | grep "Startup completed");
  sleep 3;
done

echo "Ecs is started"

setsid ./serviceMgr.sh --iip.id=$1 &> serviceMgr.log &

serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed")
while [ -z "$serviceMgrReady" ]; do
  echo "Waiting serviceMgr to be Ready";
  serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed");
  sleep 3;
done

echo "ServiceMgr is started"
echo "Broker and Platform are Running... Please don't close it"

brokerPPID=$(ps -Ao pid,command | grep broker.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
platformPPID=$(ps -Ao pid,command | grep platform.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
ecsPPID=$(ps -Ao pid,command | grep ecs.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
serviceMgrPPID=$(ps -Ao pid,command | grep serviceMgr.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)

brokerPID=$(pgrep -laP $brokerPPID | cut -d ' ' -f1)
platformPID=$(pgrep -laP $platformPPID | cut -d ' ' -f1)
ecsPID=$(pgrep -laP $ecsPPID | cut -d ' ' -f1)
serviceMgrPID=$(pgrep -laP $serviceMgrPPID | cut -d ' ' -f1)

cd ..
cd ..

echo "$brokerPID  brokerPID" > ProcessesIDs.info
echo "$platformPID  platformPID" >> ProcessesIDs.info
echo "$ecsPID  ecsPID" >> ProcessesIDs.info
echo "$serviceMgrPID  serviceMgrPID" >> ProcessesIDs.info
