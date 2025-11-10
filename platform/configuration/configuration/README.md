# oktoflow platform: Configuration Component and Configuration Model

We aim at an encompassing and consistent configuration of the whole platform in order to enable model-driven platform instantiation, i.e., to include relevant parts and to exclude unwanted parts. This is the point in the platform where optional components such as transport protocols, connectors, service execution managers, container managers, service chains and applications etc. are combined, configured, validated and ultimately turned into instantiated code and installable artifacts. 

This component realizes the integration of the configuration plugin and (potentially various) configuration models. In the future, generic AAS abilities from `configuration.easy` may be migrated here.

The setup also may contain a `serviceArtifactStorage`and a `containerImageStorage` PackageStorageSetup specification from [deviceMgt](../../resources/deviceMgt/README.md), which are currently not used.

## Tests

Tests are now in configuration.easy which relies on the files in src/main/easy, src/test/easy and generates into target/gen.

