# IIP-Ecosphere platform: Python plugins for Maven

Maven is the build system for the IIP-Ecosphere platform, in particular also for generated code, applications and generated application templates. However, so far we did not find suitable Maven plugins for Python code. This component provides two plugins, one for Python "compilation" (compile time syntax checking with pyflakes) and for executing Python unit test suites.

## Prerequisites

Python 3.8 and pyflakes.

## Python compiler plugin

The Python compiler plugin runs by default in the `compile` lifecycle phase with the goal `compile-python`. The plugin version is the version of your actual platform. As we typically use the platform dependencies as parent POM, the project version is automatically set to the actual platform (but you may of course adjust this). By default, the plugin tries to "compile" all `.py` files in `src/main/python` and `src/test/python`.

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>maven-python</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>compile-python</goal>
                    </goals>
                    <configuration>
                        <failOnError>true</failOnError>
                    </configuration>
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```

The compiler plugin supports the following configuration settings:
  - `failOnError` (default `true`, user property `python-compile.failOnError`): Whether the build process shall fail if Python compile errors are detected.
  - `skip` (default `false`, user property `python-compile.skip`) skips the execution of this plugin. 
  
## Python test plugin

The Python test plugin currently executes Python files, either those directly located in `src/test/python` or those specified by a file set. The plugin runs by default in the `test` lifecycle phase with the goal `test-python`.

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>maven-python</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>test-python</goal>
                    </goals>
                    <configuration>
                        <failOnError>true</failOnError>
                        <fileset>
                            <directory>src/test/python</directory>
                            <includes>
                                <include>**/*Test.py</include>
                            </includes>
                        </fileset>                    
                    </configuration>
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```

The compiler plugin supports the following configuration settings:
  - `failOnError` (default `true`, user property `python-compile.failOnError`): Whether the build process shall fail if Python compile errors are detected.
  - `modelProject` (default `../../../target/pySrc`, user property `python-test.modelProject`): Optional set if generated templates are moved, set to path of generated python 	sources.
  - `fileset` (default not given, execute all files directly located in `src/test/python`) is optional and can be used to determine the tests to be executed.
  - `skip` (default `false`, user property `python-compile.skip`) skips the execution of this plugin. 
  
  
## Combining the goals

You may specify both goals in different executions with individual configurations. Then an id per execution is required.
  
  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>maven-python</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <id>py-compile</id>
                    <goals>
                        <goal>compile-python</goal>
                    </goals>
                </execution>
                <execution>
                    <id>py-test</id>
                    <goals>
                        <goal>test-python</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```
  