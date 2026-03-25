# oktoflow platform: Build commands

## Building all-in-one examples

As stated above, directly after obtaining this project, the app will not run and even show compile errors. This is due to the fact that we do not commit generated files, i.e., the build process must be executed once to get rid of these "errors".

Ensure that the Maven platformDependencies are installed (see [install](../tools/Install), for the examples also the examples-only variant will do).

For building, in the project folder of the example app:

  * Execute `mvn install` This will perform the broker-instantiation, the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
  * To prevent running the test cases, add `-DdisableJavaTests=true` or `-DdisablePythonTests=true`, respectively. For disabling Javadoc generation, add `-DenableJavadoc=false`.
  * The main build steps can be executed individually (see [Maven configuration plugin](../configuration/configuration.maven/README.md)), usally with explicit profile `-P EasyGen`.

As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place)

The build steps are explained in more detail in the Platform Handbook.

## Building all-in-one examples manually

The following build steps are automatically executed by an all-in-one example build process.

* Update the meta-model if needed or forced (see below)
* Generate application template(s), service interfaces, serializers and data classes `mvn -P EasyGen process-sources` (see [configuration maven plugin](../configuration/configuration.maven) for the detailed build steps associated with `process-sources`)
* Compile and package the application `mvn -P App install` (see [configuration maven plugin](../configuration/configuration.maven) for the detailed build steps associated with `package`)
* Integrate the application `mvn -P EasyGen package`

The generated code template contains helper scripts for Windows/Linux that call a default variant of the build process (`build.bat`, `build.sh`).
Sometimes, it makes sense to execute individual steps separately, e.g., `mvn -P EasyGen configuration:generateAppsNoDeps` for a run building all integration code files leaving out all dependencies (no deps). Although this application may not run, the build process creates the application templates in this step.
Currently the goals `generateInterfaces`, `generateAppsNoDeps` and `generateApps` create as last step the application templates.

## Updating the meta-model of apps

The meta-model defines the configuration types as well as the actions for code generation. The meta-model is not automatically updated. This allows the owner to declide when potential upgrades to own code shall be applied. This may be needed when platform interface changes occur (seldom), i.e., compile errors may appear in your application, which are compensated by a matching configuration meta model.

  * To update/upgrade the model, call `mvn -P EasyGen -U generate-sources -Dunpack.force=true`
  * Some apps/platform components are built without Maven profiles, where then usually `mvn -U generate-sources -Dunpack.force=true` is the right call. Please consult the respective `README.md` file
  
As a side effect, also this command creates the application code templates. The generated code template contains helper scripts for Windows/Linux (`updateModel.bat`, `updateModel.sh`) that ease the update of the meta model.

>Shortcut: Updating the platform, the meta-model and building the app, you may use on all-in-one examples `mvn -U -Dunpack.force=true install`.

## Using a different resource folder

The default folder for resources to be packaged directly into application artifacts is `resources` in the project root. However, there are cases where this folder shall not be used, e.g., when IPR-protected binaries shall be packaged into an application. Then, a mirror folder of `resources` shall be created, usually called `resources.ipr`, which can be passed to the build process using `-Diip.resources="resources.ipr"`. Please keep in mind that by default, the `.gitignore` file of application projects excludes this folder to avoid committing IPR-protected files to a public repository. 

## Platform

Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))

  * `mvn install`, for component updates `mvn -U install`

## Failures

* When building an example, Maven complains about missing platformDependencies, in particular for a SNAPSHOT version. We did not repeate the repository declaration in all examples, which, for a fresh installation (also with a new version) means that maven does not know from where to download the basic platform dependencies. Please install the platform dependencies first as stated above.
* After successfully installing the platform/platform dependencies, Maven complains again. This may be due to the fact that build processes try to avoid repeating steps, i.e., from the last try there are leftovers that disturb the build process. In your project folder, delete the `target/gen` folder or try `mvn clean`.

## Build settings and flags

We summarize now some helpful build flags, either realized by Maven, Maven build plugins or by oktoflow build plugins as indicated. We start with standard Maven build processes that run in one stage and do not need Maven profiles. Most oktoflow components rely on such build processes. Below, `=<val>` means that one of the indicated values must be provided after the equals sign, `=<bool>` that either `true` or `false` can be used, where usually no value implicitly leads to `true`.

| flag      | (typical) phase           |     setting                                 | description                                                                                                                                   | defined by                                                         |
|-----------|---------------------------|---------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
|  -U       |  *all*                    |                                             | forces dependency and build plugin updates                                                                                                    |   mvn                                                              |
|  -o       |  *all*                    |                                             | offline, prevents updates; may speed up builds; depending on repository settings, may fail e.g., once a day as snapshot updates are required  |   mvn                                                              |
|           | generate-sources, install | `-Dunpack.force`                            | forces/disables resource update/unpacking, in particular oktoflow metamodel                                                                   | [resources build plugin](../tools/maven.dependences)               |
|           | install, package          | `-Dconfiguration.skipMapDashboard=<bool>`   | enables/disables dashboard mapping                                                                                                     | [configuration build plugin](../configuration/configuration.maven) |
|           | install, package          | `-Dconfiguration.tracingLevel=<val>`        | changes the tracing level of the platform instantiator, values `ALL`, `TOP` (the default), `FUNC`tion                                         | [configuration build plugin](../configuration/configuration.maven) |
|           | install, package          | `-DskipTests`                               | Usual, non-invoker builds: disable Java tests                                                                                                 |   mvn                                                              |
|           | install, package          | `-Deasy.docker.skip=<bool>`                 | enables/disables Docker container building                                                                                                    | EASy-Producer                                                      |
|           | install, package          | `-Deasy.docker.failOnError`                 | enables/disables ingoring Docker build failures                                                                                               | EASy-Producer                                                      |

Please note that complex build processes, e.g., for examples or platform components like AAS, service execution or configuration, typically run in multiple stages using the extended [invoker](../tools/tools.maven.invoker). These stages are typically specified in terms of common dependencies and build steps as well as Maven profiles to add specific dependencies or build steps. As oktoflow builds inherit from the core platform POMs, also the default dependencies and build steps become active in the profiles. To prevent this, e.g., in the profile that is active by default and shall only execute the invoker, some profiles use switches/properties like those summarized above as presets to disable unneeded or even problematic build steps. Due to these presets, which cannot be overwritten by the invoker, the oktoflow invoker defines additional switches like those shown below that can take precedence in these cases.

| flag      | (typical) phase           |     setting                          | description                                                                                                                                   | defined by                                                         |
|-----------|---------------------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
|           | install, package          | `-Dpython-compile.skip=<bool>`       | Non-invoker builds: enables/disables Python "compilation", i.e., syntax check                                                                 | [python build plugin](../tools/tools.maven.python)                 |
|           | install, package          | `-Dpython-test.skip=<bool>`          | Non-invoker builds: enables/disables Python unit test execution                                                                               | [python build plugin](../tools/tools.maven.python)                 |
|           | install, package          | `-Dmaven.javadoc.skip`               | Non-invoker builds: enables/disables Java documentation building                                                                              | mvn                                                                | 
|           | install, package          | `-DdisableJavaTests=<bool>`          | Invoker-builds: Invoker builds: disable Java tests                                                                                            | [invoker build plugin](../tools/tools.maven.invoker)               |
|           | install, package          | `-DdisablePythonTests=<bool>`        | Invoker builds: enables/disables Python "compilation", i.e., syntax check                                                                     | [invoker build plugin](../tools/tools.maven.invoker)               |
|           | install, package          | `-DdisablePython=<bool>`             | Invoker builds: enables/disables Python building, i.e., "compilation" and tests                                                               | [invoker build plugin](../tools/tools.maven.invoker)               |
|           | install, package          | `-DenableJavadoc=<bool>`             | Invoker builds: enables/disables Java documentation building                                                                                  | [invoker build plugin](../tools/tools.maven.invoker)               |

## Determining the update behavior
<a name="mvn-update-behavior"></a>

Go to your local Maven repository (usually in your home directory in the folder `.m2`) and modify the settings file there. If there is no settings file, you can create a new one as shown below (for always updating snapshots on the SSE maven repositories). To become effective, it is important that the repository ids are stated as in the platform dependencies pom.

  ```xml
    <settings xmlns=http://maven.apache.org/SETTINGS/1.1.0
     xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance
     xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0
      http://maven.apache.org/xsd/settings-1.1.0.xsd">
    
      <activeProfiles>
        <activeProfile>github</activeProfile>
      </activeProfiles>
    
      <profiles>
        <profile>
          <id>github</id>
          <repositories>       
           <repository>
             <id>SSE-mvn</id>
             <name>SSE</name>
             <url>https://projects.sse.uni-hildesheim.de/qm/maven/</url>
             <layout>default</layout>
             <releases>
                <enabled>true</enabled>
             </releases>
             <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
             </snapshots>
           </repository>
          <repositories>
          <pluginRepositories>
            <pluginRepository>
             <id>SSE-mvn-plugins</id>
             <name>SSE Maven</name>
             <url>https://projects.sse.uni-hildesheim.de/qm/maven</url>
             <releases>
                <enabled>true</enabled>
             </releases>
             <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
             </snapshots>
            </pluginRepository>
          </pluginRepositories>
        </profile>
       </profiles>
     </settings>
  ```

If there is already a repositories section, please add the contents for the `SSE-mvn` repository as shown above. 

For more information, please refer to the [Maven Settings Reference](https://maven.apache.org/settings.html).
