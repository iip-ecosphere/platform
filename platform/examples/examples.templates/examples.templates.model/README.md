# IIP-Ecosphere platform: Setup for an implementation project.

Project for the application model. There is a second project in the same folder called `impl.impl` that is also required.

Typical steps:
  * `mvn -U generate-sources` to obtain the meta model
  * Add a basic model to `src/test/easy`, e.g., from examples or from the tests of the configuration component. Let's call the file and the IVML project "Test". If you try multiple "platforms"
  * please also adjust `sharedArtifact`.
  * In case your basic model name is not "Test", then change the "Test" name in "< argument>Test</ argument>" in pom.xml file to your basic model name - two places (generateAppsNoDeps, generateApps).
  * Instantiate the application without integration `mvn install` (replace "Test" by the name of your model)
  * you will have a template project in the `gen\py\...` directory
  * import the .zip file into your workspace as a project
  * run `mvn install` once to generate the needed classes in traget
  * implement your service logic in the respective classes
  * files needed by python need to go into the same directory as the python service
  * run `mvn install` again to install the finished services.
  * Continue here... add the Maven artifact name of `impl.impl` as artifact for your services in your IVML file.
  * run `mvn test` in the `impl`project
  * run `mvn install` in the model project
  * Execute the result in the platform


Deprecated Typical steps:
  * `mvn -U generate-sources` to obtain the meta model
  * Add a basic model to `src/test/easy`, e.g., from examples or from the tests of the configuration component. Let's call the file and the IVML project "Test". If you try multiple "platforms", please also adjust `sharedArtifact`.
  * In case your basic model name is not "Test", then change the "Test" name in "< argument>Test</ argument>" in pom.xml file to your basic model name - two places (generateAppsNoDeps, generateApps).
  * Instantiate the application without integration `mvn exec:java@generateAppsNoDeps` (replace "Test" by the name of your model)
  * Go in in the associated project `impl.impl`.
  * Continue here... add the Maven artifact name of `impl.impl` as artifact for your services in your IVML file.
  * `mvn exec:java@generateApps`
  * Execute the result in the platform
