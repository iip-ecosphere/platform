package test.de.iip_ecosphere.platform.transport;

import java.io.File;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;

/**
 * A simple embedded HiveMQ test server for MQTT.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestHiveMqServer {
    
    private EmbeddedHiveMQ hiveMQ;

    /**
     * Starts the server.
     * 
     * @param host the host name
     * @param port the port number
     */
    public void start(String host, int port) {
        if (null == hiveMQ) {
            String tmp = System.getProperty("java.io.tmpdir");
            System.setProperty("HIVEMQ_PORT", Integer.toString(port));
            System.setProperty("HIVEMQ_ADDRESS", host);
            System.setProperty("hivemq.log.folder", tmp);
            
            File cfg = new File("./src/test");
            final EmbeddedHiveMQBuilder embeddedHiveMQBuilder = EmbeddedHiveMQBuilder.builder()
                .withConfigurationFolder(cfg.toPath())
                .withDataFolder(new File(tmp).toPath())
                .withExtensionsFolder(new File(cfg, "extensions").toPath());
    
            hiveMQ = embeddedHiveMQBuilder.build();
            hiveMQ.start().join();
        }
    }
    
    /**
     * Stops the server.
     */
    public void stop() {
        hiveMQ.stop().join();
        hiveMQ = null;
    }

}
