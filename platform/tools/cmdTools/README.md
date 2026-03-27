# oktoflow platform: command line tools / Utilities

This project contains generic, global command line tools:

* Maven tools
    * `ChangePomVersion` changes POM/parent POM versions. Please check platform dependencies installation in Install package!
    * `CleanMvnSnapshots` removes outdated snapshot versions leaving the latest 3 snapshots.
    * `CleanMvnArtifacts` removes artifacts according to version/snapshot specification from a workspace or a maven repository.
    * `DetermineRemoteRepositories` determines remote repositories used by maven.
* File system tools
    * `CleanDumps` cleans surefire dump files in the workspace.
    * `CleanTemp` cleans your temporary folder from typical leftovers of used libraries.
    * `FileList` prints the list of files with sizes in a directory for easier copying/using in Excel than Windows `dir` outputs.
    * `PluginList` prints the list of plugins with sizes in a directory for easier copying/using in Excel than Windows `dir` outputs.
* Plugin tools
    * `CleanIndexes` cleans plugin index files.
    * `InspectIndex` inspects single or multiple plugin index files and prints their contents.
* CI tools
    * `JenkinsHtmlParser` parses the CI page and extracts relevant information.

Please note that programs in here shall be self-contained and, only if required, rely on basic layers of the platform like support. So far, no plugin loading is applied/needed, i.e., support plugins may be given directly as dependencies. Further, this project is not subject to CI.

So far, the tools are directly called from an IDE. If execution via mvn is needed, please feel free to add the respective plugins/executions.