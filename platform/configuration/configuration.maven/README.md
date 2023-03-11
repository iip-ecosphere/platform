# IIP-Ecosphere platform: Maven plugins for application/platform instantiation

Maven plugin for the following platform installation tasks (represented as Maven goals in untypical notation).

* generateInterfaces: generate app interfaces, but no apps - executed only if `outputDirectory` is empty or IVML files in `modelDirectory`are newer than `outputDirectory`
* generateAppsNoDeps: app interfaces with apps, but without dependencies to artifacts - executed only if `outputDirectory` is empty or IVML files in `modelDirectory` are newer than `outputDirectory`
* generateApps: app interfaces with apps including artifact dependencies - executed always
* generateBroker: create a sample broker - executed only if `outputDirectory` is empty or IVML files in `modelDirectory`are newer than `outputDirectory`
* generatePlatform: exclusively platform components - executed only if `outputDirectory` is empty or IVML files in `modelDirectory`are newer than `outputDirectory`
* generateAll: generate everything

When you added this plugin to your POM, you may also execute the goals individually, e.g., using `mvn configuration:generateApps`. Depending on your setup it may be required that you may have to add a profile, e.g, in the platform examples `mvn -P EasyGen configuration:generateApps`.

A typical setup (an all-in-one-project implementation), happens in conjunction with the extended unpack resources plugin. As shown below, in the `generate-sources` phase, the configuration meta model is obtained, then in `generate-sources` the applications are instantiated without dependencies. For this, it is important that the`dependency-plugin` is stated in the same profile before this plugin and its executions. Not shown is the remaining setup for service implementation, which would happen in the `compile` and `test` phases. Finally, in the `package` phase the full applications are generated and assembled.

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
  - `modelDirectory` the directory the configuration model is located with (currently, usually and by default `src/test/easy`, `-Dconfiguration.modelDirectory=...`). If not absolute, the project base directory will be prepended.
  - `outputDirectory` the directory where to write the generated parts to (usually, `gen` or a sub-directory of it, default `gen`, `-Dconfiguration.outputDirectory=...`). If not absolute, the project base directory will be prepended.
  - `tracingLevel` the level of tracing during instantiation (`ALL` for everything, `TOP` for the top-level calls, `FUNC` for the VIL/VTL function level, default is `TOP`, `-Dconfiguration.tracingLevel=...`)
  - `resourcesDirectory` optional folder containing resources to be included into the application (see platform handbook, default `resources.ipr`, `-Dconfiguration.resourcesDirectory=...`). If given and not absolute, the project base directory will be prepended.
  - `fallbackResourcesDirectory` optional folder containing resources to be included into the application if `resourcesDirectory` does not exist (see platform handbook, default `resources``-Dconfiguration.fallbackResourcesDirectory=...`). If given and not absolute, the project base directory will be prepended.
  - `adjustOutputDirectoryIfGenBroker` (default `true`,`-Dconfiguration.adjustOutputDirectoryIfGenBroker=...`) adjust the output directory to the sub-directory `broker` if the goal is `generateBroker` using any `gen` folder as parent, or if no `gen` folder is on the path, using the actual output directory as parent folder for `broker`.
  
All goals take over the maven offline mode and pass it on to the instantiation for further consideration in maven sub-calls.
  
In addition, the goals `generateApps` and `generateAppsNoDeps` consider
  - `apps` (`-Dconfiguration.apps=...`) a comma separated list of application ids (as defined in the IVML configuration) to be build
