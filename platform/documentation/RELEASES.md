# IIP-Ecosphere platform: Releases

### Next release (winter 2021):
* BaSyx upgrade to 1.0.0 (from Maven Central)
* Complete platform runs in Docker container, see [install information](../documentation/INSTALL.md) how to play with them.
* Installation summary for Docker containers and platform from scratch on github.
* Bug fixes / Improvements:
    * Missing resource headline/identifier in platform Cli
    * Wrong version numbers for platformDependencies/broker in Install package
    * Wrong content types file name when writing an AASX file
    * TLS encryption support for the basic transport connectors.
    * Spring transport connector instances/binders can be configured individually. TLS encryption support for the spring transport connectors/binders is available.
    * MQTT machine connectors support optional TLS encryption.
    * PID files for the major platform components
    * Generation of Linux/systemd service descriptions for platform services, ECS runtime and service manager. 
    * Generation of README.txt with brief explanations on the generated files and folders.
    * Fixed/extended qualified name access for OPC UA connector.
* Planned improvements:
    * Python service execution environment
    * Integrated container management for Kubernetes
    * Automatic creation of containers and their accessibility for devices.
    * More detailed configuration model with even more code generation.
    * Potentially, an initial version of the device management and the platform monitoring.
    * Integration of initial security/privacy mechanisms

### Basis platform release (2021/8/09)
* Services layer, service management for Spring Cloud Stream, service execution environment for Java and (initial) Python
* Resources/ECS runtime, container management for Docker
* Initial configuration model and code generation
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