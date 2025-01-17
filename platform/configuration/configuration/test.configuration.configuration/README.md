# IIP-Ecosphere platform test artifacts: Implementation classes for generated test apps

Test artifact containing simple implementation classes for test steams generated by the configuration layer, i.e., the [platform configuration component](../README.md).

This artifact contains the implementation artifacts for three cases, SimpleMesh, SimpleMesh3 and KodexMesh. Classes for SimpleMesh3 are marked with 3 in their name. This is a rather unusual but convenient setup for CI testing. Usually, each app shall have its own project.

Please note that this project requires the classes generated from the SimpleMesh/SimpleMesh3/KodexMesh test configuration. In turn, the SimpleMesh/SimpleMesh3 production processes use this artifact as service implementation. For "bootstrapping", configuration.configuration builds this artifact in CI (and could also be stored there as sub-project). However, to allow for standalone Maven builds, this artifact is still a standalone project, which requires generateInterfaces or generateAppsNoDeps of configuration.configuration before.

This project also contains example Python service code for KodexMesh. The required generated/framework code is obtained and unpacked via Maven. Finally only the implemented services are packaged again for deployment into integration.
