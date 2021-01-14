# IIP-Ecosphere transport

Basic functionality for flexibly transporting data through the platform
* [Interfaces](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport/README.md), management and transport AAS
* Transport connector for MQTT [v3](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.mqttv3/README.md) and [v5](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.mqttv5/README.md) based on [Eclipse Paho](https://www.eclipse.org/paho/)
* Transport connector for [AMQP](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.amqp/README.md) based on [RabbitMQ](https://www.rabbitmq.com/)
* Optional [transport support](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.spring/README.md) for [Spring cloud stream](https://spring.io/projects/spring-cloud-stream)
    * Transport connector binder for MQTT [v3](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.mqttv3/README.md) and [v5](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.mqttv5/README.md) based on [Eclipse Paho](https://www.eclipse.org/paho/)
    * Transport connector binder for [AMQP](https://github.com/iip-ecosphere/platform/tree/main/platform/transport/transport.amqp/README.md) based on [RabbitMQ](https://www.rabbitmq.com/)