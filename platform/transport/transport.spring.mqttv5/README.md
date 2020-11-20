# Transport layer of the IIP-Ecosphere platform: Spring connector/binder for MQTT v5

This component provides a MQTT v5 transport implementation for the communication from/to a device. A binder/connector 
extends the Spring Cloud Stream framework by the specific protocol.

The implementation is initial and could be optimized. The following configuration options are supported:
 * `mqtt.host`: Host name of the MQTT broker
 * `mqtt.port`: TCP port number of the MQTT broker
 * `mqtt.schema`: Transport schema (default: `tcp`)
 * `mqtt.clientId`: Client identification of this device
 * `mqtt.keepAlive`: Time in milliseconds between two MQTT heartbeats (default: `60000`)
 * `mqtt.actionTimeout`: Time in milliseconds to wait for an operation to complete (default: `1000`)
 * `mqtt.filteredTopics`: List of topic names not to subscribe to.

**Missing**
- Validation/fixing topic names w.r.t. MQTT specification
- Authentication/Security

