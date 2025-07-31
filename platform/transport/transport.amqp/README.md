# oktoflow transport extension for AMQP (based on RabbitMQ)

Extends the transport layer in terms of the AMQP protocol. This extension is optional. It can be loaded as plugin or used as JSL component (direct dependency, e.g. for testing).
This implementation ships with an [AMQP 1.0](https://www.amqp.org/) client as  transport connectors based on 
[RabbitMQ](https://www.rabbitmq.com/). This connector does not consider the specified (MQTT) QoS from the connector parameters rather than it sets the AMQP basic QoS settings to unlimited queues.

TLS can be configured via the transport parameters. If no key alias is specified, the underlying implementation takes the first available key. The underlying implementation does not support SSL hostname verification, i.e., the configuration parameter is ignored.

Akin to the transport component/layer, the regression tests exercise the transport connector with simple JSON and 
[Google protobuf](https://developers.google.com/protocol-buffers) 
serialization against [Apache QPID broker J](https://qpid.apache.org/components/broker-j/index.html) as AMQP broker. 
Dependencies to the json-simple library, Google protobuf and HiveQM are used for testing 
only and not required for platform execution. The tests also exercise plaintext/TLS connections.

