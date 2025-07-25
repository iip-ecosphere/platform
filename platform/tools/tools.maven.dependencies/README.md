# IIP-Ecosphere platform: Extended dependency plugin for Maven

We use the `maven-dependency-plugin` for various tasks, e.g., to unpack Python code or the configuration model.  However, we do not limit ourselves to the target directory which may be cleaned up with `mvn clean` and other cleaning procedures do not work. So we decided to add a cleanup specification to `unpack` goal of maven-dependency-plugin provided by this package. Similarly, we need to add further non-classpath files to `build-classpath` when creating classpath files for platform instances. For convenience, we include further goals such as `delete` or `copy` of the original plugin we use them frequently in conjunction with `unpack`. Basic properties of the underlying maven dependendency plugin can be applied although not explicitly discussed here.

## unpack goal

The extended `unpack` goal behaves like the original goal offering all configuration options, but in addition the `cleanup` option. Unpacking only happens if the output directory given in the respective artifact item does not exist, it only contains files listed in `initiallyAllowed` or if `force` is specified, usually via command line.

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>dependency-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <id>unpack</id>
                    <goals>
                        <goal>unpack</goal>
                    </goals>
                    <phase>process-sources</phase>
                    <configuration>
                        <artifactItems>
                            <!-- as in the original plugin -->
                        </artifactItems>
                        <cleanup>
                            <directory>${project.basedir}/src/main/python</directory>
                            <includes>
                                <include>HelloWorld.py</include>
                            </includes>
                        </cleanup>
                        <initiallyAllowed>Test.ivml, Test.text</initiallyAllowed>
                        <initiallyAllowedFile>src/main/easy/initiallyAllowed.txt</initiallyAllowedFile>
                    </configuration>
                </execution>
            <executions>
        </plugin>
     </plugins>
  </build>
  ```

The extended unpack goal supports the following additional configuration settings:
  - `cleanup` (default not given) specifying the files and directories to be deleted (via `includes`) before unpacking. Typically, `directory` points to the `outputDirectory` of the `artifactItems`.
  - `force` (default `false`, user property `unpack.force`) forces an update of the files applying a cleanup if needed. If `false`, unpacking only happens if the output folder does not exist (see `initiallyAllowed`).
  - `initiallyAllowed` (default not given, user property `unpack.initiallyAllowed`) list of colon or semicolon separated relative file names (default empty) or file wildcards (`?` for one character, `*` for arbitrary characters) in one of the `outputDirectory` settings in the `artifactItems`. Path separators may be given in Windows or Linux manner and will be normalized. If an existing `outputDirectory` only contains `initiallyAllowed` files, perform the unpacking anyway, else only if `force` (or `-Dunpack.force`) is given.
  - `initiallyAllowedFile` (default not given, user property `unpack.initiallyAllowed`) like `initiallyAllowed` but given as a text file with one allowed file/wildcard per line. The initially allowed file is automatically part of the initially allowed file set. If both, `initiallyAllowed` and `initiallyAllowedFile` are given, first the allowed files from `initiallyAllowedFile` and then those in `initiallyAllowed` are added to the set of allowed files.
  - `skip` (default `false`, user property `mdep.skip`, inherited from original maven plugin) skips the execution of this plugin.
  - `skipIfExists` (default `false`, user property `unpack.skipIfExists`) skips the execution of this plugin if the file or folder specified in this property exists.
  - `forceCleanup` (default `false`, user property `unpack.forceCleanup`) forces the cleanup of `cleanup` before doing any checks/validating the artifacts/executing the plugin.
  - `logCleanup` (default `false`, user property `unpack.logCleanup`) enables/disables logging the cleaned up files
  
## copy-dependencies goal

The ``copy-dependencies`` goal is the same as in the original plugin and included here just for convenience.

## build-classpath goal

The ``build-classpath`` goal is the same as in the original plugin and allows for adding further entries at the beginning or ending of a classpath file.
- `prepends` is a list of `prepend` string entries specifying complete classpath entries to be prepended to the `outputFile` concatenated by `pathSeparator`
- `appends` is a list of `append` string entries specifying  complete classpath entries to be prepended to the `outputFile` concatenated by `pathSeparator`
- `rollout` rolls out the given classpath setup to a similar file for Linux (postfix `-linux`) and a file for Windows (postfix `-win`) with operating system settings like file/path separator applied automatically
- `befores` lines to be prepended before the classpath without modification, uses `lineSeparator` between given lines and classpath; e.g., use # to have comments before the classpath, see [the Java command documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#java-command-line-argument-files)
- `afters` lines to be appended after the classpath without modification, uses `lineSeparator` between given lines and classpath; e.g., use # to have comments before the classpath, see [the Java command documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html#java-command-line-argument-files)
- `lineSeparator` (default the carriage return, user property `mdep.lineSeparator`) used as separator between `befores`, `afters` and the classpath 

The expressions `${self}` or `${self-test}` can be used in prepends/appends to add the jar/test-jar artifact of the actual project.

## build-plugin-classpath goal

Specialized goal to build a plugin classpath file. Based on the refined ``build-classpath`` above, but ships with a pre-configuration for plugin classpaths. In more details, sets the output file to `${project.build.directory}/classes/classpath`, `prependGroupId` to `true`, `overWriteIfNewer` to `true`, `localRepoProperty` to `target/jars`, `prefix` to `target/jars`, `fileSeparator` to `/`, `pathSeparator` to `:`, and, if not set otherwise, `includeScope` to `runtime`. Further, prepends the own artifact by default and via `addTestArtifact` also the corresponding test artifact. Adds a set of befores as comments, including `prefix`, `unpackMode`, `setupDescriptor` and `pluginIds` (see below). 

- `addTestArtifact` (default `false`, user property `mdep.addTestArtifact`) adds the test artifact based on the actual project
- `unpackMode` (default `jars`, user property `mdep.unpackMode`) specifies how unpacking shall happen, i.e., whether jars are included in the plugin artifact (`jars`) or whether they shall be resolved (`resolve`)
- `setupDescriptor` (default `FolderClasspath`, user property `mdep.setupDescriptor`) specifies the descriptor implementation that shall be announced to the plugin manager, may be a shortcut for platform supplied descriptors (`FolderClasspath`, `CurrentClassloader`, `ClasspathFile`, `PluginBased`, `Process`) or a qualified classname assuming a non-arg constructor
- `pluginIds` (default empty, user property `mdep.pluginIds`) specifies pluginIds for the `PluginBased` setup descriptor, may also be used with others whereby then the plugin is also announced by `PluginBased`

## diff-classpath goal

The ``diff-classpath`` goal based on the original plugin and for diffing actual project dependencies against some container root dependencies, i.e., deliver the specific dependency tree.
- `rootCoordinates` comma separated list of root dependencies to diff against.

## copy goal

The ``copy`` goal is the same as in the original plugin and included here just for convenience.

## tree goal

The ``tree`` goal is the same as in the original plugin and included here just for convenience.

## delete goal

The delete goal allows to just delete files and directories. At it's core, it is similar to the
``cleanup`` part of the ``unpack`` goal discussed above. Starting at a given ``directory`` it deletes all 
``includes`` (included files and folders) while skipping the ``excludes`` (excluded files and folders).

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>dependency-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <id>delete</id>
                    <goals>
                        <goal>delete</goal>
                    </goals>
                    <phase>initialize</phase>
                    <configuration>
                        <files>
                            <directory>${project.basedir}/src/main/python</directory>
                            <includes>
                                <include>HelloWorld.py</include>
                            </includes>
                        </files>
                    </configuration>
                </execution>
            <executions>
        </plugin>
     </plugins>
  </build>
  ```

## unpack-plugins

oktoflow plugins ship as zip files with contained classpath file(s) and jars in `target/jars`. This extension of the unpack goal eases the unpacking of plugins for tests and platform installation.

In the basic version, for testing, use 

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>dependency-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <id>plugins</id>
                    <goals>
                        <goal>unpack-plugins</goal>
                    </goals>
                    <phase>prepare-package</phase>
                    <configuration>
                        <plugins>
                            <plugin>
                                <name>support.aas.basyx2</name>
                                <appends> <!-- complement the plugin, add the platform logging -->
                                   <append>log-slf4j-simple</append>
                                </appends>                                
                            </plugin>
                            <plugin>
                                <name>support.aas.basyx</name>
                                <appends> <!-- complement the plugin, add the platform logging -->
                                   <append>log-slf4j-simple</append>
                                </appends>
                            </plugin>
                        </plugins>
                        <version>${iip.version}</version>
                    </configuration>
                </execution>
            <executions>
        </plugin>
     </plugins>
  </build>
  ```

for installation just add `<relocate>true</relocate>` to the `configuration`. The `plugins` are extended `ArtifactItems` which you may use instead. However, a `plugin` allows a more concise notation as we set up the `version` to the global `version` in `configuration`, the `type` to `zip`, the `classifier` to `plugin`, `overWrite` to `true` and `outputDirectory` to `${project.build.directory}/oktoPlugins`. If in a `plugin` the `groupId` is not given, we set it automatically to `de.iip-ecosphere.platform`. A plugin may have `appends`, simple names of previously unpacked plugins that shall be merged in given sequence in the classpath of the actual plugin, e.g., to include intentionally excluded plugins, such as logging, which is decided/merged into for platform services/applications by the platform instantiation. Takes into account the prefix path and the unpack mode written by the `build-plugin-classpath` plugin.

Moreover, for installations, if `relocate` is enabled, the `outputDirectory` becomes `jars`, all unpacked jars are flattened into that directory and all classpath files are renamed based on the last part of the `artifactId`, stored into `plugins` and relocated to the relocation target folder (`relocateTarget`, user property `unpack.relocateTarget`, default `jars`); if specified, `plugins` becomes a sibling of the relocation target folder. In relocation mode, if dependencies shall be `resolved`, `resolveAndCopy` determins whether they shall be copied to the target folder (default) or taken from the local maven repository.

If we are not in `relocate` mode, the plugin is only enabled, if the relative directories `../../support/support` (for arbitrary platform component) or `../support` (for support component) do not exist, which is the case for builds outside a local git workspace, e.g., on CI.

## Missing

Java-based tests as we do not understand how to correctly set up the testing harness. Testing is done here via ANT and, thus, not subject to coverage analysis.