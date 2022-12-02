# IIP-Ecosphere platform: Extended dependency plugins for Maven

We use the `maven-dependency-plugin` for various tasks, e.g., to unpack Python code or the configuration model.  However, we do not limit ourselves to the target directory which may be cleaned up with mvn clean and other cleaning procedures do not work. So we decided a cleanup specification with the `unpack` goal of maven-dependency-plugin provided by this package. For convenience, we include further goals of the original plugin as they are used frequently in conjunction with `unpack`.

## Extended unpack goal

The extended `unpack` goal behaves like the original goal offering all configuration options, but in addition the `cleanup` option. Unpacking only happens if the output directory given in the respective artifact item does not exist or if `force` is specified, usually via command line.

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
                    </configuration>
                </execution>
            <executions>
        </plugin>
     </plugins>
  </build>
  ```

The extended unpack goal supports the following additional configuration settings:
  - `cleanup` (default not given) specifying the files and directories to be deleted (via `includes`) before unpacking. Typically, `directory` points to the `outputDirectory` of the artifact item(s).
  - `force` (default `false`) forces an update of the files applying a cleanup if needed. If `false`, unpacking only happens if the output folder does not exist.
  
  
## copy-dependencies goal

The copy-dependencies goal is the same as in the original plugion and included here just for convenience.

## build-classpath goal

The build-classpath goal is the same as in the original plugion and included here just for convenience.

## Missing

Java-based tests as we do not understand how to correctly set up the testing harness. Testing is done here via ANT and, thus, not subject to coverage analysis.