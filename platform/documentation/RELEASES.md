# IIP-Ecosphere platform releases

### Next release (winter 2021):
* BaSyx upgrade to 1.0.0 (from Maven Central)
* Bug fixes / Improvements:
    * Missing resource headline/identifier in platform Cli
    * Wrong version numbers for platformDependencies/broker in Install package
    * Wrong content types file name when writing an AASX file
    * TLS encryption support for the basic transport connectors.
    * Spring transport connector instances/binders can be configured individually. TLS encryption support for the spring transport connectors/binders is available.
    * MQTT machine connectors support optional TLS encryption.
    * PID files for the major platform components
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