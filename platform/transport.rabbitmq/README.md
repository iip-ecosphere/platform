# IIP-Ecosphere transport extension for AMQP (based on RabbitMQ)

Extends the transport layer in terms of the AMQP protocol. This extension is optional. 
This implementation ships with an [AMQP 1.0](https://www.amqp.org/) client as  transport connectors based on 
[RabbitMQ](https://www.rabbitmq.com/).  

Akin to the transport component/layer, the regression tests exercise the transport connector with simple JSON and 
[Google protobuf](https://developers.google.com/protocol-buffers) 
serialization against [Apache QPID broker J](https://qpid.apache.org/components/broker-j/index.html) as AMQP broker. 
Dependencies to the json-simple library, Google protobuf and HiveQM are used for testing 
only and not required for platform execution.

**Missing**
- Authentication/Security
