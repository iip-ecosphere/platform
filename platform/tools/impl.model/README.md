# oktoflow platform: Setup for an implementation project.

Project for the application model. It will generate a template project in ``target/gen/impl/templates`` that you can copy-import into Eclipse to implement the services. Since version 0.7.1, this project contains a simple basic model (following the basic structures of `cfg` in `src/main/easy` in the project `configuration.configuration`).

##Typical steps
  * `mvn -U generate-sources` to obtain the meta model
  * Update the basic model in `src/main/easy`. If you try multiple "platforms", please adjust `sharedArtifact` in `TechnicalSetup.ivml`.
  * Instantiate the application without integration `mvn configuration:generateAppsNoDeps`, in particular to obtain the generate interfaces and the implementation template (then as ZIP in ``gen/impl/templates``).
  * Import the implementation project into Eclipse.
  * Continue there.

##Optional steps
  * If needed, use `mvn -U -P EasyGen generate-sources -Dunpack.force=true` to update the meta model
