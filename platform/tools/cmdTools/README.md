# oktoflow platform: command line tools / Utilities

This project contains generic, global command line tools:

* Maven tools
    * `ChangePomVersion` changes POM/parent POM versions. Please check platform dependencies installation in Install package!
    * `CleanMvnSnapshots` removes outdated snapshot versions leaving the latest 3 snapshots.
    * `CleanMvnArtifacts` removes artifacts according to version/snapshot specification from a workspace or a maven repository.
    * `DetermineRemoteRepositories` determines remote repositories used by maven.
    * `MavenProfilerToCsvTest` merges and converts `maven-profile` output files to CSV
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

Pre-defined Maven execution points:
* `mvn exec:java@cleanTemp` cleans up the temporary folder of the current user, deleting typical oktoflow component leftovers (we do not have control over those).
* `mvn exec:java@jenkinsHtmlParser -Djenkins.html.file=`*file*, parses the given file and stores the extracted results into a CSV file in the same path/name as *file* just with file extension `.csv`.
* `mvn exec:java@cleanDumps` cleaning surefire dump files, assuming that this command has been called within the local git workspace, i.e., the workspace root is `../..`. Workspace root be specified with `-Dworkspace.basedir=`*folder*.
* `mvn exec:java@cleanSnapshots` cleans older maven snapshot files, leaving the most recent 5 snapshot files in place. Assumes local maven repository in the user home directory in `.m2/repository` or specifed via environment variable `M2_HOME`
* `mvn exec:java@cleanMvnArtifactsWorkspace` cleans the maven artifacts in the workspace, from `-DminVer=` (default `0.2.0`) to `-DmaxVer=` (default the actual oktoflow version), excluding `maxVer` (`-DincludingmaxVer=` with default `false`), including release artifacts (`-DincludingReleases=` with default `true`), not simulating the execution  (`-Dsimulate=` with default `false`).
* `mvn exec:java@cleanMvnArtifactsRepo` cleans the maven artifacts in the workspace, from `-DminVer=` (default `0.2.0`) to `-DmaxVer=` (default the actual oktoflow version), excluding `maxVer` (`-DincludingmaxVer=` with default `false`), including release artifacts (`-DincludingReleases=` with default `true`), not simulating the execution  (`-Dsimulate=` with default `false`).
* `mvn exec:java@changePomVersion` changes the POM/parent POM version of all `pom.xml` files found recursively in the workspace root (`../..`, or `-Dworkspace.basedir=`*folder*) from `-DoldParentPOMVersion=` (default the actual oktoflow version) to `-DnewParentPOMVersion=` (must be specified) as well as `-DoldPOMVersion=` (default the actual oktoflow version) to `-DnewPOMVersion=` (must be specified) with a predefined setup for oktoflow, not simulating the execution  (`-Dsimulate=` with default `false`). This command is for release preparation. Handle with care!

Please note that programs in here shall be self-contained and, only if required, rely on basic layers of the platform like support. So far, no plugin loading is applied/needed, i.e., support plugins may be given directly as dependencies. Further, this project is not subject to CI.

So far, the tools are directly called from an IDE. If execution via mvn is needed, please feel free to add the respective plugins/executions.