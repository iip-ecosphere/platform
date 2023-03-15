echo $1 | sudo -S $2 exec -d IIPEcs /bin/sh -c  "echo -n -e 'kill ' > kill.sh; cat /run/iip-ecsRuntime.pid >> kill.sh; chmod u+x kill.sh"
echo $1 | sudo -S $2 exec -d IIPEcs /bin/sh -c /kill.sh