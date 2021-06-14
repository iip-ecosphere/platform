# IIP-Ecosphere platform: Configuration component

IIP-Ecosphere aims at an encompassing and consistent configuration of the whole platform in order to enable model-based platform instantiation, i.e., to include relevant parts and to exclude unwanted parts. This is the point in the platform where optional components such as transport protocols, connectors, service execution managers, container managers, service chains and applications etc. are combined, configured, validated and ultimately turned into instantiated code and installable artifacts.

As configuration technology, we rely on [EASy-Producer](https://sse.uni-hildesheim.de/forschung/projekte/easy-producer/), it's variability modeling language IVML and its instantiation languages VIL/VTL. All languages are described on the EASy-Producer website. We integrate EASy-Producer here through it's Maven artifacts, define the variability model in IVML, the variability instantiation in VTL and perform respective tests.

## missing

- several details of the variability model / instantiation (in development)
- AAS-based interfaces for the remaining components 

