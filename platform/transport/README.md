# IIP-Ecosphere transport

Basic functionality for flexibly transporting data through the platform
* [Interfaces](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport/README.md), management and transport AAS
* Transport connectors for MQTT v3 [paho](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.mqttv3/README.md), i.e., based on [Eclipse Paho](https://www.eclipse.org/paho/)
* Transport connectors for MQTT v5 [paho](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.mqttv5/README.md), i.e., based on [Eclipse Paho](https://www.eclipse.org/paho/)
* Transport connector for [AMQP](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.amqp/README.md) based on [RabbitMQ](https://www.rabbitmq.com/)
* Optional [transport support](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring/README.md) for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream)
    * Transport connector binders for MQTT v3 [paho](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring.mqttv3/README.md) and [hivemq](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring.hivemqv3/README.md)
    * Transport connector binders for MQTT v5 [paho](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring.mqttv5/README.md) and [hivemq] (https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring.hivemqv5/README.md)
    * Transport connector binder for [AMQP](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring.amqp/README.md) based on [RabbitMQ](https://www.rabbitmq.com/)