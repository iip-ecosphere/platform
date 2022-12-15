echo $1 | sudo -S docker run --rm --expose 8000 --env iip.port=8000 -d --network=host -v /var/run/docker.sock:/var/run/docker.sock --name IIPEcs $2/simplemeshtestingapp/ecs:0.1.0

containerLog=$(sudo -S docker logs IIPEcs)
ecsReady=$(echo "$containerLog" | grep "Startup completed")
while [ -z "$ecsReady" ]; do
  echo "Waiting ecs to be Ready";
  containerLog=$(sudo -S docker logs IIPEcs);
  ecsReady=$(echo "$containerLog" | grep "Startup completed");
  sleep 3;
done

deviceID=$(echo "$containerLog" | grep "d.i.p.e.d.AasxDeviceAasProvider - Checking AAS for id" | rev | cut -d ' ' -f1 | rev)

echo "Ecs is Running... Please don't close it"
echo "DeviceID: $deviceID"