# Transport layer of the oktoflow platform: Spring connector/binder for the active oktoflow transport connector (generic)

This component provides a generic transport implementation for the communication from/to a device based on the active transport connector. A binder/connector 
extends the Spring Cloud Stream framework by the specific protocol. As this component is loaded into Spring, it is a partial oktoflow plugin that shall be added to/merged into a spring plugin, thus, it has no plugin descriptor.

The implementation is initial and could be optimized. The following configuration options are supported:
 * `generic.host`: Host name of the AMQP broker
 * `generic.port`: TCP port number of the AMQP broker
 * `generic.filteredTopics`: List of topic names not to subscribe to.
 * `generic.keystore`: Optional file name of TLS keystore (default: null).
 * `generic.keyPassword`: Optional plaintext keystore password (default: null).
 * `generic.keyAlias`: Optional key alias denoting the key in the keystore to be used (default: null). If no alias is given, the underlying implementation takes the first available key.
 * `generic.hostnameVerification` wheter SSL hostname verification shall be applied by the underlying transport connector.
