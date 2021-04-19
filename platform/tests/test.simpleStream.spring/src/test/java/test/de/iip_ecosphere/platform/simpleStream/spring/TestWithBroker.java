package test.de.iip_ecosphere.platform.simpleStream.spring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * Executes the streams in {@link Test} with as well as a broker. The broker is prescribed by the environment
 * this test shall be executed within, i.e., services.spring which instantiates the Qpid AMQP server. 
 * Requires the Qpid configuration file in src/test and packaged as ZIP into the services jar file (see POM). We do 
 * not rely on MQTT here, because moquette is not stable enough and Hivemq requires JDK 11.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestWithBroker {

    private static Server broker;

    /**
     * Tries to extract the broker configuration.
     * 
     * @param zipLoc the location where to read the zipped file from the classpath
     * @param unpacked the unpackaged configuration in the source project
     */
    private static void extractBrokerConfig(String zipLoc, String unpacked) {
        try {
            if (AbstractTestServer.runsFromJar() && null != zipLoc && zipLoc.length() > 0) {
                System.out.println("Extracting server configuration from " + zipLoc);
                AbstractTestServer.setConfigDir(FileUtils.createTmpFolder("brokerConfig"));
                AbstractTestServer.extractConfiguration(zipLoc, "");
            } else { // dev execution, unpacked
                AbstractTestServer.setConfigDir(new File(unpacked)); // null is ok
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
        aTmp.add("--amqp.port=" + addr.getPort());
        //aTmp.add("--test.debug=true");
        //aTmp.add("--test.ingestCount=200");
        args = new String[aTmp.size()];
        aTmp.toArray(args);
        
        // start broker
        extractBrokerConfig("hivemqv5cfg.zip", "./src/test/hiveMqv5Cfg"); // Maven turns file names to small caps
        extractBrokerConfig("qpidcfg.zip", "./src/test/qpidCfg"); // Maven turns file names to small caps
        broker = new TestQpidServer(addr);
        broker.start();
        
        Test.main(args);
    }
    
}
