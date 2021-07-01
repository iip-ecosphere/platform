package test.de.iip_ecosphere.platform.monitoring;

import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClient;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MeterType;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructionBundle;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Class that sets up the AAS server.<br>
 * This class will set up the AAS server and use the Metrics Constructor to
 * construct an AAS that contains the Metrics exposed by the Metrics Provider in
 * the Server side. This class is important in order to be able to run the
 * prototype tests to mimic how the library would work in a real scenario.<br>
 * The execution of the process in this class is needed to be able to execute
 * the other tests. It is assumed that the server side process is already
 * running.
 * 
 * @author Miguel Gomez
 */
public class AasServer {

    // Where the registry will be exposed
    public static final int REGISTRY_PORT_NO = 4001;

    // Identifiers for the AAS and Submodel
    public static final String AAS_URN = "urn:::prototype.four.aas";
    public static final String AAS_NAME = "proto4aas";
    public static final String SM_URN = "urn:::prototype.four.submodel";
    public static final String SM_NAME = "proto4sm";

    // Base Properties
    public static final String PROP_NAME = "name";
    public static final String PROP_VERSION = "version";
    public static final String PROP_DESCRIPTION = "description";

    // Custom property names for the test
    public static final String SUPPLIER_TIMER_ID = "suppliercustomtimer";
    public static final String SUPPLIER_GAUGE_ID = "suppliercustomgauge";
    public static final String SUPPLIER_COUNTER_ID = "suppliercustomcounter";

    public static final String CONSUMER_TIMER_ID = "consumercustomtimer";
    public static final String CONSUMER_GAUGE_ID = "consumercustomgauge";
    public static final String CONSUMER_COUNTER_ID = "consumercustomcounter";
    public static final String CONSUMER_RECV_ID = "consumerreceptiongauge";

    public static final String REST_GAUGE_ID = "restgauge";
    public static final String REST_COUNTER_ID = "restcounter";
    public static final String REST_TIMER_ID = "resttimer";

    // This identifies the host and port where the metrics are exposed
    private static final String SERVICE_HOST = "localhost";
    private static final int SERVICE_PORT = 8080;

    private static final String TIMEOUT_MSG = "Timeout! Shuttimg down the servers";

    // checkstyle: stop exception type check
    
    /**
     * Sets up and starts the AAS server.<br>
     * The AAS server and protocol server are set up using the Metrics Constructor
     * to help add the metrics, all of them mapped as properties. This process also
     * adds a few custom metrics. To be tested out.<br>
     * The server side must be running in order for this process to work, otherwise,
     * the AAS will crash when we attempt to use it.<br>
     * The AAS will automatically stop after running for 2 minutes.
     * 
     * @param args command line arguments, never used
     * @throws Exception if the test fails
     */
    public static void main(String[] args) throws Exception {
        // Create the addresses and the endpoints
        ServerAddress aasServerAddress = new ServerAddress(Schema.HTTP, REGISTRY_PORT_NO);
        ServerAddress vabServerAddress = new ServerAddress(Schema.HTTP);
        Endpoint aasServerBase = new Endpoint(aasServerAddress, "");
        Endpoint registry = new Endpoint(aasServerAddress, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);
        System.out.println("Endpoints created");

        // Create the builders and put them in a bundle
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(AAS_NAME, AAS_URN);
        InvocablesCreator iCreator = factory.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL,
                vabServerAddress.getHost(), vabServerAddress.getPort());
        SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(SM_NAME, SM_URN);
        ProtocolServerBuilder pBuilder = AasFactory.getInstance()
                .createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, vabServerAddress.getPort());
        MetricsExtractorRestClient client = new MetricsExtractorRestClient(SERVICE_HOST, SERVICE_PORT);

        MetricsAasConstructionBundle bundle = new MetricsAasConstructionBundle(smBuilder, pBuilder, iCreator, client);
        System.out.println("Bundle created");

        // Create base properties
        smBuilder.createPropertyBuilder(PROP_NAME).setType(Type.STRING)
                .bind(iCreator.createGetter(PROP_NAME), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(PROP_VERSION).setType(Type.STRING)
                .bind(iCreator.createGetter(PROP_VERSION), InvocablesCreator.READ_ONLY).build();
        smBuilder.createPropertyBuilder(PROP_DESCRIPTION).setType(Type.STRING)
                .bind(iCreator.createGetter(PROP_DESCRIPTION), InvocablesCreator.READ_ONLY).build();

        pBuilder.defineProperty(PROP_NAME, () -> "My service", null);
        pBuilder.defineProperty(PROP_VERSION, () -> "1.2.3", null);
        pBuilder.defineProperty(PROP_DESCRIPTION, () -> "Prototype 4 AAS", null);

        // We add the metrics and custom metrics to the submodel
        MetricsAasConstructor.addMetricsToBundle(bundle);
        MetricsAasConstructor.addCustomMetric(bundle, SUPPLIER_TIMER_ID, MeterType.TIMER);
        MetricsAasConstructor.addCustomMetric(bundle, SUPPLIER_GAUGE_ID, MeterType.GAUGE);
        MetricsAasConstructor.addCustomMetric(bundle, SUPPLIER_COUNTER_ID, MeterType.COUNTER);
        MetricsAasConstructor.addCustomMetric(bundle, CONSUMER_TIMER_ID, MeterType.TIMER);
        MetricsAasConstructor.addCustomMetric(bundle, CONSUMER_GAUGE_ID, MeterType.GAUGE);
        MetricsAasConstructor.addCustomMetric(bundle, CONSUMER_COUNTER_ID, MeterType.COUNTER);
        MetricsAasConstructor.addCustomMetric(bundle, CONSUMER_RECV_ID, MeterType.GAUGE);
        MetricsAasConstructor.addCustomMetric(bundle, REST_TIMER_ID, MeterType.TIMER);
        MetricsAasConstructor.addCustomMetric(bundle, REST_GAUGE_ID, MeterType.GAUGE);
        MetricsAasConstructor.addCustomMetric(bundle, REST_COUNTER_ID, MeterType.COUNTER);

        illegalMetricsTests(bundle);

        System.out.println("Metrics added");

        // Now that all has been added, we build the submodel, the aas and deploy
        smBuilder.build();
        Aas aas = aasBuilder.build();
        Server aasServer = pBuilder.build();
        aasServer.start();
        System.out.println("AAS server started");

        Server httpServer = factory.createDeploymentRecipe(aasServerBase).addInMemoryRegistry(registry.getEndpoint())
                .deploy(aas).createServer();
        httpServer.start();
        System.out.println("HTTP server started");

        System.out.println(
                "Registry can be found on address: " + aasServerAddress.toServerUri() + registry.getEndpoint());

        Thread.sleep(120000); // 2 mins
        System.out.println(TIMEOUT_MSG);
        httpServer.stop(true);
        aasServer.stop(true);
    }

    /**
     * We are going to attempt some illegal operations expecting the exception to be thrown.
     * 
     * @param bundle the construction bundle
     * @throws Exception exception through metrics operations
     */
    private static void illegalMetricsTests(MetricsAasConstructionBundle bundle) throws Exception {
        try {
            MetricsAasConstructor.addCustomMetric(bundle, null, MeterType.COUNTER);
            throw new Exception("No exception was thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            MetricsAasConstructor.addCustomMetric(bundle, "", MeterType.COUNTER);
            throw new Exception("No exception was thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            MetricsAasConstructor.addCustomMetric(bundle, "invalid", null);
            throw new Exception("No exception was thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            MetricsAasConstructor.addCustomMetric(null, "invalid", MeterType.COUNTER);
            throw new Exception("No exception was thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            MetricsAasConstructor.addMetricsToBundle(null);
            throw new Exception("No exception was thrown");
        } catch (IllegalArgumentException iae) {
        }
    }

    // resume: stop exception type check
    
}
