# oktoflow platform examples: VDW OPC-UA

Utilizing the VDW OPC-UA server through the respective platform connector. If compile errors are shown, run the instantiation/connector generation as described below (generated classes intentionally not in github, generated connector libraries are intentionally not deployed to Maven). Since version 0.7.1 the generated code is in `target/gen` rather than `gen`.

This example currently contains two parts:

* A generated connector based on a preliminary model.  

    * Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))
    
    * Execute `mvn -U install` This will perform the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
    * To update/upgrade the model, call `mvn -P EasyGen -U generate-sources -Dunpack.force=true`
  
    * Run the generated connector with `mvn -P App exec:java@generatedConnector`
    * The generated connector writes `opcTest.txt` containing measurements provided through a micrometer timing probe.
    * By default, full test execution is disabled as a specifically prepared docker container is required. If this docker container shall be built/executed, set ``-Dtest.docker.skip=true -Dtest.exec.args=--all``
* A hand-crafted connector for comparison: `mvn -P App exec:java@manualConnector`

Shortcuts for Eclipse: 
  * Run `test.de.iip_ecosphere.platform.examples.vdw.OpcUaModelTest` as JUnit test to execute the model instantiation/code generation. Generation may require in Eclipse a Maven update of the project (including Snapshots). 
  * Run then `test.de.iip_ecosphere.platform.examples.vdw.OpcUaConnectorTest` 
  as Java program to execute the generated connector.

For all executions, the VDW/UMATI OPC UA server must be accessible via Internet. And, as usual, ** do not modify generated code **.

An explaining overview slide is available [here](docs/Examples_VDW.pdf)

## Required Updates

See [Platform configuration](../../configuration/configuration) for details on the state of the generation and the required version of EASy-Producer.