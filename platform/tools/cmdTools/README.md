# oktoflow platform: command line tools / Utilities

This project contains generic, global command line tools:

* `de.iip_ecosphere.platform.maven.CleanMvnSnapshots` removes outdated snapshot versions leaving the latest 3 snapshots.
* `de.iip_ecosphere.platform.maven.ChangePomVersion` changes POM/parent POM versions. Please check platform dependencies installation in Install package!
* `de.iip_ecosphere.platform.maven.CleanMvnArtifacts` removes artifacts according to version/snapshot specification from a workspace or a maven repository.

Please note that programs in here shall be self-contained and, only if required, rely on basic layers of the platform like support. So far, no plugin loading is applied/needed, i.e., support plugins may be given directly as dependencies. Further, this project is not subject to CI.

So far, the tools are directly called from an IDE. If execution via mvn is needed, please feel free to add the respective plugins/executions.