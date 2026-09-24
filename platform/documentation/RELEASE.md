# oktoflow platform release guideline

## For performing a release...
* Release/deploy EASy-Producer and change the snapshot versions in the platform to that version
* Deploy software that is not in Maven Central, e.g., alert manager and its logger, jlxd in EASy-Producer.
* Inform all developing parties that a release is on the way and no commits shall be done until the release is completed (assuming that all involved parties were informed before that outstanding commits shall be done so that the release can happen in a clean CI state).
* Save relevant artifacts: install, documentation, container
* Change the (non-SNAPSHOT) version number using the `ChangePomVersion` tool in `MvnCentral`, e.g., `java ChangePomVersion <pathToPlatform> --oldParentPOMVersion=0.7.1-SNAPSHOT --newParentPOMVersion=0.8.0 --oldPOMVersion=0.7.1-SNAPSHOT --newPOMVersion=0.8.0 --properties=iip.version --excludes=.*[/,\\]target[/,\\].*` Change manually
  - the version number of EASy-Producer in `platformDependencies`
  - the version number in `DataTypes.ivml` in `configuration.configuration`. 
* For a minimal validation up to the configuration layer, build and deploy locally so that your IDE can initially build the remaining components
  - `tools.dependencies`
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
  - `support` 
  - `support.ssh-sshd`
  - `support.rest-spark`
  - `support.http-apache`
  - `support.websocket-websocket`
  - `support.processInfo-oshi`
  - `support.metrics-micrometer`
  - `support.bytecode-bytebuddy`
  - `support.aas`
  - `support.sysmetrics.bitmotec`
  - `support.sysmetrics.plcnext`
  - `support.aas.basyx`
  - `support.aas.basyx.server`
  - `support.aas.basyx1_0`
  - `support.aas.basyx2.commons`
  - `support.aas.basyx2.server`
  - `support.aas.basyx2`
  - `support.iip-aas`
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
  - `services.environment` (may fail if qpid remainders are still there, e.g., /tmp/qpid folder)
  - `services.environment.spring`
  - `services.spring.loader`
  - `services`
  - `services.spring/test.simplestream.spring`
  - `services.spring`
  - `kiServices.functions`  
  - `kiServices.rapidminer.rtsaFake` (if neither `JAVA8_HOME` is set to the JDK8 home directory nor `-Diip.test.java8` is set to the java binary in JDK8, `-DskipTests` may be needed) 
  - `kiServices.rapidminer.rtsa` (if neither `JAVA8_HOME` is set to the JDK8 home directory nor `-Diip.test.java8` is set to the java binary in JDK8, `-DskipTests` may be needed)
  - `security.services.kodex`  
  - `deviceMgt`
  - `deviceMgt.basicRegistry`
  - `deviceMgt.s3mock`
  - `ecsRuntime`
  - `ecsRuntime.docker`
  - `monitoring`
  - `monitoring.prometheus`
  - `configuration.interface`
  - `configuration.easy` (initial build steps see [README.md](../configuration/configuration.easy/README.md))
  - `configuration.maven`
  - `platform`
  - `managementUi` (may require cleanup of `gen` and `target/easy`, `target/easy-test` and `target/gen`))
  - `examples`  (may require 2 runs), examples then with `-Dunpack.force=true`
* Finalize platform, prepare record on Zenodo
* **Check** platform dependencies installation POM in **Install** package! 
* First, commit 
  - `tools.dependencies`
  - `tools.lib`
  - the Maven plugins in `tools`
  - the parent poms in `platformDependencies`, `platformDependenciesBOM`, `platformDependenciesSpring` 
  - `support` and `test`
  - the the rest. 
* Change version of IIP-examples to corresponding release commit
* Check the CI that all builds are completed, all required plugins are there. Generated test-apps or examples will not be released.
* Deploy to Maven central. On the deployment machine
  - Hint: we cannot deploy `test.simpleStream.spring` as Maven Central does not understand split classpath and also not `ecsRuntime.lxc` unless EASy deploys jlxc components
  - Get gnupgp and obtain a public/private keypair.
  - Copy `deployCentral.sh` and `insert.txt` from [`MavenCentral`](../tools/MvnCentral)
  - Prepare your `settings.sh`
  - Execute script `deployCentral.sh`.
  - Log in to `central.sonatype.com`, validate the validation. If no validation errors occur, deploy the deployment.
* Create a new version tag for platform, IIP-Examples and oktoflow2grafana on github.
* Change the POM version number to the next SNAPSHOT number. Change also the version number in `DataTypes.ivml` in `configuration.configuration`. First, commit the platform dependencies. In most of the other cases, only changes to the POM parent entry are required. 
* Change POM version of IIP-examples to next SNAPSHOT and commit
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

## Examples in Eclipse after update

Eclipse may still show build path errors even after re-building the project with the new version via maven. Typically, updating maven dependencies in Eclipse, updating the project, closing and re-opening helps, sometimes also re-booting Eclipse or manually deleting the error message and updating maven dependencies again.

## oktoflow plugins after a release

oktoflow plugins are loaded locally if tests are executed within the full git workspace (not for sparse checkouts as on CI). Local execution is based on the plugin classpath files/index files and bypasses the plugins in the Maven repository. This is usually faster than doing a full plugin round-trip, but requires careful rebuild of affected platform components after a change (see sequence above). By default, local execution is enabled and after a release, a local rebuild as listed above is required. If you want to disable local plugin execution in tests, use `-Dokto.plugins.enableLocal=false` or the corresponding environment variable `OKTO_PLUGINS_ENABLELOCAL=false`.