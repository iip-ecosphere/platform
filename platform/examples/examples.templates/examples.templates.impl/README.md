# Service implementation project for impl.TestTestApp.

Contains a template project for service implementations for impl.TestTestApp. The configuration model is assumed to be 
managed by the platform. Although generated, this code is intended to be imported into your Eclipse IDE and modified. 
Please do not link the code into your IDE as it will be re-generated and will override your changes. Please feel free
to delete class templates or POM settings if needed, e.g., if you want to realize individual re-usable services in 
separate projects.

The project is set up for JavaSE 8 and without (platform-defined) checkstyle as this project contains your code.
If python code is included, a basic assembly descriptor is created and hooked into the POM. The files to include via 
assembly descriptors may need adjustment.

Typical build steps:
 - Configure your application and generate application interfaces (done).
 - Implement your services
 - `mvn -P App install`.
 - Test your services / application. Helper command line executions are hooked into the POM.
 - Generate application integration code.
 - Create a deployment plan and upload it into the artifacts folder of the platform.
 - Run your application.
 
 For testing the connectors/services individually or the application locally, please start the configured broker first. 
 Following maven commands are generated, but may require individual customization, e.g., through environment properties.
 Also input files (see `src/test/resources`) require renaming and adjustments.
 - `mvn -P App exec:java@app` runs the full application with all services locally without the platform. If you want to 
 implement individual services, this execution renders useless.
 - `mvn -P App exec:java@transportLogger` runs the transport logger with default setup in the context of this application.
  - `mvn -P App exec:java@PyServiceTest` executes the service/connector test for `PyService` - `mvn -P App exec:java@SinkTest` executes the service/connector test for `Sink` - `mvn -P App exec:java@SourceTest` executes the service/connector test for `Source`
- `mvn -P App exec:java@py-test` tests for python syntax.
 
