# oktoflow platform: App debugging

This document aims at collecting topics and techniques on developing oktoflow apps. 

## Typical Issues and Resolutions

Although a first impression might be that oktoflow is not doing it's job, in particular also the service code parts contributed by you might be the culprit. Below, we list questions, properties and topics that shall help thinking out-of-the-box when app execution issues occur.

* Is your code correct?
  * Syntax checking and (for Java compiability) are covered by the build process.
  * Does it work? Take over the unit test template (test code and mock input resource file) and adjust the mock input to your case at hands. Adjust the (generated generic) test assertions so that they ensure basic functionality.
  * The unit tests rely on your plain code, but does not consider the packaging of your resources and the unpacking. For this purpose, use the service-level tests which emulate the execution as the app/platform does without running the full application.
  * Are your service resources relative to the service (Java packaged: `src/main/resources`, Java unpackaged: `resources/software`, Python: in same or sub-folder of service)?
* Do your code parts comply with the most recent version of the application model.
  * After model changes and re-generation, perform a diff and identify structural differences, e.g., caused as consequence of changing the `RecordType`s of your application.
  * Are the generated interfaces and types as recent as your application model? The build process cares for that, but not if you for some reason copy these files into your code folders (which be outdated, shadow the generated code etc.).
  * Also consider the unit test files, the mock data source files and the connector connectivity tests as they may also change with application changes.
* What does the log say? Identify point where the service in question is loaded. Temporary logging in the service code may help (see also below the logging abilities of the platform).
  * Can the service be found? Is the service really loaded by oktoflow/the app? Are there errors/issues to fix?
  * Does it fail in service/constructor initialization? Besides exceptions, no logging output from your service could be an indication here.
  * Does it process data? Logging output from your service before processing (initialization and start function) but not on processing could be an indication here.
  * Does it yet process data? If your service needs initialization, e.g., AI model loading, called in the wrong place may significantly delay the processing in your service, in the extreme case the mock data is already ingested until your service is ready. 
    * Do not call longer-running initialization code in the service constructor rather than the service `start` method.
    * Delay the first mocking data item until your service is ready (`$period` and/or `$repeat` entries in mocking data, see explanation in template). Background: If an app runs standalone, startup might be rather lazy, i.e., in sequence: sinks, transformers and probe services, sources but without considering input/output dependencies. If the platform starts the app, it reverses the data flow and starts service after service with data sources last to avoid data loss. 
* If the applications seems "to loose data", enable tracing on selected/all connectors/services, e.g., in IVML `traceSent=TraceKind::SYSOUT` (see [service configuration concepts](concepts/services.MD)). 
  * Is the communication broker running?
  * Does the transport connector have a connection (see application log)?
  * Is there a transport authentication issue? 
    * Did the app load its identity store (see application log) and if yes, is it the intended one (validate the name of the identity store given in the log)? 
    * Are the entries in the application store correct? Did you accidentally override default entries that now prevent transport connections? 
    * If no identity store is loaded, are you running an app without service implementation, e.g., instantiated from a template or consisting only of connectors (then you may wish to specify an [identity store in IVML](../concepts/app.MD)).
  * For performance reason, tracing is disabled by default and shall be disabled after debugging is done.
  * Tracing also works in distributed manner, i.e., via a central collector.
  
Apps can be tested in automated/distributed fashion using the [Platform Evaluation and Testing Environment (PETE)](../../tests/test.environment).

## Online Update of Services

When a service issue shall be identified, e.g., by adding debug outputs, or a resolution is found, the traditional approach is to stop the communication broker, to re-build the application, to re-start the broker and to run the application again. As this involves a complete re-generation, re-compilation and re-packaging of the application, it might be rather tedious. Since version 0.8.1 of oktoflow we aim at speeding up this cycle by more lightweight build and runtime service update processes.7

* **Stopping and re-startnig the communication broker is not alyways needed:** An app build process only re-builds the broker if the meta-model has been changed (and then a running-broker may prevent overriding it's packaged binaries). Thus, am application re-build, you can keep the broker running. Moreover, if the broker protocol/setup is the same, you may also run the broker of another app or install a global broker (e.g., mosquitto for MQTT) and use that insinde to avoid keeping track of a broker.
* Use **artifact-update shortcut build processes** (additional profile `-P Art`, see generated scripts `updateArtifacts.sh` and `updateArtifacts.bat`). These build process variants bypass interface/app code generation and just focus on updating the Java/Python/resource artifacts for app services. If enabled in the POM of the service implementation project/all-in-one-example, the app is packaged afterwards, a prerequisite that the changed artifacts become available. Basically, this variant still requires re-starting the application. 
* In testing/evaulation: Rely on **runtime service updates** (in combination with artifact-update shortcut build processes), i.e., keep broker and app running, just run the artifact-update shortcut build process and let the app update its services. Thereby, mocked connectors are re-set so that mocked data ingestion starts from the beginning of the respective data files.
  * Requires that all involved (data ingesting) services correctly take the service state into account, i.e., do not ingest data when they are not in `RUNNING` state. Not considering service states may confuse the service execution, which may believe that ingested and transported data is not processed further so that individual data paths may be disabled.
  * If required, services shall store relevant parts of their state on `stop`/reload the last state in `start`. Java services may override `Service.transferState(Service)` but shall call the respective basic implementation.
  * Python: Synchronous and asynchronous Python services with websocket integration seem to "survive" a service update. Python services are independend of oktoflow platform plugins. Please take into account that unpacked service files may be overridden with updated files, i.e., no files are deleted and relative files stored by a service, e.g., an AI model shall go into files that are not part of the initial service installation as otherwise the update may override them. 
  * Java: Runtime update for Java services is only supported when 
    * oktoflow platform plugins are enabled
    * service descriptors are present in your services (see generated service classes in the app implementation template).
    * JSL descriptor file is in place (see `src/main/resources/META-INF/services/de.iip_ecosphere.platform.support.plugins.PluginDescriptor` in the app implementation template)
    * the classpath is written into a file called according to the groupId/artifactId of the implementation project (see `POM.xml` in the app implementation template).
 
Please note that due to the currently used version of Spring Cloud Stream, sudden disconnects among services upon runtime updates may occur, in particular if the `ServiceState` is not handled/transferred correctly.