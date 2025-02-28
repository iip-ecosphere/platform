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
  
Directly after obtaining this project, the application will not run and even show compile errors. This is an all-in-one example. For building/updating the meta-model, please refer to the [building instructions for all-in-one examples](../../tools/Install).
