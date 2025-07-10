# oktoflow platform: Build commands

## Building all-in-one examples

As stated above, directly after obtaining this project, the app will not run and even show compile errors. This is due to the fact that we do not commit generated files, i.e., the build process must be executed once to get rid of these "errors".

Ensure that the Maven platformDependencies are installed (see [install](../tools/Install), for the examples also the examples-only variant will do).

For building, in the project folder of the example app:

  * Execute `mvn install` This will perform the broker-instantiation, the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
  * To prevent running the test cases, add `-DdisableJavaTests=true` or `-DdisablePythonTests=true`, respectively. For disabling Javadoc generation, add `-DenableJavadoc=false`.
  * The main build steps can be executed individually (see [Maven configuration plugin](../configuration/configuration.maven/README.md)), usally with explicit profile `-P EasyGen`.

As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place)

The build steps are  explained in more detail in the Platform Handbook.

## Building all-in-one examples manually

The following build steps are automatically executed by an all-in-one example build process.

* Update the meta-model if needed or forced (see below)
* Generate service interfaces, serializers and data classes `mvn -P EasyGen process-sources` (see [configuration maven plugin](../configuration/configuration.maven) for the detailed build steps associated with `process-sources`)
* Compile and package the application `mvn -P App install` (see [configuration maven plugin](../configuration/configuration.maven) for the detailed build steps associated with `package`)
* Integrate the application `mvn -P EasyGen package`

Sometimes, it makes sense to execute individual steps separately, e.g., `mvn -P EasyGen configuration:generateAppsNoDeps` for a run building all integration code files leaving out all dependencies (no deps). Although this application may not run, the build process creates the application templates in this step.

## Updating the meta-model of apps

The meta-model is not automatically updated. This allows the owner to declide when potential upgrades to own code shall be applied. This may be needed when platform interface changes occur (seldom), i.e., suddely compile errors appear in your application, which are compensated by a matching configuration meta model.

  * To update/upgrade the model, call `mvn -P EasyGen -U generate-sources -Dunpack.force=true`

## Platform

Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))

  * `mvn install`, for component updates `mvn -U install`

## Failures

* When building an example, Maven complains about missing platformDependencies, in particular for a SNAPSHOT version. We did not repeate the repository declaration in all examples, which, for a fresh installation (also with a new version) means that maven does not know from where to download the basic platform dependencies. Please install the platform dependencies first as stated above.
* After successfully installing the platform/platform dependencies, Maven complains again. This may be due to the fact that build processes try to avoid repeating steps, i.e., from the last try there are leftovers that disturb the build process. In your project folder, delete the `target/gen` folder or try `mvn clean`.