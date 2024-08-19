# oktoflow platform examples: MODBUS/TCP

Demonstrating and utilizing the MODBUS/TCP connector. If compile errors are shown, run the instantiation/connector generation as described below (generated classes intentionally not in github, generated connector libraries are intentionally not deployed to Maven). We utilize a software server with a fixed layout for testing. Since version 0.7.1 the generated code is in `target/gen` rather than `gen`.

This example currently contains two parts:

* A generated connector:  

    * Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))
    
    * Execute `mvn -U install` This will perform the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
    * To update/upgrade the model, call `mvn -P EasyGen -U generate-sources -Dunpack.force=true`. Build shortcuts are described in [configuration.maven](../../configuration/configuration.maven/README.md).
  
    * Run the generated connector with `mvn -P App exec:java@generatedConnector`
    * The generated connector writes `modbusTest.txt` containing measurements provided through a micrometer timing probe.
* A hand-crafted connector for comparison: `mvn -P App exec:java@manualConnector`

Shortcuts for Eclipse: 
  * Run `test.de.iip_ecosphere.platform.examples.modbusTcp.ModbusModelTest` as JUnit test to execute the model instantiation/code generation. Generation may require in Eclipse a Maven update of the project (including Snapshots). 
  * Run then `test.de.iip_ecosphere.platform.examples.modbusTcp.ModbusConnectorTest` 
  as Java program to execute the generated connector.

As usual, ** do not modify generated code **.

An explaining overview slide is available [here](docs/Examples_MODBUS.pdf)

## Required Updates

See [Platform configuration](../../configuration/configuration) for details on the state of the generation and the required version of EASy-Producer.