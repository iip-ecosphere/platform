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

## Setting up the Management UI

An installed management UI contains a setup JSON file in `assets/config/config.json`, which is created/modified during platform instantiation based on the UI configuration in the configuration model. Currently, an example looks as follows

    {
        "ip": "http://192.168.0.199:9001",
        "urn": "urn%3A%3A%3AAAS%3A%3A%3AiipEcosphere%23"
    }
    
whereby the `ip` points to the platform AAS server and the the `urn` denotes the URN of the IIP-Ecosphere platform AAS.