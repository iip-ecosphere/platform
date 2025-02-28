# oktoflow platform: Build commands

## Building all-in-one examples

As stated above, directly after obtaining this project, the app will not run and even show compile errors. This is due to the fact that we do not commit generated files, i.e., the build process must be executed once to get rid of these "errors".

Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install)).

For building, in the project folder of the example app:

  * Execute `mvn install` This will perform the broker-instantiation, the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
  * To prevent running the test cases, add `-DdisableJavaTests=true` or `-DdisablePythonTests=true`, respectively. For disabling Javadoc generation, add `-DenableJavadoc=false`.
  * The main build steps can be executed individually (see [Maven configuration plugin](../configuration/configuration.maven/README.md)), usally with explicit profile `-P EasyGen`.

As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place)

The build steps are  explained in more detail in the Platform Handbook.

## Updating the meta model of apps

The meta-model is not automatically updated. This allows the owner to declide when potential upgrades to own code shall be applied.

  * To update/upgrade the model, call `mvn -P EasyGen -U generate-sources -Dunpack.force=true`

## Platform

Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))

  * `mvn install`, for component updates `mvn -U install`
