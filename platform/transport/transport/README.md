# Transport Component in the Transport Layer of the IIP-Ecosphere platform

The Transport Component shall provide a simple frontend for the upstream layers/components to communicate in 
stream-based soft-realtime manner. At the same time this component shall enable flexibility and simplicity in the 
communication, allowing to exchange the underlying transport protocol if needed (before platform startup), e.g., due 
to license issues. 

At the moment, we assume homogeneous streams. Heterogeneous streams may be added, but are currently not supported.

For turning objects into their transport representation, we use as serialization interfaces. However, we assume that
the object classes will be defined in upstream layers, i.e., this component ships without type-specific serialization 
mechanisms that shall be defined accordingly in upstream layers. Type translators are intended to translate application
specific into more general types required by reusable components, i.e., the translators act as input or output type 
adapters. Serializers are bidirectional type translators.

For the moment, the regression tests exercise the transport connectors with simple JSON and [Google protobuf](https://developers.google.com/protocol-buffers) serialization against [HiveMQ](https://www.hivemq.com) as MQTT broker 
supporting MQTT v3 and v5. Dependencies to the json-simple library, Google protobuf and HiveQM are used for testing 
only and not required for platform execution.

User/password authentication is currently supported for transport mechanisms that implement such authentication, whereby
the interpretation of the "password", e.g., as token, depends on the transport mechanism. Transport Layer Security is
prepared in terms of specifying a keystore (JKS, PCK12) and a keystore password, whereby the validity of the keystore type and the interpretation of the password depends on the implementing transport mechanisms.
