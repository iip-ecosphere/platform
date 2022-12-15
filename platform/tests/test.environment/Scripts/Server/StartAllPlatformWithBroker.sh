cd Files/Install/gen/broker

setsid ./broker.sh &> broker.log &

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

setsid ./platform.sh &> platform.log &

platformReady=$(cat platform.log | grep "Startup completed")
while [ -z "$platformReady" ]; do
  echo "Waiting platform to be Ready";
  platformReady=$(cat platform.log | grep "Startup completed");
  sleep 3;
done

echo "Platform is started"

setsid ./mgtUi.sh &> mgtUi.log &

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
echo "Broker and Platform are Running... Please don't close it"

brokerPPID=$(ps -Ao pid,command | grep broker.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
platformPPID=$(ps -Ao pid,command | grep platform.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
mgtUiPPID=$(ps -Ao pid,command | grep mgtUi.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
monitoringPPID=$(ps -Ao pid,command | grep monitoring.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)

brokerPID=$(pgrep -laP $brokerPPID | cut -d ' ' -f1)
platformPID=$(pgrep -laP $platformPPID | cut -d ' ' -f1)
mgtUiPID=$(pgrep -laP $mgtUiPPID | cut -d ' ' -f1)
monitoringPID=$(pgrep -laP $monitoringPPID | cut -d ' ' -f1)

cd ..
cd ..

echo "$brokerPID  brokerPID" > ProcessesIDs.info
echo "$platformPID  platformPID" >> ProcessesIDs.info
echo "$mgtUiPID  mgtUiPID" >> ProcessesIDs.info
echo "$monitoringPID  monitoringPID" >> ProcessesIDs.info