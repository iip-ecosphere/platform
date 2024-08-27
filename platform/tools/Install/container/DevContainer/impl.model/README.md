# IIP-Ecosphere platform: Setup for an implementation project.

Project for the application model. After setting up an application model, an implementation template will be generated, which is the basis for implementing the application.

Typical steps:
  * `mvn -U generate-sources` to obtain the meta model
  * Add a basic model to `src/test/easy`, e.g., from examples or from the tests of the configuration component. Let's call the file and the IVML project "Test". If you try multiple "platforms", please also adjust `sharedArtifact`.
  * In case your basic model name is not "Test", then change the "Test" name in "< argument>Test</ argument>" in pom.xml file to your basic model name - two places (generateAppsNoDeps, generateApps).
  * Instantiate the application without integration `mvn exec:java@generateAppsNoDeps` (replace "Test" by the name of your model). This will also generate the implementation template in `gen/templates`. 
  * Import the contained project into the development Eclipse by copying it so that a re-generation of the template does not override your code.
  * There may be template Java/Python classes/templates depending on the application setup. You may modify them. In particular service tests try to load data files, for which the generation also places templates into the resource folders. Of course, these files shall be filled with relevant data (or cleaned up).
  * When the implementation is completed and tested, execute `mvn exec:java@generateApps`
  * Execute the resulting application directly with Java (local execution) or in the platform. Please note that the platform UI allows for generating/downloading implementation templates. Implemented applications (based on the templates) may be uploaded for application generation and application execution. A more complete setup allows for deploying the application into the platform Maven repository from where the platform can go on with creating the application.
