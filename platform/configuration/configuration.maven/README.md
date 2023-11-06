# oktoflow platform: Maven plugins for application/platform instantiation and testing

Maven plugin for the following platform configuration tasks (represented as Maven goals in atypical notation).

# Platform instantiation

* generateInterfaces: generate app interfaces, but no apps - executed only if `outputDirectory` is empty or IVML files in `modelDirectory`are newer than `outputDirectory`
* generateAppsNoDeps: app interfaces with apps, but without dependencies to artifacts - executed only if `outputDirectory` is empty or IVML files in `modelDirectory` are newer than `outputDirectory`
* generateApps: app interfaces with apps including artifact dependencies - executed always
* generateBroker: create a sample broker - executed only if `outputDirectory` is empty or IVML files in `modelDirectory` are newer than `outputDirectory`
* generatePlatform: exclusively platform components - executed only if `outputDirectory` is empty or IVML files in `modelDirectory` are newer than `outputDirectory`
* generateAll: generate everything

When you added this plugin to your POM, you may also execute the goals individually, e.g., using `mvn configuration:generateApps`. Depending on your setup it may be required that you may have to add a profile, e.g, in the platform examples `mvn -P EasyGen configuration:generateApps`.

A typical setup (an all-in-one-project implementation), happens in conjunction with the extended unpack resources plugin. As shown below, in the `generate-sources` phase, the configuration meta model is obtained, then in `generate-sources` the applications are instantiated without dependencies. For this, it is important that the `dependency-plugin` is stated in the same profile before this plugin and its executions. Not shown is the remaining setup for service implementation, which would happen in the `compile` and `test` phases. Finally, in the `package` phase the full applications are generated and assembled.

  ```xml
  <properties>
        <iip.resources>resources</iip.resources>
  </properties>
  
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
                    <phase>generate-sources</phase>
                    <configuration>
                       <artifactItem>
                         <groupId>de.iip-ecosphere.platform</groupId>
                         <artifactId>configuration.configuration</artifactId>
                         <version>${project.version}</version>
                         <classifier>easy</classifier>
                         <type>zip</type>
                         <overWrite>true</overWrite>
                         <outputDirectory>${project.basedir}/src/main/easy</outputDirectory>
                         <destFileName>model.jar</destFileName>
                       </artifactItem>
                        <cleanup>
                            <directory>${project.basedir}/src/main/easy</directory>
                             <directory>${project.basedir}/src/main/easy</directory>
                             <includes>
                                 <include>cfg</include>
                                 <include>files</include>
                                 <include>meta</include>
                                 <include>vtl</include>
                                 <include>IIPEcosphere*.*</include>
                             </includes>
                        </cleanup>
                    </configuration>
                </execution>
            <executions>
         </plugin>

         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>configuration-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <execution>
                    <id>generateAppsNoDeps</id>
                    <goals>
                        <goal>generateAppsNoDeps</goal>
                    </goals>
                    <phase>generate-sources</phase>
                </execution>
                <execution>
                    <id>generateApps</id>
                    <goals>
                        <goal>generateApps</goal>
                    </goals>
                    <phase>package</phase>
                </execution>
            </executions>
            <configuration>
                <model>ExamplePython</model>
                <modelDirectory>src/test/easy</modelDirectory>
                <outputDirectory>gen/py</outputDirectory>
                <tracingLevel>TOP</tracingLevel>
                <resourcesDirectory>${iip.resources}</resourcesDirectory>
                <fallbackResourcesDirectory>${iip.resources}</fallbackResourcesDirectory>
                <adjustOutputDirectoryIfGenBroker>true</adjustOutputDirectoryIfGenBroker>
            </configuration>
        </plugin>
     </plugins>
  </build>
  ```

The goals support the following configuration settings: 
  - `model` (`-Dconfiguration.model=...`) the name of the IVML configuration model. This value must be given.
  - `modelDirectory` the directory where the configuration model is located with (currently, usually and by default `src/test/easy`, `-Dconfiguration.modelDirectory=...`). If not absolute, the project base directory will be prepended.
  - `metaModelDirectory` the directory where the configuration meta-model is located with (currently, usually and by default `src/main/easy`, `-Dconfiguration.metaModelDirectory=...`). If not absolute, the project base directory will be prepended.
  - `outputDirectory` the directory where to write the generated parts to (usually, `gen` or a sub-directory of it, default `gen`, `-Dconfiguration.outputDirectory=...`). If not absolute, the project base directory will be prepended.
  - `tracingLevel` the level of tracing during instantiation (`ALL` for everything, `TOP` for the top-level calls, `FUNC` for the VIL/VTL function level, default is `TOP`, `-Dconfiguration.tracingLevel=...`)
  - `resourcesDirectory` optional folder containing resources to be included into the application (see platform handbook, default `resources.ipr`, `-Dconfiguration.resourcesDirectory=...`). If given and not absolute, the project base directory will be prepended.
  - `fallbackResourcesDirectory` optional folder containing resources to be included into the application if `resourcesDirectory` does not exist (see platform handbook, default `resources`, `-Dconfiguration.fallbackResourcesDirectory=...`). If given and not absolute, the project base directory will be prepended.
  - `adjustOutputDirectoryIfGenBroker` (default `true`,`-Dconfiguration.adjustOutputDirectoryIfGenBroker=...`) adjust the output directory to the sub-directory `broker` if the goal is `generateBroker` using any `gen` folder as parent, or if no `gen` folder is on the path, using the actual output directory as parent folder for `broker`.
  - `force`(default `true`,`-Dconfiguration.force=...`) force the execution irrespective of file dates and `-Dunpack.force`
  
All goals take over the maven offline mode and pass it on to the instantiation for further consideration in maven sub-calls.
  
In addition, the goals `generateApps` and `generateAppsNoDeps` consider
  - `apps` (`-Dconfiguration.apps=...`) a comma separated list of application ids (as defined in the IVML configuration) to be build

# Application testing

Testing a generated application typically requires starting the communication broker (possibly on an emphemeral port) and the the application in a time-framed manner and to check the application output for some patterns indicating that the application is working. Within the configuration plugin, a minimal Maven setup looks like:

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>configuration-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <!-- executions from above before, in particular generateApps -->
                <execution>
                    <id>testApp</id>
                    <goals>
                        <goal>testApp</goal>
                    </goals>
                    <configuration>
                        <logRegExprs>
                            <logRegExpr>.*RECEIVED.*</logRegExpr>
                        </logRegExprs>
                    </configuration>
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```
  
The `testApp` goal (default phase `package`, can be seen as integration test but our invoker disables tests to avoid conflicts with app in one-shot-projects) allows for testing an oktoflow application either via `mvn exec:java@app` or through a given command. In case of maven, passes on the value of the environment variable `MAVEN_SETTINGS_PATH` or if not given the maven user settings as user settings file location. Starts the required broker and may start platform services. This goal supports the following configuration settings: 

  - `testCmd` (`-Dconfiguration.testApp.testCmd=...`, default `""`) the command to be executed for testing instead of an oktoflow application. Arguments are in `appArgs`. If not given, an application is tested via `mvn exec:java@app`.
  - `testCmdAsScript` (`-Dconfiguration.testApp.testCmdAsScript=...`, default `false`) to indicate that the command is a script and may need special treatment, e.g., on windows (automatically set `true` for `ant`, `mvn` or `npm` or `ng`)
  - `appId` (`-Dconfiguration.testApp.appId=...`, default `app`) the id for executing the application through `mvn exec:java@app` in test mode as given in the POM.
  - `appArgs` (`-Dconfiguration.testApp.appArgs=...`, default `""`) additional arguments given as individual `appArg` entries to be passed to the application.
  - `mvnArgs` (`-Dconfiguration.testApp.mvnArgs=...`, default `""`) additional arguments given as individual `mvnArg` entries to be passed to a maven execution.
  - `mvnPluginArgs` (`-Dconfiguration.testApp.mvnPluginArgs=...`, default `""`) additional arguments given as individual `mvnPluginArg` entries to be passed to a maven plugin execution.
  - `logFile` (`-Dconfiguration.testApp.logFile=...`, default `""`) if given, the name/location of the file where to write the output of the tested application to. Per default, the output is merged into the output of this plugin.
  - `logRegExprs` (`-Dconfiguration.testApp.logRegExprs=...`, default `""`) if given, regular expressions indicating in the application log that the application execution was successful (see also `logRegExConjunction`).
  - `logRegExConjunction` (`-Dconfiguration.testApp.logRegExConjunction=...`, default `true`) whether all specified regular expressions must match or at least one.
  - `skip` (`-Dconfiguration.testApp.skip=...`, default `false`) skips the execution of this plugin.
  - `brokerPort` (`-Dconfiguration.testApp.brokerPort=...`, default `-1`) the port to use for the communication broker, an ephemeral one is used if not positive, execution of broker and platform are skipped if `0`.
  - `brokerWaitTime` (`-Dconfiguration.testApp.brokerWaitTime=...`, default `3000`) time in ms to wait for the broker to be safely up.
  - `testTime` (`-Dconfiguration.testApp.testTime=...`, default `120000`) time in ms to wait until the test shall be terminated as a failure (if not a log pattern match occurred before).
  - `platformDir` (`-Dconfiguration.testApp.platformDir=...`, default `""`) directory into which an optional platform was instantiated. If not given, no platform services will be executed at all. 
  - `startPlatform` (`-Dconfiguration.testApp.startPlatform=...`, default `true`) if `platformDir` is given, try to start the central platform services (transport adjusted to the `brokerPort`). 
  - `startEcsRuntime` (`-Dconfiguration.testApp.startEcsRuntime=...`, default `false`) if `platformDir` is given, try to start the ECS-runtime (transport adjusted to the `brokerPort`). 
  - `startServiceManager` (`-Dconfiguration.testApp.startServiceManager=...`, default `false`) if `platformDir` is given, try to start the service manager (transport adjusted to the `brokerPort`, separated AAS implementation port than ECS-runtime). 
  - `startEcsServiceMgr` (`-Dconfiguration.testApp.startEcsServiceMgr=...`, default `true`) if `platformDir` is given, try to start the combined ECS-Runtime/service manager (transport adjusted to the `brokerPort`). 
  - `deploymentPlan` (`-Dconfiguration.testApp.deploymentPlan=...`, default `""`) if `testCmd` and `platformDir` are set, defines a deployment plan to be started with the platform around the test, resource in deployment plan must be `deploymentResource`
  - `deploymentResource` (`-Dconfiguration.testApp.deploymentResource=...`, default `local`) the resource to be used in `deploymentPlan`
  - `befores` (`-Dconfiguration.testApp.befores=...`, default `""`) specifies additional processes to be started before the test and to be stopped afterwards
    - `description` the description of the process
    - `cmd` the command to be executed
    - `cmdAsScript` optional flag whether `cmd` is a script (automatically set `true` for `ant`, `mvn` or `npm` or `ng`, default `false`)
    - `args` optional arguments of the process, as above
    - `networkPort` optional network port to be assigned, ephemeral if negative, ignored if 0 (default `0`)
    - `networkPortProperty` optional name of the maven property to be set and to be made available, substituted in args `${networkPortProperty}`
    - `home` optional home directory of the process
    - `waitFor` whether the process shall be executed and completed before continuing (default `true`)

# Process execution

This artifact also contains the `process` goal (default `compile` phase) for executing processes.

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>configuration-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <!-- executions from above above where applicable -->
                <execution>
                    <id>process</id>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <processes>
                            <process>
                                <description>...</description>
                                <cmd>...</cmd>
                                <args>
                                   <arg>...</arg>
                                </args>
                            </process>
                        </processes>
                    </configuration>                    
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```

The goal can be configured as follows
  - `processes` (`-Dconfiguration.process.processes=...`, default `""`) specifies processes to be executed:
    - `description` the description of the process
    - `cmd` the command to be executed
    - `cmdAsScript` optional flag whether `cmd` is a script (automatically set `true` for `ant`, `mvn` or `npm` or `ng`, default `false`)
    - `args` optional arguments of the process, as above
    - `home` optional home directory of the process

# Angular build

For pragmatic reasons, this artifact also contains the `ngBuild` goal (default `compile` phase) for building angular applications.

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>configuration-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <!-- executions from above above where applicable -->
                <execution>
                    <id>ngBuild</id>
                    <goals>
                        <goal>ngBuild</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```

It can be configured by:
  - `skip` (`-Dconfiguration.ngBuild.skip=...`, default `false`) skips the execution of this plugin.

# Angular test

For pragmatic reasons, this artifact also contains the `ngTest` goal (default `test` phase) for building angular applications.

  ```xml
  <build>
      <plugins>
         <plugin>
            <groupId>de.iip-ecosphere.platform</groupId>
            <artifactId>configuration-plugin</artifactId>
            <version>${project.version}</version>
            <executions>
                <!-- executions from above above where applicable -->
                <execution>
                    <id>ngTest</id>
                    <goals>
                        <goal>ngTest</goal>
                    </goals>
                    <!-- configuration may go here -->
                </execution>
            </executions>
        </plugin>
     </plugins>
  </build>
  ```

It can be configured by:
  - `skip` (`-Dconfiguration.ngTest.skip=...`, default `false`) skips the execution of this plugin.
  - `noWatch` (`-Dconfiguration.ngTest.noWatch=...`, default `true`) disables watching.
  - `noProgress` (`-Dconfiguration.ngTest.noProgress=...`, default `true`) disables progress output.
  - `headless` (`-Dconfiguration.ngTest.headless=...`, default `true`) enables headless mode.
  