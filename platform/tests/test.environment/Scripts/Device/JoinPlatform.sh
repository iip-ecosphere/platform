cd Files

if [ $3 == "True" ]; then
    echo $1 | sudo -S rm -r platformFiles
    mkdir platformFiles
    cd platformFiles
    wget http://$4:4200/download/DeviceFolder.tar.gz
    tar xzpvf DeviceFolder.tar.gz
else
    cd platformFiles
fi

cd broker

if [ $5 != "Non" ]; then
    echo $1 | sudo -S setsid ./broker.sh $5 &> broker.log &
else
    echo $1 | sudo -S setsid ./broker.sh &> broker.log &
fi

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

echo $1 | sudo -S setsid ./ecs.sh --iip.id=$2 &> ecs.log &

ecsReady=$(cat ecs.log | grep "Startup completed")
while [ -z "$ecsReady" ]; do
  echo "Waiting ecs to be Ready";
  ecsReady=$(cat ecs.log | grep "Startup completed");
  sleep 3;
done

echo "Ecs is started"

echo $1 | sudo -S setsid ./serviceMgr.sh --iip.id=$2 &> serviceMgr.log &

serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed")
while [ -z "$serviceMgrReady" ]; do
  echo "Waiting serviceMgr to be Ready";
  serviceMgrReady=$(cat serviceMgr.log | grep "Startup completed");
  sleep 3;
done

echo "ServiceMgr is started"
echo "Ecs and ServiceMgr are Running... Please don't close it"
echo "DeviceID: $2"

brokerPID=$(ps -Ao pid,command | grep "cp brokerJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
ecsPID=$(ps -Ao pid,command | grep "cp ecsJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
serviceMgrPID=$(ps -Ao pid,command | grep "cp svcJars/" | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)

#brokerPID=$(pgrep -laP $brokerPPID | cut -d ' ' -f1)
#ecsPID=$(pgrep -laP $ecsPPID | cut -d ' ' -f1)
#serviceMgrPID=$(pgrep -laP $serviceMgrPPID | cut -d ' ' -f1)

echo "$ecsPID  ecsPID" > ecsSvcProcessesIDs.info
echo "$serviceMgrPID  serviceMgrPID" >> ecsSvcProcessesIDs.info
echo "$brokerPID  brokerPID" >> ecsSvcProcessesIDs.info