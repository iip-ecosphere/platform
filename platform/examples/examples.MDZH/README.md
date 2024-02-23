# oktoflow platform examples: MDZH PCF calculation

Implements the internal processing of the MDZH (Mittelstand Digital Zentrum Hannover) PCF (Product Carbon Footprint) demonstrator. This encompasses a serial EAN code scanner, an external cloud-based AAS server, an engraving laser and a Siemens Sentron energy measurement device. The EAN code scanner identifies the idShort of the product plan of a pre-configured metal pen to be "produced" (manual composition, pick-by-light) and engraved by the laser. The laser production control happens via OPC UA based on the pre-configured program read from the cloud-based AAS server. While engraving, the energy consumption is (externally) recorded and reported into the cloud-based AAS server as timeseries. The PCF calculation service of the APP receives production start/end time and energy consumption via AAS/OPC UA and updates the PCF values in the PCF AAS.

The application consists of the following (micro)-services, which are composed in the configuration model and integrated in a model-based fashion through the platform/application instantiation:
  * A serial connector for the EAN code scanner.
  * An AAS connector for the production plan.
  * An OPC UA connector to steer the laser and to receive start/stop time.
  * An AAS connector for the PCF submodel.
  * A Java-based PCL calculation.

An explaining overview slide is available [here](docs/Examples_MDZH.pdf).

This example consists of several pieces:
  * An IVML configuration for the application in `src/main/easy`.
  * An implementation of the Java services used in the application in `src/main/java`
  * Two Maven profiles, one for obtaining the configuration meta-model / performing the instantiation as well as one for the application itself (executes the assembly descriptor). 
      
Regarding Python code, we make the assumption that the module of the Python Service Environment `iip` and the generated modules `datatypes`, `interfaces`, `serializers` and `services` are visible to Python within the same folder (physically or virtually).
  
As stated above, directly after obtaining this project, the application will not run and even show compile errors. This is due to the fact that generated parts and even the configuration meta model are missing. We will add them through the following steps (as explained in more details in the Platform Handbook). As usual with Maven projects, you may add the argument `-U` to update snapshots if parts are already in place):

  * Ensure that the Maven platformDependencies are installed (see [install](../../tools/Install))
  * Execute `mvn -U install` This will perform the broker-instantiation, the interface generation, the code compilation and packaging as well as the final application packaging. Build steps are only executed if the configuration model changes or generate code is not already existing. If a `resources.ipr` folder is present, it will take precendence over the `resources` folder. 
  * To update/upgrade the model, call `mvn -P EasyGen -U generate-sources -Dunpack.force=true`.

## Required Updates

See [Platform configuration](../../configuration/configuration) for details on the state of the generation and the required version of EASy-Producer (at least from the day of the last commit of this example). If the configuration meta model shall be updated, add `-Dunpack.force=true`.
