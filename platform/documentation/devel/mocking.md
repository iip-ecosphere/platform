# oktoflow platform: Mocking

While user/application-specific data sources can be programmed in a way that they provide data for testing, oktoflow connectors are different. Connectors aim at bridging external data sources into oktoflow apps and support data flowing back. If such an external data source, e.g., a production machine, a sensor, etc. is not available in your test setup (no VPN connection, machine shall not be affected, ...) then it would be good if we could virtually replace a connector with an instance that can ingest test data, i.e., we would like to **mock** a connector.

Mocking a connector requires two steps:
* Provide data that the connector can read for mocking.
* Switch the connector in the application model to mocking and re-build the application. 

Please note that connector mocking is inserted by code generation so avoid unnecessary code on connector invocations, i.e., for enabling/disabling connector mocking, the respective app must be re-build.

## Mocking data

Mocking data is provided in terms of simplified JSON files. As connectors differ in the data types they handle, each data file has a specific format. To cope with these differences, the generated application code templates contains example mocking data files for each used connector. You can find them in the app code template in the folder `resources/software`. The mocking data files are named according to the connectors and the file ending is `.json.tmpl` to indicate that these are template files and need adjustment, in particular data entries. Please note:

* To become effective, you must copy a template mocking data file into `resources/software` of your example/service implementation project. Thereby, rename the file to `.json`, i.e., remove the secondary file name extension `.tmpl`. Note: As these files shall be directly accessible within the packaged application and not be packaged into service code artifacts, the path is `resources/software`, in particular neither `src/main/resources` nor `src/test/resources`!
* A mocking template file starts with explanatory text specific for the respective connector. Please read and then remove that text before re-building your application.
* Each line contains a full data point to be ingested at one point in time. For all mandatory fields of the data types used by the connector, there is an equally named JSON attribute. Optional fiels may but must not be given.
* If a connector defines a `samplingPeriod`, this is used as time period/delay between two subsequent data points. Channel-connectors ingest data as it arrives without sampling; then the data points shall take control over the ingestion speed through meta attributes as described next.  
* Besides the pure data, there are meta-attributes like `$period` or `$repeats`. `$period` defines the waiting time between this and the next data point to be ingested (and for this data entry takes precedence over `samplingPeriod`). Akin, `$repeats` defines how often (including "endless") a data point shall be ingested repeatedly taking the actual ingestion period. 

Please note that there are also data files for service testing. They look similar, but as services may operate with different data types, require testing data to be assigned to an input type, i.e., data point specification becomes nested. Further, when you change your application model, in particular the data types, the mocking data files must be modified accordingly. In particular, if mandatory data entries are missing, data ingestion will fail. For this purpose, please consult the re-generated application code template. 

## Enabling connector mocking

In your application model, change the `mock` flag for the connectors that you would like to mock to `true`. If you do that directly in IVML, it might be needed to add a line like `mock=true,` to the connector definition, as by default mocking is switched off. On the management UI, search for the mock field, set it to `true`and store the changed connector.

Re-build then your application, either using the command line build commands/scripts or the application integration of the management UI. Re-run your application and observe the output whether when services are started, the connector takes up the data. Please note that the connector will ingest only the specified data and turn to idle when there is no more data.

## Disabling connector mocking

Akin to enabling connector mocking, change the `mock` flag to false, re-build and re-start your application.

## Connectors and runtime service replacement

During development, the re-build of an application for changing application-specific service code can take some time and, thus, be tedios. Since version 0.8.1 (in improvement in 0.8.2), oktoflow provides a build shortcut for updating service artifacts only. A running oktoflow app is notified about changes and re-loads running services on changes. As part of this update process, connector data ingestions are first stopped to drain data flow paths and after the updates data ingestion is re-started. Re-starting data ingestion also resets the connector mocking, i.e., after service updates, mocked connectors starts ingesting data from the beginning of a data file.   
