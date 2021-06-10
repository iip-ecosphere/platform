# IIP-Ecosphere platform: Service execution environment

This component provides a multi-language service execution environment/support classes for IIP-Ecosphere. In particular,
a main task is to encapsulate the communication of services with the platform. 

The core here is a service interface for administrative purposes. The service interface can directly be mapped into a AAS implementation server and, thus, represent the AAS implementation side (we rely for now on BaSyx VAB through the AAS Factory).

However, it is important to recall, that this component shall not contain the full interfaces rather than basic and support interfaces. The full interfaces shall be derived via code generation for configured services and be based on the interfaces here. The support methods shall be called from generated code to create the implementation server, to map the service(s) into etc.

## Java

On the Java side, this component provides the ``Service`` interface and a ``ServiceMapper`` linking the known service functions through ``ProtocolServerBuilder`` into an AAS implementation server. 

TBD: Miguel: The current general monitoring properties provided by Spring/Micrometer.io including health monitoring.

Further additional support classes allow reading basic information from the YAML service deployment descriptors (forward defintion for services component).

## Python

On the Python side, this component provides a simple framework around the "abstract" ``Service`` class and similar ``ServiceMapper`` linking the known service functions through a Python port of the ``ProtocolServerBuilder`` into an AAS implementation server. It is important to recall, that the Python side just provides the implementation server. No more interaction with the AAS is needed. 

Prerequisites/requirements:

- Python at least version 3.5
- pyyaml for YAML parsing 

## missing

* Complete service interface, integration with services component.
* Data streaming communication (generation support)
* Python packaging for services startup (Maven, e.g., ZIP)
