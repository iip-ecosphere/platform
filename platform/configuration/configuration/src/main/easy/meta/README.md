# IIP-Ecosphere platform: Configuration model (Configuration Meta Model, 'meta')

We briefly explain the modules (`project`) in along their import hierarchy, starting with the basic modules.

* `MetaConcepts`: Generic concepts for ressource-assigned, adaptive systems. Originates from work in FP7 QualiMaster and used here to experiment with the concepts.
* `DataTypes`: Basic data types used to define data flows and applications, such as primitives or "records" with typed fields.
* `Transport`: Settings for data transport and wire format within the plattform.
* `AAS`: Technical settings for provisioning and accessing the platform Asset Administration Shell (AAS).
* `Devices`: Configuration structures for devices including edge, server and cloud resources. Settings may influence how containers are generated.
* `Resources`: Setup of resource management and platform-wide file exchange, e.g., for OTA.
* `Services`: Definition of custom (Java/Python) services and platform-integrated services. Services may be assigned to/require devices.
* `Connectors`: Specialization of Services for the data ingestion/control of utilized machines or installed platforms.
* `Applications`: Concepts for defining service-orienged IIoT applications.
