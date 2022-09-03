# IIP-Ecosphere platform release guideline

For performing a release...
* Get gnupgp and obtain a public/private keypair.
* Prepare your maven `settings.xml` so that a server entry for `ossrh-iip` with credentials from the project administration and a profile for `ossrh-iip` pointing to your GPG installation are defined.
* Download the [`MavenCentral`](../tools/MvnCentral) deployment project from the tools folder in the IIP-Ecosphere github repository.
    * Check the `pom.xml` so that all relevant top-level components are mentioned.
    * Check the `deploy.bat`.
* Release/deploy EASy-Producer and change the snapshot versions in the platform to that version
* Deploy software that is not in Maven Central, e.g., alert manager and its logger.
* Inform all developing parties that a release is on the way and no commits shall be done until the release is completed (assuming that all involved parties were informed before that outstanding commits shall be done so that the release can happen in a clean CI state).
* Change the (non-SNAPSHOT) version number using the `ChangePomVersion` tool in `MvnCentral`. Change also the version number in `DataTypes.ivml` in `configuration.configuration`. First, commit the platform dependencies . In most of the other cases, only changes to the POM parent entry are required. **Check** platform dependencies installation POM in **Install** package! 
* Temporarily disable `IvmlTests.testSerializerConfig1` for release/first compilation of next snapshot, e.g., in the CI set `-Diip.build.initial=true`. Commit the changes along the sequence of build dependencies. To speed up, bulk commits of higher parts such as `connectors` may be done but then require manual interruption of unwanted builds.
* Check the CI that all builds are completed. Generated test-apps will not be released.
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
    * `docker images ls`
    * `docker tag <id> iipecosphere/platform:cli.<ver>`
    * `docker tag <id> iipecosphere/platform:platform_all.<ver>`
    * `docker login -u iioecosphere`
    * `docker push iipecosphere/platform:cli.<ver>`
    * `docker push iipecosphere/platform:platform_all.<ver>`
    * `docker logout`
* Inform all developing parties that the release is done, everybody shall update their workspaces, refresh their Maven dependencies and development can continue.
