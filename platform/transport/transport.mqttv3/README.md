# Transport layer of the IIP-Ecosphere platform: Transport plugin MQTT v3

This component provides a MQTT v3 transport plugin for the IIP-Ecosphere platform transport layer. It can be used
standalone and in tests. Also the tests can be used as further basis for MQTT v5 based protocols.

This implementation ships with [MQTT](https://mqtt.org/) v3 client as default transport connectors based on 
[Eclipse Paho](https://www.eclipse.org/paho/). The implementation allows for optional TLS encryption.

For the moment, the regression tests exercise the transport connectors with simple JSON and [Google protobuf](https://developers.google.com/protocol-buffers) serialization against [HiveMQ](https://www.hivemq.com) as MQTT broker 
supporting MQTT v3 and v5. Dependencies to the json-simple library, Google protobuf and HiveQM are used for testing 
only and not required for platform execution. Serializers and data classes are reused from the basic transport 
component. For TLS testing, we use the keystore from the IIP-Ecosphere moquette testing package.

**Missing**
- Validation/fixing topic names w.r.t. MQTT specification
