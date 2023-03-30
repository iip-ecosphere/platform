# IIP-Ecosphere platform: Invoker for Maven

Explicitly invoking multiple lifecycles, goals and profiles for execution is usually not possible from inside maven. The maven-invoker plugin allows such tasks for integration testing but not in simplified manner directly from a maven POM. This plugin is largely inspired by (the code of) maven-invoker.

This package binds with `invoke` in life-cycle phase `validate`, with `install` in life-cycle phase `install` and with `package` in life-cycle phase `package`.

The following example demonstrates the application of this plugin. We declare two profiles, `EasyGen` and `Main`. `EasyGen` shall be executed from `Main` (and explicitly via command line). The `Main` profile is declared to be activated by default and uses this plugin to define an execution on the `EasyGen` profile for phase `process-sources`. When executing `mvn`, the `Main` profile will be executed, which in turn executes `EasyGen`. Please be careful to not invoke the profile in which this plugin is declared, which may lead to an endless loop.


  ```xml
  <profiles>
      <profile>
         <id>EasyGen</id>
          <build>
              <plugins>
                 <plugin>
                    <!-- some plugin in phase process-sources -->
                </plugin>
            </plugins>
          </build>
      </profile>

      <profile>
         <id>Main</id>
         <activation>
            <activeByDefault>true</activeByDefault>
         </activation>
         <properties>
            <maven.main.skip>true</maven.main.skip>
            <maven.test.skip>true</maven.test.skip>
            <python-compile.skip>true</python-compile.skip>
            <maven.javadoc.skip>true</maven.javadoc.skip>
         </properties>       

          <build>
              <plugins>
                 <plugin>
                    <groupId>de.iip-ecosphere.platform</groupId>
                    <artifactId>invoker-plugin</artifactId>
                    <version>${project.version}</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>invoke</goal>
                            </goals>
                            <configuration>
                                <invokeGoals>
                                    <invokeGoal>process-sources</invokeGoal>
                                </invokeGoals>
                                <invokeProfiles>
                                    <invokeProfile>EasyGen</invokeProfile>
                                </invokeProfiles>
                                <disableJava>true</disableJava>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
          </build>
      </profile>
  </profiles>
  ```

The plugin supports the following configuration settings:
  - `invokeGoals` the mandatory goals to be invoked, each stated in an own element `invokeGoal`
  - `invokeProfiles` optional profiles to be invoked, each stated in an own element `invokeProfile` (default: not given)
  - `disableJava` sets system properties to disable Java compilation, Java test compilation, test execution and javadoc generation. Helpful shortcut for executing generation profiles.
  - `disablePython` sets system properties to disable IIP-Ecosphere Python "compilation" and testing.
  - `disableBuild` is a shortcut for `disableJava` and `disablePython`.
  - `systemProperties` properties to be set for the Maven POM to be called, given in terms of `key` and `value` entries (default: not given)
  - `pom` the POM file to execute (default: not given, i.e., the actual POM)
  - `offline` run the called POM in offline mode (default `false`)
  - `localRepositoryPath`the path to the local repository (default taken from Maven settings)
  - `showVersion` to display the version of the called Maven executable (default `false`)
  - `showErrors` on the called Maven POM (default `true`)
  - `javaHome` the home directory of the JDK/JRE to execute (default not set, using the one Maven was called on)
  - `mavenHome` the home directory of Maven to execute (default not set, using the one Maven was called on)
  - `timeoutInSeconds` a timeout when the invocation shall be terminated as failed execution (default `0`)
  
The plugin takes over the system properties of the original request, in particular `-Dunpack.force` and passes them to the invoked maven processes.
