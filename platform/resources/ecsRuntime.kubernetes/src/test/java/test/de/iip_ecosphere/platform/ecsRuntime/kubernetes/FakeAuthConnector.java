package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import com.rabbitmq.client.ConnectionFactory;

import de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportConnector;

public class FakeAuthConnector extends RabbitMqAmqpTransportConnector {

    @Override
    protected void configureFactory(ConnectionFactory factory) {
        factory.setUsername("user");
        factory.setPassword("pwd");
    }

}