# IIP-Ecosphere platform: Releases

### At the horizon (Fall 2023):

Planned:
* Measurements in testing environment [#126](https://github.com/iip-ecosphere/platform/issues/126)
* RBAC on AAS [#122](https://github.com/iip-ecosphere/platform/issues/122)
* Component upgrades: Spring Cloud Stream [#57](https://github.com/iip-ecosphere/platform/issues/57)
* Integrated container management for Kubernetes [#39](https://github.com/iip-ecosphere/platform/issues/39)
* Filtering of monitoring telegrams [#75](https://github.com/iip-ecosphere/platform/issues/75)

### HM'23: At the fair (Spring 2023):

Planned:
* Enable tolerant switching: [#26](https://github.com/iip-ecosphere/platform/issues/26)
* Tracing in Python Service environment: [#88](https://github.com/iip-ecosphere/platform/issues/88)
* CLI multi-device deployment problem: [#113](https://github.com/iip-ecosphere/platform/issues/113)
* KODEX: dynamic port assignment: [#14](https://github.com/iip-ecosphere/platform/issues/14), documentation: [#6](https://github.com/iip-ecosphere/platform/issues/6)

In progress:
* [#127](https://github.com/iip-ecosphere/platform/issues/127): Integration of federated learning 
* [#70](https://github.com/iip-ecosphere/platform/issues/70): UI progress reporting 
* [#117](https://github.com/iip-ecosphere/platform/issues/117): Multiple service managers per device
* [#146](https://github.com/iip-ecosphere/platform/issues/146): UI editors for configuration tables
* [#145](https://github.com/iip-ecosphere/platform/issues/145): UI table for nameplate configurations
* [#144](https://github.com/iip-ecosphere/platform/issues/144): Deployment plans and application instances
* [#143](https://github.com/iip-ecosphere/platform/issues/143): Container testing mode(s)
* [#142](https://github.com/iip-ecosphere/platform/issues/142): AAS resilience for ECS-Runtime/Service Manager
* [#141](https://github.com/iip-ecosphere/platform/issues/141): Fix AppAAS performance/resource consumption issue
* [#115](https://github.com/iip-ecosphere/platform/issues/115): Cleanup, e.g., old build processes

New features:
* [#127](https://github.com/iip-ecosphere/platform/issues/127): Integration of Flower-based federated learning through generated templates
* [#100](https://github.com/iip-ecosphere/platform/issues/100): Allowing to start/stop multiple app instances (only via deployment plans)
* [#59](https://github.com/iip-ecosphere/platform/issues/59): Basic version of distributed testing/evaluation environment for platform and apps (PETE)
* [#90](https://github.com/iip-ecosphere/platform/issues/90): UI display application service meshes
* [#62](https://github.com/iip-ecosphere/platform/issues/62): Platform component heartbeat and AAS cleanup
* [#107](https://github.com/iip-ecosphere/platform/issues/107): Platform installation scripts
* [#56](https://github.com/iip-ecosphere/platform/issues/56): Network-based integration protocol for Python service environment (websockets)
* Generic time series data aggregator in data functions
* Application intercommunication support (`AppIntercom`)
* [#132](https://github.com/iip-ecosphere/platform/issues/132): Bitmotec system monitoring integration
* [#118](https://github.com/iip-ecosphere/platform/issues/118): Service development video tutorial

Improvements:
* [#128](https://github.com/iip-ecosphere/platform/issues/128): Refactor model to move RecordType::path to connector. **May require an upgrade of configuration models using connectors.**
* [#129](https://github.com/iip-ecosphere/platform/issues/129): Read-only flag for RecordType field
* [#137](https://github.com/iip-ecosphere/platform/issues/132): MQTT authentication on transport/connectors
* [#86](https://github.com/iip-ecosphere/platform/issues/86): UI restructuring larger lists, unifying use and look-and-feel
* [#79](https://github.com/iip-ecosphere/platform/issues/79): Angular config.json loading problem (ok for now)
* [#140](https://github.com/iip-ecosphere/platform/issues/140): Generated containers log platform service startup/execution
* Configurable startup waiting time/timeout for AAS resources (ECS-Runtime, serviceManager)
* Connector improvements for OPC UA and MQTT, improved support for nested data types in transport/Python
* Connector mocking data with $period and $repeats, preparation for exact JSON field mapping
* Transport connector can now have a local serialization provider, e.g., for MQTT out.
* Trace-to-AAS service and (new) extensible Java service can utilize optional (output) transport connector, e.g., for northbound integration.
* More reliable artifact file modification watching, in particular for Linux.
* Build process fixes for examples.

### Release 0.5.0 (2023/03/01):

New features:
* Multi-channel connectors for MQTT in the connectors component and the code generation.
* [#10](https://github.com/iip-ecosphere/platform/issues/10), [#9](https://github.com/iip-ecosphere/platform/issues/9): Automatic creation of containers from the configuration model.
* [#51](https://github.com/iip-ecosphere/platform/issues/51): Device specific measurement plugin for Phoenix Contact AXC
* [#63](https://github.com/iip-ecosphere/platform/issues/63): Semantic Ids in the mangement UI (resources view)
* [#64](https://github.com/iip-ecosphere/platform/issues/64): Device specific measurement plugins controlled via configuration model 
* [#92](https://github.com/iip-ecosphere/platform/issues/92): Generate application implementation project templates
* [#93](https://github.com/iip-ecosphere/platform/issues/93): Connector caching mode
* [#89](https://github.com/iip-ecosphere/platform/issues/89): IVML configuration mapping into AAS
* [#96](https://github.com/iip-ecosphere/platform/issues/96): Write back IVML models into managed structure, complementing [#89](https://github.com/iip-ecosphere/platform/issues/89)
* [#74](https://github.com/iip-ecosphere/platform/issues/74): Specify keystore via identityStore
* Ad-hoc device connectivity via device AAS (initial)
* [#94](https://github.com/iip-ecosphere/platform/issues/94): Transport logging tool for debugging
* [#96](https://github.com/iip-ecosphere/platform/issues/96): Hostname as device id (optional, not default)
* [#36](https://github.com/iip-ecosphere/platform/issues/36): Generated service test frames
* [#101](https://github.com/iip-ecosphere/platform/issues/101): Documentation of fixed formats used by the platform
* [#114](https://github.com/iip-ecosphere/platform/issues/114): Initial integration of the OPC UA companion spec to IVML generator by J.-H. Cepok
* [#124](https://github.com/iip-ecosphere/platform/issues/124): Deployment plans can be configured and generated.
* [#105](https://github.com/iip-ecosphere/platform/issues/105): Maven plugins for Python build steps like "compile" (syntax check) and test. [#125](https://github.com/iip-ecosphere/platform/issues/125) allowing to execute python tests individually similar to surefire.
* [#109](https://github.com/iip-ecosphere/platform/issues/101): Maven integration for platform/application instantiation and orchestrated execution of Maven, e.g., for the examples.
* [#76](https://github.com/iip-ecosphere/platform/issues/76): Maven version of broker can be defined in configuration, optionally enabling use of QPID-J 9.0.0.
* [#81](https://github.com/iip-ecosphere/platform/issues/81): UI displays information from device AAS.
* [#102](https://github.com/iip-ecosphere/platform/issues/102): Development Docker container for service development.
* Technical Integration of federated learning [#127](https://github.com/iip-ecosphere/platform/issues/127), in particular server processes in application lifecycle and preparation of (template) generation
* Connector data paths (RecordType::path) are not assumed to be full paths anymore rather. They may end with a separator ("/") for full paths or not for prefix paths. For achieving the same behavior as before this version, please add a "/" to all path specifications in the configuration model.

Improvements:
* Extend configuration model, e.g., by constraints [#52](https://github.com/iip-ecosphere/platform/issues/52)
* [#38](https://github.com/iip-ecosphere/platform/issues/89): Transparent numpy conversion in generated Python
* [#58](https://github.com/iip-ecosphere/platform/issues/58): Remove user/password/keystore from model/code, use identity store instead
* [#95](https://github.com/iip-ecosphere/platform/issues/96): Document IVML model/variables for UI
* [#53](https://github.com/iip-ecosphere/platform/issues/53): Document test models 
* [#54](https://github.com/iip-ecosphere/platform/issues/54): Document example models 
* [#71](https://github.com/iip-ecosphere/platform/issues/71): Test/fixes for MQTT on transport layer instead of AMQP.
* [#85](https://github.com/iip-ecosphere/platform/issues/85): Display of platform build information on the UI
* [#104](https://github.com/iip-ecosphere/platform/issues/104): Generation and execution of synchronous Python services/Java sinks
* [#108](https://github.com/iip-ecosphere/platform/issues/108): Generated implementation templates support Eclipse PythonEditor PyDev
* [#103](https://github.com/iip-ecosphere/platform/issues/36): Generation of Python code with initialized fields and Pythonic field access while keeping getters/setters
* Multi-module top-level POM for local builds of the platform (please refer to the platform handbook for details).
* [#55](https://github.com/iip-ecosphere/platform/issues/55): Service integration workshop example/material
* [#112](https://github.com/iip-ecosphere/platform/issues/112): Example use of managed platform model structure in install package.
* [#91](https://github.com/iip-ecosphere/platform/issues/91): Dockerhub demonstration containers now with monitoring and management UI.
* [#119](https://github.com/iip-ecosphere/platform/issues/119): CI build process for configuration.configuration now cleans up left-over containers.
* [#111](https://github.com/iip-ecosphere/platform/issues/119): Simplifying the platform installation using the IIP-Ecosphere Maven plugins.
* [#121](https://github.com/iip-ecosphere/platform/issues/121): Examples and selected test models turned into managed model structure. Starter classes of examples substituted by mvn exec:java.
* [#84](https://github.com/iip-ecosphere/platform/issues/84): (generated) parameters for Python services.
* Component upgrades to BaSyx 1.3.0 [#35](https://github.com/iip-ecosphere/platform/issues/35), Apache QPID-J 9.0.0 [#76](https://github.com/iip-ecosphere/platform/issues/76) and *Java 11*.
* [#61](https://github.com/iip-ecosphere/platform/issues/71): examples.pythonSync now running with MQTT v3 instead of AMQP for transport.
* [#69](https://github.com/iip-ecosphere/platform/issues/69): Revise platform installation guidelines

### Version 0.4.0 (2022/09/02, TddT'22):

New features:
* Allowing to change service ensembles on demand via deployment plans.
* Upgrade of AMQP library rabbitmq client to 5.15.0 to enable ensemble service communication. Switching from direct queue to exchange-based AMQP communication.
* Transport message sending support with simple key-based routing for local and global transport connector.
* [#28](https://github.com/iip-ecosphere/platform/issues/28): Asynchronous service streams via Transport Layer for better stability.
* [#43](https://github.com/iip-ecosphere/platform/issues/43): Additional IVML model datatypes for OPC UA
* [#48](https://github.com/iip-ecosphere/platform/issues/48): Start of a platform-integrated library with basic data processing functionalities (based on the HM'22 demonstrator)
* [#32](https://github.com/iip-ecosphere/platform/issues/32): Take over functionality from HM'22 example into platform, here `PythonSupport` (now `service.environment` `ProcessSupport`).
* [#64](https://github.com/iip-ecosphere/platform/issues/64): Config model provides software information about devices to steer container creation.
* [#68](https://github.com/iip-ecosphere/platform/issues/68): Increasing number of semanticIDs in platform AAS and extensible semanticId resolution 
* [#77](https://github.com/iip-ecosphere/platform/issues/77): Semantic ID resolver based on ECLASS catalogue web service.
* Ability to mock connectors in applications for testing. Data to be provided to the application is loaded from connector-specific JSON file.
* Initial Java-side (optional) time monitoring of service execution.
* Explicit control of management port assignment in Spring Service execution to mitigate re-deployment limitations.
* HM'22/TddT'22 source code as example [#83](https://github.com/iip-ecosphere/platform/issues/83)

Improvements:
* [#30](https://github.com/iip-ecosphere/platform/issues/30): Redirect Python stdout in service environment console mode to allow for `print` in service without affecting console communication.
* [#27](https://github.com/iip-ecosphere/platform/issues/27): Move `iip.app.` prefix and system settings into `service.environment`.
* [#31](https://github.com/iip-ecosphere/platform/issues/31): Python service cleanup including `__pycache__` by terminating all external service implementations via alive-wait and destroy forcibly (from HM'22 example).
* [#33](https://github.com/iip-ecosphere/platform/issues/33): Split `IvmlTests` into separated tests in `configuration.configuration` to speed up automated container creation on Github.
* [#37](https://github.com/iip-ecosphere/platform/issues/37): Delay service mapping to allow for longer service startup.
* [#47](https://github.com/iip-ecosphere/platform/issues/47): Starting services with correct service state (STARTING, not RUNNING)
* [#46](https://github.com/iip-ecosphere/platform/issues/46): Validating/securing container lifecycle
* [#45](https://github.com/iip-ecosphere/platform/issues/45): Validating/securing service lifecycle
* [#34](https://github.com/iip-ecosphere/platform/issues/34): Fixed monitoring integration
* [#22](https://github.com/iip-ecosphere/platform/issues/22): Transport considers identity store, user/pwd deprecated and discouraged but still available.
* [#24](https://github.com/iip-ecosphere/platform/issues/24): Unify resource loading.
* [#41](https://github.com/iip-ecosphere/platform/issues/41), [#15](https://github.com/iip-ecosphere/platform/issues/15): Platform monitoring improvements 
* [#61](https://github.com/iip-ecosphere/platform/issues/61): Runtime dependency missing in alternative service package formats.
* [#60](https://github.com/iip-ecosphere/platform/issues/60): Nested connector paths not correctly composed in generation.
* [#25](https://github.com/iip-ecosphere/platform/issues/25): Enable application-specific connector triggers via code plugins.
* [#66](https://github.com/iip-ecosphere/platform/issues/66): Improvements of examples.KODEX and identifier check/fixing for generated Java code.
* [#65](https://github.com/iip-ecosphere/platform/issues/65): No configuration error messages issues by platform instantiator.
* [#67](https://github.com/iip-ecosphere/platform/issues/67): UI progress reporting support on platform side
* [#72](https://github.com/iip-ecosphere/platform/issues/72): Generated ingestor handling in default service implementation
* [#49](https://github.com/iip-ecosphere/platform/issues/49): Check whether there is a TensorFlow inference-only library - no.
* Improvements of service, family and stream generation: [#44](https://github.com/iip-ecosphere/platform/issues/44) and [#42](https://github.com/iip-ecosphere/platform/issues/42).
* Improvements of service execution: [#50](https://github.com/iip-ecosphere/platform/issues/50)
* Identity mechanism now with keystore support. `UriResolver` moved into basic support layer.
* [#40](https://github.com/iip-ecosphere/platform/issues/40): Simplification and unification of build processes for installation, templates and examples.
* [#73](https://github.com/iip-ecosphere/platform/issues/73): UI server improvements
* [#80](https://github.com/iip-ecosphere/platform/issues/80): UI instantiation 

### Version 0.3.0 (2022/06/04, HM'22):

New functionality:
* BaSyx upgrade to 1.0.0 and then to 1.0.1 (from Maven Central)
* EASy-Producer version 1.3.2
* Complete platform runs in Docker container, see [install information](../documentation/INSTALL.md) how to play with them.
* Installation summary for Docker containers and platform from scratch on github.
* Model-based integration of KODEX as platform service (and upgrade to 0.0.8) for anonymization/pseudonymization (GO program, via command line streams). Respective extension of configuration model and code generation.
* Initial mapping of the AAS product name/typeplate into the AAS abstraction and the default BaSyx implementation.
* Initial version of service execution with multiple brokers and data paths between multiple devices.
* More flexible device ids via IdProvider and, if permitted, overriding the device id via command line parameter `--iip.id`.
* Simple deployment plans for the CLI.
* Python service execution environment (command-line based approach) and model-based automated integration of Python services.
* Automatic build of demo containers on github, deployment to dockerhub.
* Extended model-based integration of MQTT, OPC UA and AAS connectors
* Internal status channel for added/removed/changed components
* Examples for OPC UA connector creation, RapidMiner RTSA service chain and Python service integration. RTSA and Python examples include application-specific Java services
* Individual AAS for devices, services and Apps.
* Platform can instantiate broker.
* Template projects for application creation.
* Lifecycle profiles for starting parts of components.
* Integration of Prometheus for central monitoring, i.e., delivery of micrometer data into prometheus, alerts to AAS. Disabled, conflicting with Tomcat/Apache Qpid.    

Bug fixes / Improvements:
* Missing resource headline/identifier in platform CLI
* Wrong version numbers for platformDependencies/broker in Install package
* Wrong content types file name when writing an AASX file
* TLS encryption support for the basic transport connectors.
* Spring transport connector instances/binders can be configured individually. TLS encryption support for the spring transport connectors/binders is available.
* MQTT machine connectors support optional TLS encryption.
* PID files for the major platform components
* Generation of Linux/systemd service descriptions for platform services, ECS runtime and service manager. Generation of separate scripts for Java 8 (no module system).
* Generation of README.txt with brief explanations on the generated files and folders.
* Fixed/extended qualified name access for OPC UA connector.
* Automated instantiation of a one-process ECS runtime/service manager without container manager for devices with low resources.
* Platform instantiation process without Java test execution (may fail depending on JDK/surefire combination).
* Integration of initial device management (BSc Dennis Pidun) with ThingsBoard, in-memory registry, MinIO and S3Fake connectors.
* Code refactoring: Renaming setup-related "Configuration" classes to "Setup".
* TLS on AAS abstraction level and VAB-HTTPS protocol, TLS for AAS connector
* Security fix for [CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228): Determined by the log4j use of integrated components, we enforce log4j 2.15 (core) where needed, in particular in service artifacts, or rely transitively on logback.
* Separated generation of platform application interfaces and application code
* Changed generated service artifact classifier to *bin* in order to keep the original file for testing.

### Basis platform release (2021/8/09)
* Services layer, service management for Spring Cloud Stream, service execution environment for Java and (initial) Python
* Resources/ECS runtime, container management for Docker
* Initial configuration model and code generation
* Initial transport-based resource/service monitoring (based on PA Miguel Casado)
* Maven central deployment script, deployment guideline
* First central platform server
* Platform handbook

### First release on Maven central (2021/03/06)
* Improved performance of stream-based transport 
* Hivemq spring binders
* Preparation of Maven central release

### Initial release (2021/01/27)
* Dependency management
* Basic transport component
* Basic connectors component
* Basic AAS abstraction (based on PA Monika Staciwa)