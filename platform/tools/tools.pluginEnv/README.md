# oktoflow platform: Plugin Environment

Simple Plugin Environment for Plugin testing. Place the plugin classpath files that you need into `target/standalone`, run `de.oktoflow.platform.tools.pluginEnv` (might need adjustments), and then `test.bat`. Results shall be in `log`.

You may use it as it is, or in a relocatable form, add the dependency to a maven POM, and execute it through 

'''
        <plugin>
          <groupId>org.codehaus.mojo</groupId>
          <artifactId>exec-maven-plugin</artifactId>
          <version>3.0.0</version>
          <executions>
              <execution>
                 <id>pluginEnv</id>
                 <configuration>
                     <mainClass>de.oktoflow.platform.tools.pluginEnv.Standalone</mainClass>
                 </configuration>
              </execution>
          </executions>
        </plugin>
'''

Arguments can be given via system settings to ease calling through maven, e.g., `mvn -Dokto.version=$OKTO_VERSION -Dokto.pwd=$BASE -Dokto.files=./standalone exec:java@pluginEnv` utilizes the oktoflow version in `$OKTO_VERSION`, `$BASE` as directory where built platform code/projects can be found (the workspace folder with `support`, `configuration` etc. or a feasible subset thereof) and `./standalone` as directory where plugin descriptors are read from and the classpath file is written to.