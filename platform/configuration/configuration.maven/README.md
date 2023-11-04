# oktoflow platform: Maven plugins for application/platform instantiation and testing

Maven plugin for the following platform configuration tasks (represented as Maven goals in untypical notation).

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
  - `fallbackResourcesDirectory` optional folder containing resources to be included into the application if `resourcesDirectory` does not exist (see platform handbook, default `resources``-Dconfiguration.fallbackResourcesDirectory=...`). If given and not absolute, the project base directory will be prepended.
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
  
The ``testApp`` goal (default phase ``package``, can be seen as integration test but our invoker disables tests to avoid conflicts with app in one-shot-projects) supports the following configuration settings: 
  - `testCmd` (`-Dconfiguration.testApp.testCmd=...`, default ``""``) the command to be executed for testing instead of an oktoflow application. If not given, an application is tested via ``maven exec:java@app``.
  - `appId` (`-Dconfiguration.testApp.appId=...`, default ``app``) the id for executing the application in test mode in the POM.
  - `appArgs` (`-Dconfiguration.testApp.appArgs=...`, default ``""``) additional arguments to be passed to the application.
  - `logFile` (`-Dconfiguration.testApp.logFile=...`, default ``""``) if given, the name/location of the file where to write the output of the tested application to. Per default, the output is merged into the output of this plugin.
  - `logRegExprs` (`-Dconfiguration.testApp.logRegExprs=...`, default ``""``) if given, regular expressions indicating in the application log that the application execution was successful (see also ``logRegExConjunction``).
  - `logRegExConjunction` (`-Dconfiguration.testApp.logRegExConjunction=...`, default ``true``) whether all specified regular expressions must match or at least one.
  - `skip` (`-Dconfiguration.testApp.skip=...`, default ``false``) skips the execution of this plugin.
  - `brokerPort` (`-Dconfiguration.testApp.brokerPort=...`, default ``-1``) the port to use for the communication broker, an ephemeral one is used if not positive.
  - `brokerWaitTime` (`-Dconfiguration.testApp.brokerWaitTime=...`, default ``3000``) time in ms to wait for the broker to be safely up.
  - `testTime` (`-Dconfiguration.testApp.testTime=...`, default ``120000``) time in ms to wait until the test shall be terminated as a failure (if not a log pattern match occurred before).
  - `platformDir` (`-Dconfiguration.testApp.platformDir=...`, default ``""``) directory into which an optional platform was instantiated. If not given, no platform services will be executed at all. 
  - `startPlatform` (`-Dconfiguration.testApp.startPlatform=...`, default ``true``) if ``platformDir`` is given, try to start the central platform services (transport adjusted to the ``brokerPort``). 
  - `startEcsRuntime` (`-Dconfiguration.testApp.startEcsRuntime=...`, default ``false``) if ``platformDir`` is given, try to start the ECS-runtime (transport adjusted to the ``brokerPort``). 
  - `startServiceManager` (`-Dconfiguration.testApp.startServiceManager=...`, default ``false``) if ``platformDir`` is given, try to start the service manager (transport adjusted to the ``brokerPort``, separated AAS implementation port than ECS-runtime). 
  - `startEcsServiceMgr` (`-Dconfiguration.testApp.startEcsServiceMgr=...`, default ``true``) if ``platformDir`` is given, try to start the combined ECS-Runtime/service manager (transport adjusted to the ``brokerPort``). 
