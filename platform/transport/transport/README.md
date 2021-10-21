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

### How to use this component?

* Select a protocol and set up the server/broker side.
* Include the transport component and the specific transport protocol component (`transport.*`) into your project, preferably using Maven.
* Select a wire format and set up the type-based serialization classes (in platform: generated). Register the serializer with the `SerializerRegistry` (along with default serializers, e.g. for Strings).
* Create a transport parameter instance via its builder class (`TransportParameter.TransportParameterBuilder`)
* Create a connector instance
    * Pragmatic: Call the constructor of the connector
    * Intended platform use: Use the `TransportFactory`, which is automatically set up via JSL for the selected transport component (not for multiple ones).
* If you want to receive data: Register a `ReceptionCallback` with the connector. Wire-to-instance translation will be done by the serializer mechanism.
* If you want to send data: Call the send method (synchronous, asynchronous). Wire-to-instance translation will be done by the serializer mechanism.
