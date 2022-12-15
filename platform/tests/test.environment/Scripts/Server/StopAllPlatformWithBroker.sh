cd Files

brokerPID=$(cat ProcessesIDs.info | grep brokerPID | cut -d ' ' -f1)
platformPID=$(cat ProcessesIDs.info | grep platformPID | cut -d ' ' -f1)
mgtUiPID=$(cat ProcessesIDs.info | grep mgtUiPID | cut -d ' ' -f1)
monitoringPID=$(cat ProcessesIDs.info | grep monitoringPID | cut -d ' ' -f1)
((monitoringPID=monitoringPID+1))

echo $1 | sudo -S kill $monitoringPID

monitoringKilled=$(ps -ef | grep  $monitoringPID | grep -v grep)
while [[ $monitoringKilled ]]; do
  echo "Waiting monitoring to be stopped";
  monitoringKilled=$(ps -ef | grep  $monitoringPID | grep -v grep);
  sleep 3;
done

echo "Monitoring is stopped"

kill $mgtUiPID

mgtUiKilled=$(ps -ef | grep  $mgtUiPID | grep -v grep)
while [[ $mgtUiKilled ]]; do
  echo "Waiting manage UI to be stopped";
  mgtUiKilled=$(ps -ef | grep  $mgtUiPID | grep -v grep);
  sleep 3;
done

echo "Manage UI is stopped"

kill $platformPID

platformKilled=$(ps -ef | grep  $platformPID | grep -v grep)
while [ -z "$platformKilled" ]; do
  echo "Waiting platform to be stopped";
  platformKilled=$(ps -ef | grep  $platformPID | grep -v grep);
  sleep 3;
done

echo "Platform is stopped"

kill $brokerPID

brokerKilled=$(ps -ef | grep  $brokerPID | grep -v grep)
while [ -z "$brokerKilled" ]; do
  echo "Waiting broker to be stopped";
  brokerKilled=$(ps -ef | grep  $brokerPID | grep -v grep);
  sleep 3;
done

echo "Broker is stopped"

echo " " > ProcessesIDs.info
