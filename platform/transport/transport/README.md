# Transport layer of the IIP-Ecosphere platform (basics)

The transport layer/component shall provide a simple frontend for the upstream layers/components to communicate in 
stream-based soft-realtime manner. At the same time this component shall enable flexibility and simplicity in the 
communication, allowing to exchange the underlying transport protocol if needed (before platform startup), e.g., due 
to license issues. 

At the moment, we assume homogeneous streams. Heterogeneous streams may be added, but are currently not supported.

For turning objects into their transport representation, we use as serialization interfaces. However, we assume that
the object classes will be defined in upstream layers, i.e., this component ships without type-specific serialization 
mechnisms that shall be defined accordingly in upstream layers.

This implementation ships with [MQTT](https://mqtt.org/) v3 and v5 clients as default transport connectors based on 
[Eclipse Paho](https://www.eclipse.org/paho/). As explained above, there are no default transport serializers included. 

For the moment, the regression tests exercise the transport connectors with simple JSON and [Google protobuf](https://developers.google.com/protocol-buffers) serialization against [HiveMQ](https://www.hivemq.com) as MQTT broker 
supporting MQTT v3 and v5. Dependencies to the json-simple library, Google protobuf and HiveQM are used for testing 
only and not required for platform execution.

**Missing**
- Transport AAS 
- Authentication/Security

