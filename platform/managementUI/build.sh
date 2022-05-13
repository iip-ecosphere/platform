#Build script for CI
#Assumption npm and angular cli installed
#!/bin/bash

npm install
ng build --base-href ./
mkdir pckg
timestamp=$(date +%Y%m%d%H%M%S)
zip -r pckg/IIP-Ecosphere-mgtUi-$timestamp.zip dist
cp pckg/IIP-Ecosphere-mgtUi-$timestamp.zip pckg/IIP-Ecosphere-mgtUi-latest.zip
tar czvf pckg/IIP-Ecosphere-mgtUi-$timestamp.tgz dist
cp pckg/IIP-Ecosphere-mgtUi-$timestamp.tgz pckg/IIP-Ecosphere-mgtUi-latest.tgz