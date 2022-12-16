cd Files/Install/gen/

if [ $1 == "deploy" ]; then
  ./cli.sh deploy $2
elif [ $1 == "undeploy" ]; then  
  ./cli.sh undeploy $2
fi