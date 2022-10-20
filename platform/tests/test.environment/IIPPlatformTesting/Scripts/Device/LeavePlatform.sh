cd Files 

ecsPID=$(cat ecsSvcProcessesIDs.info | grep ecsPID | cut -d ' ' -f1)
serviceMgrPID=$(cat ecsSvcProcessesIDs.info | grep serviceMgrPID | cut -d ' ' -f1)

kill $serviceMgrPID

serviceMgrKilled=$(ps -ef | grep  $serviceMgrPID | grep -v grep)
while [ -z "$serviceMgrKilled" ]; do
  echo "Waiting serviceMgr to be stopped";
  serviceMgrKilled=$(ps -ef | grep  $serviceMgrPID | grep -v grep);
  sleep 3;
done

echo "ServiceMgr is stopped"

kill $ecsPID

ecsKilled=$(ps -ef | grep  $ecsPID | grep -v grep)
while [ -z "$ecsKilled" ]; do
  echo "Waiting ecs to be stopped";
  ecsKilled=$(ps -ef | grep  $ecsPID | grep -v grep);
  sleep 3;
done

echo "Ecs is stopped"

echo " " > ecsSvcProcessesIDs.info
