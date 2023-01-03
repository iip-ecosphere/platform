#/bin/bash

IMGS=$(docker images -qa -f 'dangling=true')
echo "Cleaning up dangling docker images: $IMGS"
if [ ! -z "$IMGS" ]
then
    docker rmi -f "$IMGS"
fi

IMGS=$(docker images -qa "simplemeshtestingcontainerapp/dflt")
echo "Cleaning up IIP-Ecosphere configuration.configuration test images: $IMGS"
if [ ! -z "$IMGS" ]
then
    docker rmi -f "$IMGS" 
fi