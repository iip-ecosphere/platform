# oktoflow platform: Transport Component

Basic functionality for flexibly transporting data through the platform
* [Interfaces](transport/README.md), management and transport AAS
* Transport connectors for [MQTT v3](transport.mqttv3/README.md), i.e., based on [Eclipse Paho](https://www.eclipse.org/paho/)
* Transport connectors for [MQTT v5](transport.mqttv5/README.md), i.e., based on [Eclipse Paho](https://www.eclipse.org/paho/)
* Transport connector for [AMQP](transport.amqp/README.md) based on [RabbitMQ](https://www.rabbitmq.com/)
* Optional [transport support](transport.spring/README.md) for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream)
    * Generic transport connector binder for the transport [connector instances provided by the transport component](transport.spring.generic/README.md)
    * Transport connector binders for MQTT v3 [paho](transport.spring.mqttv3/README.md) and [hivemq](transport.spring.hivemqv3/README.md) based on [HiveMq client](https://www.hivemq.com/developers/community/)
    * Transport connector binders for MQTT v5 [paho](transport.spring.mqttv5/README.md) and [hivemq](transport.spring.hivemqv5/README.md) based on [HiveMq client](https://www.hivemq.com/developers/community/)
    * Transport connector binder for [AMQP](transport.spring.amqp/README.md) based on [RabbitMQ](https://www.rabbitmq.com/)