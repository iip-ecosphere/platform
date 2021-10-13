# Transport layer of the IIP-Ecosphere platform: Spring connector/binder for AMQP

This component provides an AMQP transport implementation for the communication from/to a device. A binder/connector 
extends the Spring Cloud Stream framework by the specific protocol.

The implementation is initial and could be optimized. The following configuration options are supported:
 * `amqp.host`: Host name of the AMQP broker
 * `amqp.port`: TCP port number of the AMQP broker
 * `amqp.schema`: Transport schema (default: `tcp`)
 * `amqp.clientId`: Client identification of this device
 * `amqp.keepAlive`: Time in milliseconds between two AMQP heartbeats (default: `60000`)
 * `amqp.actionTimeout`: Time in milliseconds to wait for an operation to complete (default: `1000`)
 * `amqp.filteredTopics`: List of topic names not to subscribe to.
 * `amqp.keystore`: Optional file name of TLS keystore (default: null).
 * `amqp.keyPassword`: Optional plaintext keystore password (default: null).
 * `amqp.keyAlias`: Optional key alias denoting the key in the keystore to be used (default: null). If no alias is given, the underlying implementation takes the first available key.

`amqp.hostnameVerification` is ignored as the underlying implementation does not support SSL hostname verification.

**Missing**
- Validation/fixing topic names w.r.t. AMQP specification
