#/bin/bash

IMGS=$(docker images -qa -f 'dangling=true')
echo "Cleaning up dangling docker images: $IMGS"
if [ ! -z "$IMGS" ]
then
    docker rmi -f "$IMGS"
fi

IMGS=$(docker images -qa "simplemeshtestingcontainerapp/*")
echo "Cleaning up IIP-Ecosphere configuration.configuration test images: $IMGS"
if [ ! -z "$IMGS" ]
then
    docker rmi -f "$IMGS" 
fi

docker system prune -f --volumes
rm -rf /tmp/tomcat-docbase.*
rm -rf /tmp/tomcat.*
rm -rf /tmp/qmProfiling*
rm -f /tmp/services.spring*.xml
rm -f /tmp/platform-*.aasx
rm -f /tmp/librocksdbjni*.so
rm -f /tmp/examples-test-*.out