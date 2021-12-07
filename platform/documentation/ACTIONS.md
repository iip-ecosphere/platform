# IIP-Ecosphere platform: Administrative actions

This document is intended for maintainers and developers. It is currently not linked from other documents.

## Create demonstration containers of the platform

* Go to github web page
* Select [iip-ecosphere/platform](https://github.com/iip-ecosphere/platform)
* Select "Actions"
* Select "Publish docker containers"
* Select "Run workflow" | "Run workflow"

This will run the github action script and deploy the containers to dockerhub. Please note that the script requires secret settings in your user profile, here `secrets.DOCKER_HUB_USER` and `secrets.DOCKER_HUB_PW` (a valid access token).

Please cleanup old runs when the information is superfluous. Demonstration containers are automatically build when a version of the repository is created on github.

## Archive certain containers in dockerhub

Let's assume that we want to archive the two demonstration containers under a tag related to the build date (e.g., `20211206`). Run the following commands on a Linux machine with good internet connection:

    docker pull iipecosphere/platform:platform_all.latest
    docker pull iipecosphere/platform:cli.latest
    docker image tag iipecosphere/platform:platform_all.latest iipecosphere/platform:platform_all.20211206
    docker image tag iipecosphere/platform:cli.latest iipecosphere/platform:cli.20211206
    docker login --username iipecosphere
    docker image push iipecosphere/platform:platform_all.20211206
    docker image push iipecosphere/platform:cli.20211206
    docker logout
    