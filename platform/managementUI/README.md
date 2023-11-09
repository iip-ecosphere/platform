# IIP-Ecosphere platform Management UI

## Prerequisites (for Ubuntu Server, CI)

- install node.js and node package manager (npm)
- install Angular CLI

Currently, we build the UI with node.js version 16.10.0, npm version 7.24.0 (or 9.5.1) and Angular 14.2.10. More recent versions may not be compatible. Please see also (Angular version compatibility matrix)[https://angular.io/guide/versions].

## Running Management UI from Code

- install dependencies: change directory to /managementUi and run `npm install`
- environment variables: change directory to /managementUi/src/assets/config and edit config.json. Change ip to the platform ip that the UI is supposed to connect to
- serve managementUI: change directory to /managementUi and `run ng serve`

## Building the Management UI

Handled now through `mvn compile`. On the first try, we recommend running the following two steps manually
  - `npm install` 
  - `ng build` 
  - `ng test` 

## Setting up the Management UI

An installed management UI contains a setup JSON file in `assets/config/config.json`, which is created/modified during platform instantiation based on the UI configuration in the configuration model. Currently, an example looks as follows

    {
        "ip": "http://192.168.0.199:9001",
        "urn": "urn%3A%3A%3AAAS%3A%3A%3AiipEcosphere%23"
    }
    
whereby the `ip` points to the platform AAS server and the the `urn` denotes the URN of the platform AAS.
