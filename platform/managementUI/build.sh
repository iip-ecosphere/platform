#!/bin/bash

# Build script for CI
# Assumption npm and angular cli installed

npm install
ng build --base-href ./
mkdir pckg
timestamp=$(date +%Y%m%d%H%M%S)
zip -r pckg/IIP-Ecosphere-mgtUi-$timestamp.zip dist
cp pckg/IIP-Ecosphere-mgtUi-$timestamp.zip pckg/IIP-Ecosphere-mgtUi-latest.zip
tar czvf pckg/IIP-Ecosphere-mgtUi-$timestamp.tgz dist
cp pckg/IIP-Ecosphere-mgtUi-$timestamp.tgz pckg/IIP-Ecosphere-mgtUi-latest.tgz

# run pseudo maven and deploy to SSE maven repo
# maven copies pckg/IIP-Ecosphere-mgtUi-latest.zip to target and renames it as Maven knows the version/project name
# ANT uses SSE macros to call maven and to deploy file in target
ant -f build-jk.xml