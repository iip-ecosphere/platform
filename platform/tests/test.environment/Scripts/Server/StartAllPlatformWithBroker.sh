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
echo "Broker and Platform are Running... Please don't close it"

brokerPPID=$(ps -Ao pid,command | grep broker.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)
platformPPID=$(ps -Ao pid,command | grep platform.sh | grep -v grep | head -1 | xargs | cut -d ' ' -f -1)

brokerPID=$(pgrep -laP $brokerPPID | cut -d ' ' -f1)
platformPID=$(pgrep -laP $platformPPID | cut -d ' ' -f1)

cd ..
cd ..

echo "$brokerPID  brokerPID" > ProcessesIDs.info
echo "$platformPID  platformPID" >> ProcessesIDs.info
