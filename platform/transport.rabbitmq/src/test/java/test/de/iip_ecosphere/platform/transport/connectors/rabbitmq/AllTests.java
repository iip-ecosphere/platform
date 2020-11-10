package test.de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RabbitMqAmqpTransportConnectorTest.class
})
public class AllTests {
}
