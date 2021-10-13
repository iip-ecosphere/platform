# Connectors Component MQTTv5 extension in the Transport Layer of the IIP-Ecosphere platform

MQTTv5 machine/platform for bi-directional access to machines and already installed platforms. This connector supports TLS encryption based on a keystore, a keystore password, an optional key alias and a setting for hostname verification in the generic connector parameters. 

**Issues/Worth considering**
- So far, testing this component requires JDK 11.
- As testing relies on HiveMQ, minimum SSL key lengths apply (`see test.mqtt.hivemq`).
