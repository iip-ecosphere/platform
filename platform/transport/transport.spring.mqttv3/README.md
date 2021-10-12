# Transport layer of the IIP-Ecosphere platform: Spring connector/binder for MQTT v3

This component provides a MQTT v3 transport implementation for the communication from/to a device. A binder/connector 
extends the Spring Cloud Stream framework by the specific protocol.

The following configuration options are supported:
 * `mqtt.host`: Host name of the MQTT broker
 * `mqtt.port`: TCP port number of the MQTT broker
 * `mqtt.schema`: Transport schema (default: `tcp`)
 * `mqtt.clientId`: Client identification of this device
 * `mqtt.autoClientId`: Adjust the client id to make it unique (default: `true`)
 * `mqtt.keepAlive`: Time in milliseconds between two MQTT heartbeats (default: `60000`)
 * `mqtt.actionTimeout`: Time in milliseconds to wait for an operation to complete (default: `1000`)
 * `mqtt.filteredTopics`: List of topic names not to subscribe to  (as list, via suffix [0], [1], ...).
 * `mqtt.qos`: Quality of service level, one from `AT_MOST_ONCE`, `AT_LEAST_ONCE` (default), `EXACTLY_ONCE`
 * `mqtt.keystore`: Optional file name of TLS keystore (default: null).
 * `mqtt.keyPassword`: Optional plaintext keystore password (default: null).
 * `mqtt.keyAlias`: Alias of the key top use (default: null). If not set, the best matching key is taken (currently TLS hostname verification is disabled by default).

**Missing**
- Validation/fixing topic names w.r.t. MQTT specification
- Authentication/Security

