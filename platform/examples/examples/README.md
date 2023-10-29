# IIP-Ecosphere platform: Examples support package

Helpful basic classes for Spring-based local testing of applications and examples.

Executes some regression tests of [configuration.configuration](../../configuration/configuration):

* Tests that perform the instantiation of a full platform including [management UI](../../managmementUI) and [platform services](../../platform), which are built in the Jenkins built flow between configuration.configuration and examples: ``IvmlSerializerConfig1Tests``, ``ContainerTests``, ``ContainerLxcTests``.
* Tests that rely on Spring-based execution, which is optional and intentionally not part of the dependencies of configuration.configuration: ``SimpleMesh``, ``SimpleMesh3``, ``RoutingTest``.

