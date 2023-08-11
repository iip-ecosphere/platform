cd Files/Install/gen/broker

echo $1 | sudo -S setsid ./broker.sh &> broker.log &

if [[ $(cat broker.sh | grep amqp) ]]; then
    brokerReady=$(cat broker.log | grep "Qpid Broker Ready")
    while [ -z "$brokerReady" ]; do
      echo "Waiting broker to be Ready";
      brokerReady=$(cat broker.log | grep "Qpid Broker Ready");
      sleep 3;
    done
elif [[ $(cat broker.sh | grep mqtt) ]]; then
    brokerReady=$(cat broker.log | grep "Started EmbeddedHiveMQ")
    while [ -z "$brokerReady" ]; do
      echo "Waiting broker to be Ready";
      brokerReady=$(cat broker.log | grep "Started EmbeddedHiveMQ")
      sleep 3;
    done
fi

echo "Broker is started"

cd ..

echo $1 | sudo -S setsid ./platform.sh &> platform.log &

platformReady=$(cat platform.log | grep "Startup completed")
while [ -z "$platformReady" ]; do
  echo "Waiting platform to be Ready";
  platformReady=$(cat platform.log | grep "Startup completed");
  sleep 3;
done

echo "Platform is started"

echo $1 | sudo -S setsid ./mgtUi.sh &> mgtUi.log &

mgtUiReady=$(cat mgtUi.log | grep "Server listening on port")
while [ -z "$mgtUiReady" ]; do
  echo "Waiting manage UI to be Ready";
  mgtUiReady=$(cat mgtUi.log | grep "Server listening on port");
  sleep 3;
done

echo "Manage UI is started"

echo $1 | sudo -S setsid ./monitoring.sh &> monitoring.log &

monitoringReady=$(cat monitoring.log | grep "Server is ready to receive web requests")
while [ -z "$monitoringReady" ]; do
  echo "Waiting monitoring to be Ready";
  monitoringReady=$(cat monitoring.log | grep "Server is ready to receive web requests");
  sleep 3;
done

echo "Monitoring is started"

echo $1 | sudo -S setsid ./ecs.sh --iip.id=$1 &> ecs.log &

ecsReady=$(cat ecs.log | grep "Startup completed")
while [ -z "$ecsReady" ]; do
  echo "Waiting ecs to be Ready";
  ecsReady=$(cat ecs.log | grep "Startup completed");
  sleep 3;
done

echo "Ecs is started"

echo $1 | sudo -S setsid ./serviceMgr.sh --iip.id=$1 &> serviceMgr.log &

serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed")
while [ -z "$serviceMgrReady" ]; do
  echo "Waiting serviceMgr to be Ready";
  serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed");
  sleep 3;
done

echo "ServiceMgr is started"
echo "Broker and Platform are Running... Please don't close it"

brokerPID=$(ps -Ao pid,command | grep "cp brokerJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
platformPID=$(ps -Ao pid,command | grep "cp plJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
mgtUiPID=$(ps -Ao pid,command | grep "nodejs server.js" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
monitoringPID=$(ps -Ao pid,command | grep "cp monJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
ecsPID=$(ps -Ao pid,command | grep "cp ecsJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
serviceMgrPID=$(ps -Ao pid,command | grep "cp svcJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)

cd ..
cd ..

echo "$brokerPID  brokerPID" > ProcessesIDs.info
echo "$platformPID  platformPID" >> ProcessesIDs.info
echo "$mgtUiPID  mgtUiPID" >> ProcessesIDs.info
echo "$monitoringPID  monitoringPID" >> ProcessesIDs.info
echo "$ecsPID  ecsPID" >> ProcessesIDs.info
echo "$serviceMgrPID  serviceMgrPID" >> ProcessesIDs.info
