# IIP-Ecosphere platform: Maven Central Deployment

This project contains an all-project dependencies POM as well as a Windows script to deploy all platform components to Maven central. Relying on a POM and a batch script may be a bit strange, but the CI process having individual tasks/responsibilities does not really fit to Maven structures, even not to a modules POM. For the full process, please refer to the [Deployment Guidelines](../../documentation/RELEASE.md). 

This project is not subject to CI and you only need to check it out if you are responsible for doing releases.