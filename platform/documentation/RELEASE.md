# IIP-Ecosphere platform release guideline

For performing a release...
* Get gnupgp and obtain a public/private keypair.
* Prepare your maven `settings.xml` so that a server entry for `ossrh-iip` with credentials from the project administration and a profile for `ossrh-iip` pointing to your GPG installation are defined.
* Download the [`MavenCentral`](../tools/MvnCentral) deployment project from the tools folder in the IIP-Ecosphere github repository.
    * Check the `pom.xml` so that all relevant top-level components are mentioned.
    * Check the `deploy.bat`.
* Release/deploy EASy-Producer and change the snapshot versions in the platform to that version
* Inform all developing parties that a release is on the way and no commits shall be done until the release is completed (assuming that all involved parties were informed before that outstanding commits shall be done so that the release can happen in a clean CI state).
* Go through all projects and change the (non-SNAPSHOT) version number appropriately. First, adjust the platform dependencies. In most of the other cases, only changes to the POM parent entry are required. However, for the `Install` package, the POM in the main project directory as well as the POMs in the `broker` and `platformDependencies` folders must be changed.
* Temporarily disable `IvmlTests.testSerializerConfig1` for release/first compilation of next snapshot. Commit the changes along the sequence of build dependencies. To speed up, bulk commits of higher parts such as `connectors` may be done but then require manual interruption of unwanted builds.
* Check the CI that all builds are completed. Generated test-apps will not be released.
* Create a new version tag on github.
* Execute the POM in `MavenCentral` so that all artifacts are downloaded.
* Execute `deployment.bat` in `MavenCentral`.
* Log into Maven Central Sonatype nexus and close the current staging repository. If no validation errors occur, deploy that repository.
* Go through all projects and change to the next SNAPSHOT version number. First, adjust the platform dependencies. In most of the other cases, only changes to the POM parent entry are required. However, for the `Install` package, the POM in the main project directory as well as the POMs in the `broker` and `platformDependencies` folders must be changed.
* Commit the changes along the sequence of build dependencies. To speed up, bulk commits of higher parts such as `connectors` may be done but then require manual interruption of unwanted builds.
* If required, change back the EASy-Producer dependencies to the desired snapshot version.
* Reactivate `IvmlTests.testSerializerConfig1`.
* Inform all developing parties that the release is done, everybody shall update their workspaces, refresh their Maven dependencies and development can continue.
