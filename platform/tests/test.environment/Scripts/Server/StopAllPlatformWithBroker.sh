cd Files

brokerPID=$(cat ProcessesIDs.info | grep brokerPID | cut -d ' ' -f1)
platformPID=$(cat ProcessesIDs.info | grep platformPID | cut -d ' ' -f1)

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
