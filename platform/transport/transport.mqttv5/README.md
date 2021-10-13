# Transport layer of the IIP-Ecosphere platform: Transport plugin MQTT v5

This component provides a MQTT v5 transport plugin for the IIP-Ecosphere platform transport layer. It can be used
standalone and in tests. Also the tests can be used as further basis for MQTT v5 based protocols.

This implementation ships with [MQTT](https://mqtt.org/) v5 client as default transport connectors based on 
[Eclipse Paho](https://www.eclipse.org/paho/).

TLS can be configured via the transport parameters. If no key alias is specified, the underlying implementation takes the first available key.

For the moment, the regression tests exercise the transport connectors with simple JSON and [Google protobuf](https://developers.google.com/protocol-buffers) serialization against [HiveMQ](https://www.hivemq.com) as MQTT broker 
supporting MQTT v3 and v5. Dependencies to the json-simple library, Google protobuf and HiveQM are used for testing 
only and not required for platform execution. Serializers and data classes are reused from the basic transport 
component. 

**Missing**
- Validation/fixing topic names w.r.t. MQTT specification

**Issues/Worth considering**
- This project requires JDK 11 for testing. 
- As testing relies on HiveMQ, minimum SSL key lengths apply (`see test.mqtt.hivemq`).

