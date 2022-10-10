# IIP-Ecosphere platform examples: VDW OPC-UA

Utilizing the VDW OPC-UA server through the respective platform connector. If compile errors are shown, run the instantiation/connector generation as described below (generated classes intentionally not in github, generated connector libraries are intentionally not deployed to Maven).

This example currently contains two parts:

* A generated connector based on a preliminary model.  

    * Ensure that the Maven platformDependencies are installed (see [install](https://github.com/iip-ecosphere/platform/tree/main/platform/tools/Install))
    * Obtain the platform configuration meta-model, which is intentionally not included here: `mvn -P EasyGen generate-sources` (use `-U` to update it if it is already in place) 
    * Instantiate the pseudo-application using the OPC UA connector: `mvn -P EasyGen exec:java@generateApps`
    * If you try the example from within Eclipse, we would now need here now a Maven project refresh.
    * Compile the project with `mvn -P App compile`
    * Run the generated connector with `mvn -P App exec:java@generatedConnector`
    * The generated connector writes `opcTest.txt` containing measurements provided through a micrometer timing probe.
* A hand-crafted connector for comparison: `mvn -P App exec:java@manualConnector`

Shortcuts for Eclipse: 
  * Run `de.iip_ecosphere.platform.examples.vdw.OpcUaModelTest` as JUnit test to execute the model instantiation/code generation. Generation may require in Eclipse a Maven update of the project (including Snapshots). 
  * Run then `de.iip_ecosphere.platform.examples.vdw.OpcUaModelTest` 
  as Java program to execute the generated connector.

For all executions, the VDW/UMATI OPC UA server must be accessible via Internet. And, as usual, ** do not modify generated code **.

## Required Updates

See [Platform configuration](https://github.com/iip-ecosphere/platform/tree/main/platform/configuration/configuration) for details on the state of the generation and the required version of EASy-Producer.

## Desirable

Explaining slides, may be a video.

An explaining overview slide is available [here](https://github.com/iip-ecosphere/platform/tree/main/platform/examples/examples.vdw/docs/Examples_VDW.pdf)