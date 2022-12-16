cd Files/Install/gen/

if [ $2 == "add" ]; then
  ./cli.sh services $1 $2 $3
else
  serviceID=$(unzip -p $3 BOOT-INF/classes/deployment.yml | grep "id: " | head -1 | rev | cut -d ' ' -f1 | rev)
  
  ./cli.sh services $1 $2 $serviceID
fi