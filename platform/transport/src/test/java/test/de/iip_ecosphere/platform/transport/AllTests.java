package test.de.iip_ecosphere.platform.transport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Defines the tests to be executed.
 * 
 * @author Holger Eichelberger, SSE
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PahoMqttV3TransportConnectorTest.class, 
    PahoMqttV5TransportConnectorTest.class
})
public class AllTests {
}
