#Build script for CI
#Assumption npm and angular cli installed
#!/bin/bash

npm install
ng build --base-href ./
mkdir pckg
timestamp=$(date +%Y%m%d%H%M%S)
zip -r pckg/IIP-Ecosphere-mgtUi-$timestamp.zip dist
tar czvf pckg/IIP-Ecosphere-mgtUi-$timestamp.tgz dist