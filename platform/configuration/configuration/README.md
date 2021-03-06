# IIP-Ecosphere platform: Configuration component

IIP-Ecosphere aims at an encompassing and consistent configuration of the whole platform in order to enable model-based platform instantiation, i.e., to include relevant parts and to exclude unwanted parts. This is the point in the platform where optional components such as transport protocols, connectors, service execution managers, container managers, service chains and applications etc. are combined, configured, validated and ultimately turned into instantiated code and installable artifacts.

As configuration technology, we rely on [EASy-Producer](https://sse.uni-hildesheim.de/forschung/projekte/easy-producer/), it's variability modeling language IVML and its instantiation languages VIL/VTL. All languages are described on the EASy-Producer website. We integrate EASy-Producer here through it's Maven artifacts, define the variability model in IVML, the variability instantiation in VTL and perform respective tests.

It is important to understand that the configuration model and the related code generation are evolving. For the actual release, we did not aim at a complete model rather than a model that can serve for initial tests and demonstrators. Thus, for now, advanced capabilities such as the assignment of services to resources, the respective partitioning (strategy) of the generated artifacts, or a mapping of external data types through the model/generation are intentionally missing. In particular data type mapping is currently considered as application-specific code that must be provided as application artifact. Further, services are currently loaded at runtime, which may turn into an IVML/VIL extension in the future.

So far there is no graphical user interface which shall be located one layer above this component, i.e., use the interface the component provides. For initial instantiation, we provide the PlatformInstantiator class which executes the configuration process from command line.

## missing

- variability model: resources, resource assignment to ServiceMeshes
- generation of containers
- AAS-based interface to the configuration, reasoning, instantiation 
- generated Java service instantiation: connector adapter type code
- generated Python service instantiation: inclusion of environment zip, data connection, serializer registration, ...
- optional: included micrometer gauges, timers, counters

