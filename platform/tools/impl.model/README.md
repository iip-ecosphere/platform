# IIP-Ecosphere platform: Setup for an implementation project.

Project for the application model. There is a second project in the same folder called `impl.impl` that is also required.

##Typical steps
  * `mvn -U -P EasyGen generate-sources` to obtain the meta model
  * Add a basic model to `src/test/easy`, e.g., from examples or from the tests of the configuration component. Let's call the file and the IVML project "Test". If you try multiple "platforms", please also adjust `sharedArtifact`.
  * In case your basic model name is not "Test", then change the "Test" name in "< argument>Test</ argument>" in pom.xml file to your basic model name - two places (generateAppsNoDeps, generateApps).
  * Instantiate the application without integration `mvn -P EasyGen configuration:generateAppsNoDeps`, in particular to obtain the generate interfaces and the implementation template (then as ZIP in `gen/impl/templates).
  * Import the implementation project into Eclipse.
  * Continue there.

##Optional steps
  * If needed, use `mvn -U -P EasyGen generate-sources -Dunpack.force=true` to update the meta model
