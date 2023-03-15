EcsContainer=$(curl -s 'http://'$2'/v2/_catalog' | jq -r '.[]' | jq -r '.[] | select(contains("ecs"))')

EcsContainerTags=$(curl -L -s http://$2/v2/$EcsContainer/tags/list | jq '.tags[0]' | sed 's/"//g')

echo $1 | sudo -S $3 run --pull=always --rm --expose 8000 --env iip.port=8000 -d --network=host -v /var/run/docker.sock:/var/run/docker.sock --name IIPEcs $2/$EcsContainer:$EcsContainerTags

containerLog=$(sudo -S $3 logs IIPEcs)
ecsReady=$(echo "$containerLog" | grep "Startup completed")
while [ -z "$ecsReady" ]; do
  echo "Waiting ecs to be Ready";
  containerLog=$(sudo -S $3 logs IIPEcs);
  ecsReady=$(echo "$containerLog" | grep "Startup completed");
  sleep 3;
done

deviceID=$(echo "$containerLog" | grep "d.i.p.e.d.AasxDeviceAasProvider - Checking AAS for id" | rev | cut -d ' ' -f1 | rev)

echo "Ecs is Running... Please don't close it"
echo "DeviceID: $deviceID"