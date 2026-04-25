# oktoflow platform release guideline

For performing a release...
* Release/deploy EASy-Producer and change the snapshot versions in the platform to that version
* Deploy software that is not in Maven Central, e.g., alert manager and its logger.
* Inform all developing parties that a release is on the way and no commits shall be done until the release is completed (assuming that all involved parties were informed before that outstanding commits shall be done so that the release can happen in a clean CI state).
* Save relevant artifacts: install, documentation, container
* Change the (non-SNAPSHOT) version number using the `ChangePomVersion` tool in `MvnCentral`, e.g., `java ChangePomVersion <pathToPlatform> --oldParentPOMVersion=0.7.1-SNAPSHOT --newParentPOMVersion=0.8.0 --oldPOMVersion=0.7.1-SNAPSHOT --newPOMVersion=0.8.0 --properties=iip.version --excludes=.*[/,\\]target[/,\\].*` Change manually
  - the version number in `DataTypes.ivml` in `configuration.configuration`. 
  - the version number in `tools.pluginEnv`
* For a minimal validation up to the configuration layer, build and deploy locally so that your IDE can initially build the remaining components
  - `tools.lib`
  - `tools.maven.python`
  - `tools.maven.invoker`
  - `tools.maven.dependencies`
  - `platformDependencies`
  - `platformDependenciesBOM`
  - `platformDependenciesSpring`
  - `support.boot`
  - `support.log-slf4j-simple`
  - `support.commons-apache`
  - `support.yaml-snakeyaml`
  - `support.json-jackson`
  - `support.ssh-sshd`
  - `support.rest-spark`
  - `support.http-apache`
  - `support.websocket-websocket`
  - `support.processInfo-oshi`
  - `support.meter-micrometer`
  - `support.bytecode-bytebuddy`
  - `support.sysmetrics.bitmotec`
  - `support.sysmetrics.plcnext`
  - `support` 
  - `support.aas`
  - `support.iip-aas`
  - `support.aas.basyx`
  - `support.aas.basyx.server`
  - `support.aas.basyx1_0`
  - `support.aas.basyx2.commons`
  - `support.aas.basyx2.server`
  - `support.aas.basyx2`
  - `transport`
  - `test.amqp.qpid`
  - `test.mqtt.moquette`
  - `test.mqtt.hivemq`
  - `transport.amqp`
  - `transport.mqttv3`
  - `transport.spring`
  - `transport.spring.amqp`
  - `connectors`  
  - `connectors.opcuav1`  
  - `connectors.mqttv3`  
  - `services.environment` (with `-DskipTests`)
  - `services.environment.spring`
  - `services.spring.loader`
  - `services`
  - `test.simplestream.spring`
  - `services.spring`
  - `kiServices.functions`  
  - `kiServices.rapidminer.rtsaFake` (with `-DskipTests`) 
  - `kiServices.rapidminer.rtsa` (with `-DskipTests`) 
  - `security.services.kodex`  
  - `deviceMgt`
  - `deviceMgt.basicRegistry`
  - `deviceMgt.s3mock`
  - `ecsRuntime`
  - `ecsRuntime.docker`
  - `monitoring` (with `-DskipTests`)
  - `monitoring.prometheus`
  - `configuration.interface`
  - `configuration.easy` (initial build steps see [README.md](../configuration/configuration.easy/README.md))
  - `configuration.maven`
  - `platform`
  - `managementUi`
- `examples`
* Finalize platform, prepare record on Zenodo
* **Check** platform dependencies installation POM in **Install** package! 
* First, commit `tooks.lib` and the maven plugins in `tool`, then platform dependencies, then `support` and the rest. In most of the other cases, only changes to the POM parent entry are required. 
* Change version of IIP-examples and commit

* Get gnupgp and obtain a public/private keypair.
* Prepare your maven `settings.xml` so that a server entry for `ossrh-iip` with credentials from the project administration and a profile for `ossrh-iip` pointing to your GPG installation are defined.
* Download the [`MavenCentral`](../tools/MvnCentral) deployment project from the tools folder in the github repository.
    * Check the `pom.xml` so that all relevant top-level components are mentioned.
    * Check the `deploy.bat`.

* Temporarily disable `IvmlTests.testSerializerConfig1` for release/first compilation of next snapshot, e.g., in the CI set `-Diip.build.initial=true`. Commit the changes along the sequence of build dependencies. To speed up, bulk commits of higher parts such as `connectors` may be done but then require manual interruption of unwanted builds.
* Check the CI that all builds are completed. Generated test-apps or examples will not be released.
* Create a new version tag on github.
* Execute the POM in `MavenCentral` so that all artifacts are downloaded.
* Execute `deployment.bat` in `MavenCentral`.
* Log into Maven Central Sonatype nexus and close the current staging repository. If no validation errors occur, deploy that repository.
* Change the POM version number to the next SNAPSHOT version number. Change also the version number in `DataTypes.ivml` in `configuration.configuration`. First, commit the platform dependencies. In most of the other cases, only changes to the POM parent entry are required. 
* Commit the changes along the sequence of build dependencies. To speed up, bulk commits of higher parts such as `connectors` may be done but then require manual interruption of unwanted builds.
* If required, change back the EASy-Producer dependencies to the desired snapshot version.
* Reactivate `IvmlTests.testSerializerConfig1`, e.g. setting in the CI `-Diip.build.initial=false`.
* Check whether the containers in github were created/are archived with the actual date/version number and tag most recent images.
    * `docker pull iipecosphere/platform:cli.latest`
    * `docker pull iipecosphere/platform:platform_all.latest`
    * `docker pull iipecosphere/dev-container`
    * `docker image ls`
    * `docker tag <id> iipecosphere/platform:cli.<ver>`
    * `docker tag <id> iipecosphere/platform:platform_all.<ver>`
    * `docker tag <id> iipecosphere/dev-container:<ver>`
    * `docker login -u iipecosphere`
    * `docker push iipecosphere/platform:cli.<ver>`
    * `docker push iipecosphere/platform:platform_all.<ver>`
    * `docker push iipecosphere/dev-container:<ver>`
    * `docker logout`
* Inform all developing parties that the release is done, everybody shall update their workspaces, refresh their Maven dependencies and development can continue.
