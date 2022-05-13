# IIP-Ecosphere platform Management UI

## Prerequisites (for Ubuntu Server, CI)

- install node.js and node package manager (npm): `sudo apt install nodejs npm`
- install Angular CLI: `sudo npm install -g @angular/cli`

## Building the Management UI
- install dependencies: change directory to /managementUi and run `npm install`
- environment variables: change directory to /managementUi/src/assets/config and edit config.json. Change ip to the platform ip that the UI is supposed to connect to
- serve managementUI: change directory to /managementUi and `run ng serve`
