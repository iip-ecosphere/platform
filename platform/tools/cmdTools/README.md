# oktoflow platform: command line tools / Utilities

This project contains generic, global command line tools:

* `de.iip_ecosphere.platform.maven.CleanMvnSnapshots` removes outdated snapshot versions leaving the latest 3 snapshots.
* `de.iip_ecosphere.platform.maven.ChangePomVersion` changes POM/parent POM versions. Please check platform dependencies installation in Install package!
* `de.iip_ecosphere.platform.maven.CleanMvnArtifacts` removes artifacts according to version/snapshot specification from a workspace or a maven repository.

Please note that programs in here shall be self-contained and not rely on (inner) platform dependencies. Further, this project is not subject to CI.