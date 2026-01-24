# oktoflow platform: App debugging

This document aims at collecting potential issues and resolutions when developing oktoflow apps. Although the first impression might be that the platform is not doing it's job, in particular also the service code parts contributed by you might be the culprit.

* Is your code correct?
  * Syntax checking and (for Java compiability) are covered by the build process.
  * Does it work? Take over the unit test template (test code and mock input resource file) and adjust the mock input to your case at hands. Adjust the (generated generic) test assertions so that they ensure basic functionality.
  * The unit tests rely on your plain code, but does not consider the packaging of your resources and the unpacking. For this purpose, use the service-level tests which emulate the execution as the app/platform does without running the full application.
  * Are your service resources relative to the service (Java packaged: `src/main/resources`, Java unpackaged: `resources/software`, Python: in same or sub-folder of service)?
* Make sure that your code parts comply with the most recent version of the application model.
  * After model changes and re-generation, perform a diff and identify structural differences, e.g., caused as consequence of changing the `RecordType`s of your application.
  * Are the generated interfaces and types as recent as your application model? The build process cares for that, but not if you for some reason copy these files into your code folders (which be outdated, shadow the generated code etc.).
  * Also consider the unit test files, the mock data source files and the connector connectivity tests as they may also change with application changes.
* Read log output, identify point where the service in question is loaded. Temporary logging in the service code may help (see also below the logging abilities of the platform).
  * Can the service be found? Is the service really loaded by oktoflow/the app? Are there errors/issues to fix?
  * Does it fail in service/constructor initialization? Besides exceptions, no logging output from your service clould be an indication here.
  * Does it process data? Logging output from your service before processing (initialization and start function) but not on processing could be an indication here.
  * Does it yet process data? If your service needs initialization, e.g., AI model loading, called in the wrong place may significantly delay the processing in your service, in the extreme case the mock data is already ingested until your service is ready. 
    * Do not call longer-running initialization code in the service constructor rather than the service `start` method.
    * Delay the first mocking data item until your service is ready (`$period` and/or `$repeate` entries in mocking data, see explanation in template). Background: If an app runs standalone, startup might be rather lazy. If the platform starts the app, it reverses the data flow and starts service after service with data sources last to avoid data loss. 
* If the applications seems "to loose data", enable tracing on selected/all connectors/services, e.g., in IVML `traceSent=TraceKind::SYSOUT` (see [service configuration concepts](concepts/services.MD)). 
  * For performance reason, tracing is disabled by default and shall be disabled after debugging is done.
  * Tracing also works in distributed manner, i.e., via a central collector.