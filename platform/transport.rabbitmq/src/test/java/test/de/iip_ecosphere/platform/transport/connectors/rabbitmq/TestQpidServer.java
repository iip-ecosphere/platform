package test.de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;

/**
 * A simple AMQP server.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestQpidServer {
    
    private SystemLauncher systemLauncher;
    
    // checkstyle: stop exception type check
    
    /**
     * Starts the server.
     * 
     * @param host the host name
     * @param port the port number
     * @throws IOException if starting fails
     */
    public void start(String host, int port) throws IOException {
        System.setProperty("qpid.amqp_port", Integer.toString(port));
        systemLauncher = new SystemLauncher();
        Map<String, Object> attributes = new HashMap<String, Object>();
        File f = new File("./src/test/config.json");
        URL initialConfig = f.toURI().toURL();
        // https://qpid.apache.org/releases/qpid-broker-j-8.0.0/book/
        // Java-Broker-Initial-Configuration-Configuration-Properties.html
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        try {
            systemLauncher.startup(attributes);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    // checkstyle: resume exception type check
    
    /**
     * Stops the server.
     */
    public void stop() {
        systemLauncher.shutdown();
    }

}
