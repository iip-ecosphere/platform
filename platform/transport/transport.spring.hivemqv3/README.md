# Transport layer of the IIP-Ecosphere platform: Spring connector/binder for HiveMq MQTT v3

This component provides a MQTT v3 transport implementation for the communication from/to a device. A binder/connector 
extends the Spring Cloud Stream framework by the specific protocol.

The implementation is initial and could be optimized. The following configuration options are supported:
 * `mqtt.host`: Host name of the MQTT broker
 * `mqtt.port`: TCP port number of the MQTT broker
 * `mqtt.clientId`: Client identification of this device
 * `mqtt.autoClientId`: Adjust the client id to make it unique (default: `true`)
 * `mqtt.keepAlive`: Time in milliseconds between two MQTT heartbeats (default: `60000`)
 * `mqtt.filteredTopics`: List of topic names not to subscribe to (as list, via suffix [0], [1], ...).
 * `mqtt.qos`: Quality of service level, one from `AT_MOST_ONCE`, `AT_LEAST_ONCE` (default), `EXACTLY_ONCE`
 * `mqtt.keystore`: Optional file name of TLS keystore (default: null).
 * `mqtt.keyPassword`: Optional plaintext keystore password (default: null).
 * `mqtt.keyAlias`: Alias of the key top use (default: null). If not set, the best matching key is taken.
 * `mqtt.hostnameVerification` (default `false`) determines whether the hostname shall be verified against the SSL certificates during SSL handshake. `true` enables HTTPS hostname verification.

**Missing**
- Validation/fixing topic names w.r.t. MQTT specification

**Issues/Worth considering**
- This project requires JDK 11 for testing.
- As testing relies on HiveMQ, minimum SSL key lengths apply (`see test.mqtt.hivemq`).
