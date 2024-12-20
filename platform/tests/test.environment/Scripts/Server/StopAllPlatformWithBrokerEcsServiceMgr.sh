cd Files

brokerPID=$(cat ProcessesIDs.info | grep brokerPID | cut -d ' ' -f1)
platformPID=$(cat ProcessesIDs.info | grep platformPID | cut -d ' ' -f1)
mgtUiPID=$(cat ProcessesIDs.info | grep mgtUiPID | cut -d ' ' -f1)
monitoringPID=$(cat ProcessesIDs.info | grep monitoringPID | cut -d ' ' -f1)
ecsPID=$(cat ProcessesIDs.info | grep ecsPID | cut -d ' ' -f1)
serviceMgrPID=$(cat ProcessesIDs.info | grep serviceMgrPID | cut -d ' ' -f1)

echo $1 | sudo -S kill $serviceMgrPID

serviceMgrKilled=$(ps -ef | grep  $serviceMgrPID | grep -v grep)
while [ -z "$serviceMgrKilled" ]; do
  echo "Waiting serviceMgr to be stopped";
  serviceMgrKilled=$(ps -ef | grep  $serviceMgrPID | grep -v grep);
  sleep 3;
done

echo "ServiceMgr is stopped"

echo $1 | sudo -S kill $ecsPID

ecsKilled=$(ps -ef | grep  $ecsPID | grep -v grep)
while [ -z "$ecsKilled" ]; do
  echo "Waiting ecs to be stopped";
  ecsKilled=$(ps -ef | grep  $ecsPID | grep -v grep);
  sleep 3;
done

echo "Ecs is stopped"

echo $1 | sudo -S kill $monitoringPID

monitoringKilled=$(ps -ef | grep  $monitoringPID | grep -v grep)
while [[ $monitoringKilled ]]; do
  echo "Waiting monitoring to be stopped";
  monitoringKilled=$(ps -ef | grep  $monitoringPID | grep -v grep);
  sleep 3;
done

echo "Monitoring is stopped"

echo $1 | sudo -S kill $mgtUiPID

mgtUiKilled=$(ps -ef | grep  $mgtUiPID | grep -v grep)
while [[ $mgtUiKilled ]]; do
  echo "Waiting manage UI to be stopped";
  mgtUiKilled=$(ps -ef | grep  $mgtUiPID | grep -v grep);
  sleep 3;
done

echo "Manage UI is stopped"

echo $1 | sudo -S kill $platformPID

platformKilled=$(ps -ef | grep  $platformPID | grep -v grep)
while [ -z "$platformKilled" ]; do
  echo "Waiting platform to be stopped";
  platformKilled=$(ps -ef | grep  $platformPID | grep -v grep);
  sleep 3;
done

echo "Platform is stopped"

echo $1 | sudo -S kill $brokerPID

brokerKilled=$(ps -ef | grep  $brokerPID | grep -v grep)
while [ -z "$brokerKilled" ]; do
  echo "Waiting broker to be stopped";
  brokerKilled=$(ps -ef | grep  $brokerPID | grep -v grep);
  sleep 3;
done

echo "Broker is stopped"

echo " " > ProcessesIDs.info
