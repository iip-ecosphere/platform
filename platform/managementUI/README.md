# IIP-Ecosphere platform Management UI

## Prerequisites (for Ubuntu Server, CI)

- install node.js and node package manager (npm): `sudo apt install nodejs npm`
- install Angular CLI: `sudo npm install -g @angular/cli`

If your version is not recent enough, e.g., not 12:

- `sudo apt-get purge --auto-remove nodejs`
- `sudo apt-get install curl`
- `curl -sL https://deb.nodesource.com/setup_12.x | sudo -E bash -`
- `sudo apt-get install -y nodejs`

## Running Management UI from Code

- install dependencies: change directory to /managementUi and run `npm install`
- environment variables: change directory to /managementUi/src/assets/config and edit config.json. Change ip to the platform ip that the UI is supposed to connect to
- serve managementUI: change directory to /managementUi and `run ng serve`

## Building the Management UI

- install dependencies: change directory to /managementUi and run `npm install` 
- Angular Projekt build: `ng build --base-href ./` creates a build and stores it in folder `dist`