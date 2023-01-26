# IIP-Ecosphere platform: Extended dependency plugin for Maven

We use the `maven-dependency-plugin` for various tasks, e.g., to unpack Python code or the configuration model.  However, we do not limit ourselves to the target directory which may be cleaned up with mvn clean and other cleaning procedures do not work. So we decided a cleanup specification with the `unpack` goal of maven-dependency-plugin provided by this package. For convenience, we include further goals of the original plugin as they are used frequently in conjunction with `unpack`.

## Extended unpack goal

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
  
## copy-dependencies goal

The copy-dependencies goal is the same as in the original plugion and included here just for convenience.

## build-classpath goal

The build-classpath goal is the same as in the original plugion and included here just for convenience.

## Missing

Java-based tests as we do not understand how to correctly set up the testing harness. Testing is done here via ANT and, thus, not subject to coverage analysis.