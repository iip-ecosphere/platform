# Connectors Component MQTTv3 extension in the Transport Layer of the oktoflow platform

MQTTv3 machine/platform for bi-directional access to machines and already installed platforms. It can be loaded as plugin or used as JSL component (direct dependency, e.g. for testing). This connector supports TLS encryption based on a keystore, a keystore password, an optional key alias and a setting for hostname verification in the generic connector parameters.

**Issues/Worth considering**
- As testing relies on HiveMQ, minimum SSL key lengths apply (`see test.mqtt.hivemq`).
