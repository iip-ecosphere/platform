# IIP-Ecosphere platform: Releases

### Next release (winter 2021):
* BaSyx upgrade to 1.0.0 and then to 1.0.1 (from Maven Central)
* Complete platform runs in Docker container, see [install information](../documentation/INSTALL.md) how to play with them.
* Installation summary for Docker containers and platform from scratch on github.
* Initial integration of KODEX as platform service for anonymization/pseudonymization.
* Initial mapping of the AAS product name/typeplate into the AAS abstraction and the default BaSyx implementation.
* Initial version of service execution with multiple brokers and data paths between multiple devices.
* More flexible device ids via IdProvider and, if permitted, overriding the device id via command line parameter `--iip.id`.
* Simple deployment plans for the CLI.
* Bug fixes / Improvements:
    * Missing resource headline/identifier in platform Cli
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
    * Automatic build of demo containers on github, deployment to dockerhub.
    * TLS on AAS abstraction level and VAB-HTTPS protocol, TLS for AAS connector
    * Security fix for [CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228): Determined by the log4j use of integrated components, we enforce log4j 2.15 (core) where needed, in particular in service artifacts, or rely transitively on logback.
* Planned improvements:
    * Python service execution environment
    * Integrated container management for Kubernetes
    * Automatic creation of containers and their accessibility for devices.
    * More detailed configuration model with even more code generation.
    * Potentially, an initial version of the platform monitoring.


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