cd Files/Install/gen/

containerID=$(curl $3 | grep "id:" | rev | cut -d ' ' -f1 | rev)

if [ $2 == "add" ]; then
  ./cli.sh container $1 $2 $3
else
  ./cli.sh container $1 $2 $containerID
fi

