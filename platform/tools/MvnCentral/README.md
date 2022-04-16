# IIP-Ecosphere platform: Maven Central Deployment / Utilities

This project contains an all-project dependencies POM as well as a Windows script to deploy all platform components to Maven central. Relying on a POM and a batch script may be a bit strange, but the CI process having individual tasks/responsibilities does not really fit to Maven structures, even not to a modules POM. For the full process, please refer to the [Deployment Guidelines](../../documentation/RELEASE.md). 

Moreover, this project contains two Maven utilities:
* `de.iip_ecosphere.platform.maven.CleanMvnSnapshots` removes outdated snapshot versions leaving the latest 3 snapshots.
* `de.iip_ecosphere.platform.maven.ChangePomVersion` changes POM/parent POM versions.

Please note that programs in here shall be self-contained and not rely on IIP-Ecosphere dependencies. Further, this project is not subject to CI.