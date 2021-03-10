package test.de.iip_ecosphere.platform.simpleStream.spring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * Executes the streams in {@link Test} with as well as a broker.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWithBroker {

    private static Server broker;

    /**
     * Tries to extract the broker configuration.
     */
    private static void extractBrokerConfig() {
        try {
            String loc = "hivemqv5cfg.zip"; // Maven turns file names to small caps                
            if (AbstractTestServer.runsFromJar() && null != loc && loc.length() > 0) {
                System.out.println("Extracting server configuration from " + loc);
                AbstractTestServer.setConfigDir(AbstractTestServer.createTmpFolder("brokerConfig"));
                AbstractTestServer.extractConfiguration(loc, "");
            } else { // dev execution, unpacked
                AbstractTestServer.setConfigDir(new File("./src/test/hiveMqv5Cfg")); // null is ok
            }
        } catch (IOException e) {
            System.err.println("Cannot find/extract server configuration: " + e.getMessage());
        }
    }
    
    /**
     * Main function.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        ServerAddress addr = new ServerAddress(Schema.IGNORE); // localhost, ephemeral
        List<String> aTmp = new ArrayList<String>();
        
        // extend args with dynamic mqtt.port, i.e., override application.yml
        CollectionUtils.addAll(aTmp, args);
        aTmp.add("--mqtt.port=" + addr.getPort());
        //aTmp.add("--test.debug=true");
        //aTmp.add("--test.ingestCount=200");
        args = new String[aTmp.size()];
        aTmp.toArray(args);
        
        // start broker
        extractBrokerConfig();
        broker = new test.de.iip_ecosphere.platform.transport.mqttv5.TestHiveMqServer(addr);
        broker.start();
        
        Test.main(args);
    }
    
}
