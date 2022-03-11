# IIP-Ecosphere platform examples: VDW OPC-UA

Utilizing the VDW OPC-UA server through the respective platform connector. This example currently contains two parts:

* A hand-crafted connector. Run `de.iip_ecosphere.platform.examples.vdw.App` or Run `test.de.iip_ecosphere.platform.examples.vdw.OpcUaConnectorTest`.
* A generated connector based on a preliminary model. Run `de.iip_ecosphere.platform.examples.vdw.OpcUaModelTest` 
  as JUnit test to execute the model instantiation/code generation. Generation may require in Eclipse a Maven update of the project (including Snapshots). Run then `de.iip_ecosphere.platform.examples.vdw.OpcUaModelTest` 
  as Java program to execute the connector.

For all executions, the VDW/UMATI OPC UA server must be accessible via Internet. See [Platform configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration) for current limitations/state of the generation.

