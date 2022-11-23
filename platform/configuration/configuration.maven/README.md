# IIP-Ecosphere platform: Maven plugins for application/platform instantiation

Maven plugin for the following platform installation tasks (represented as Maven goals in untypical notation).

* generateInterfaces: app interfaces, no apps
* generateAppsNoDeps: app interfaces, apps without artifact dependencies
* generateApps: app interfaces, apps with artifact dependencies
* generateBroker: create a sample broker
* generatePlatform: platform components only

A typical setup (an all-in-one-project implementation). happens in conjunction with the extended unpack resources plugin. As shown below, in the `prepare-sources` phase, the configuration meta model is obtained, then in `generate-sources` the applications are instantiated without dependencies. Not shown is the remaining setup for service implementation, which would happen in the `compile` and `test` phases. Finally, in the `package` phase the full applications are generated and assembled.

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
                    <phase>prepare-sources</phase>
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
                <modelDirectory>${project.basedir}/src/test/easy</modelDirectory>
                <outputDirectory>${project.basedir}/gen/py</outputDirectory>
                <tracingLevel>TOP</tracingLevel>
                <resourcesDirectory>${project.basedir}/${iip.resources}</resourcesDirectory>
                <fallbackResourcesDirectory>${project.basedir}/${iip.resources}</fallbackResourcesDirectory>
            </configuration>
        </plugin>
     </plugins>
  </build>
  ```

The phases support the following configuration settings: 
  - `model` the name of the IVML configuration model
  - `modelDirectory` the directory the configuration model is located with (currently, usually `src/test/easy`). If not absolute, the project base directory will be prepended.
  - `outputDirectory` the directory where to write the generated parts to (usually, `gen` or a sub-directory of it). If not absolute, the project base directory will be prepended.
  - `tracingLevel` the level of tracing during instantiation (`ALL` for everything, `TOP` for the top-level calls, `FUNC` for the VIL/VTL function level, default is `TOP`)
  - `resourcesDirectory` optional folder containing resources to be included into the application (see platform handbook, default `resources.ipr`). If given and not absolute, the project base directory will be prepended.
  - `fallbackResourcesDirectory` optional folder containing resources to be included into the application if `resourcesDirectory` does not exist (see platform handbook, default `resources`). If given and not absolute, the project base directory will be prepended.
