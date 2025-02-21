package test.de.iip_ecosphere.platform.examples.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseTariffNumberRestType;
import de.iip_ecosphere.platform.examples.rest.mixed.MachineInputMixed;
import de.iip_ecosphere.platform.examples.rest.mixed.MachineInputTranslatorMixed;
import de.iip_ecosphere.platform.examples.rest.mixed.MachineOutputMixed;
import de.iip_ecosphere.platform.examples.rest.mixed.MachineOutputTranslatorMixed;
import de.iip_ecosphere.platform.examples.rest.mixed.SpecificRESTConnectorMixed;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;





public class GeneratedRESTConnectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratedRESTConnectorTest.class);
    private static TestServerEEM testServer;
    
    /**
     * Creates an instance of this test.
     */
    public GeneratedRESTConnectorTest() {

    }
    
    /**
     * Sets the test up by starting an embedded REST server.
     */
    @BeforeClass
    public static void init() {
        testServer = new TestServerEEM();
        testServer.start();
        LOGGER.info("RESTConnectorTest -> REST server started");

    }

    /**
     * Shuts down the test server.
     */
    @AfterClass
    public static void shutdown() {
        LOGGER.info("RESTConnectorTest -> REST server stopped");
        testServer.stop();
    }

    /**
     * Tests the connector in polling mode.
     * 
     * @throws IOException          in case that creating the connector fails
     * @throws InterruptedException
     */
    @Test
    public void testWithPolling() throws IOException, InterruptedException {
        LOGGER.info("GeneratedRESTConnectorTest -> testWithPolling()");
        
        testGeneratedRESTConnector();

    }
    
    /**
     * Test with generated RESTConnector.
     */
    private void testGeneratedRESTConnector() {  
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        
        ConnectorParameter param = getParam();        
        RESTConnector<MachineOutputMixed, MachineInputMixed> connector = new SpecificRESTConnectorMixed(
                getProtocolAdapter());
        
        try {
            connector.connect(param);
            
            AtomicReference<MachineOutputMixed> restReference = new AtomicReference<MachineOutputMixed>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallback(restReference, count));
            
            MachineOutputMixed rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }
            
            Assert.assertNotNull(rest);
            Assert.assertEquals(3, rest.getTn1().getValue());
            Assert.assertEquals(50.000, rest.getF().getValue());
            Assert.assertEquals(229.845, rest.getU1().getValue());
            Assert.assertEquals(229.805, rest.getU2().getValue());
            Assert.assertEquals(229.853, rest.getU3().getValue());
            Assert.assertEquals(2.533, rest.getAll().getItems()[7].getValue());
            Assert.assertEquals(2.468, rest.getAll().getItems()[8].getValue());
            Assert.assertEquals(2.476, rest.getAll().getItems()[9].getValue());           
            Assert.assertEquals("Device information", rest.getInformation().getRootItems()[0].getDescription());
            Assert.assertEquals("Instantaneous values", rest.getInformation().getRootItems()[1].getDescription());
            Assert.assertEquals("EEM-MA370", rest.getInformation().getInfoItems()[0].getValue());
            Assert.assertEquals("2.0", rest.getInformation().getInfoItems()[1].getValue()); 
            
            MachineInputMixed input = new MachineInputMixed();
            TestServerResponseTariffNumberRestType tn1 = rest.getTn1();
            tn1.setValue(1);
            input.setTn1(tn1);
            connector.write(input);            

            rest = waitUntilReceived(count, restReference, rest);
            
            Assert.assertEquals(1, rest.getTn1().getValue());                        
            Assert.assertEquals("", rest.getTn2().getContext());
            Assert.assertEquals("", rest.getTn2().getId());
            Assert.assertEquals("", rest.getTn2().getTimestamp());
            Assert.assertEquals("", rest.getTn2().getName());
            Assert.assertEquals(null, rest.getTn2().getValue());
            Assert.assertEquals("", rest.getTn2().getDescription());
            
            TestServerResponseTariffNumberRestType tn2 = new TestServerResponseTariffNumberRestType();
            tn2.setContext("/api/v1/measurements/tn2");
            tn2.setId("tn2");
            tn2.setTimestamp("timestamp");
            tn2.setName("TN2");
            tn2.setValue(3);
            tn2.setDescription("Tariff Number 2");
            
            input.setTn2(tn2);
            connector.write(input);
            
            rest = waitUntilReceived(count, restReference, rest);

            Assert.assertEquals("/api/v1/measurements/tn2", rest.getTn2().getContext());
            Assert.assertEquals("tn2", rest.getTn2().getId());
            Assert.assertEquals("timestamp", rest.getTn2().getTimestamp());
            Assert.assertEquals("TN2", rest.getTn2().getName());
            Assert.assertEquals(3, rest.getTn2().getValue());
            Assert.assertEquals("Tariff Number 2", rest.getTn2().getDescription());
                   
            System.out.println("");
            LOGGER.info("GeneratedRESTConnectorTest() -> success" + "\n");
            connector.disconnect();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
         
    }
    
    /**
     * Creates and returns a ProtocolAdapter for testing.
     * 
     * @return ProtocolAdapter for testing
     */
    private ProtocolAdapter<RESTItem, Object, MachineOutputMixed, MachineInputMixed> getProtocolAdapter() {

        ProtocolAdapter<RESTItem, Object, MachineOutputMixed, MachineInputMixed> adapter = 
                new TranslatingProtocolAdapter<RESTItem, Object, MachineOutputMixed, MachineInputMixed>(
                new MachineOutputTranslatorMixed<RESTItem>(false, RESTItem.class),
                new MachineInputTranslatorMixed<Object>());

        return adapter;
    }
    
    /**
     * Creates and returns a ReceptionCallbac<MachineOutputSet> for the Connector.
     * 
     * @param restRef AtomicReference<MachineOutputSet> to set received data
     * @return ReceptionCallback<MachineOutputSet> callback
     */
    private ReceptionCallback<MachineOutputMixed> createCallback(AtomicReference<MachineOutputMixed> restRef, 
            AtomicInteger count) {

        ReceptionCallback<MachineOutputMixed> callback = new ReceptionCallback<MachineOutputMixed>() {

            @Override
            public void received(MachineOutputMixed data) {
                restRef.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<MachineOutputMixed> getType() {
                return MachineOutputMixed.class;
            }
        };

        return callback;
    }
    
    /**
     * Waits until new data are received.
     * 
     * @param count AtomicInteger
     * @param restReference AtomicReference<MachineOutputMixed>
     * @param rest MachineOutputMixed
     */
    private MachineOutputMixed waitUntilReceived(AtomicInteger count, 
            AtomicReference<MachineOutputMixed> restReference, MachineOutputMixed rest) {
        
        int currentCount = count.get();
        int targetCount = currentCount + 1;

        while (currentCount < targetCount) {
            TimeUtils.sleep(10);
            rest = restReference.get();
            currentCount = count.get();
        }
        
        return rest;
    }
    
    /**
     * Creates and returns ConnectorParameter for this test.
     * 
     * @return ConnectorParameter for this test
     */
    private ConnectorParameter getParam() {
        
        Endpoint endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/");
        String generatedEndpointDescription = testServer.getGeneratedEndpointDescription();
        
        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(endpoint);
        testParameter.setApplicationInformation("App_Id", "App_Description");
        testParameter.setEndpointPath(endpoint.toUri());
        testParameter.setSpecificSetting("SERVER_STRUCTURE", generatedEndpointDescription);
        ConnectorParameter param = testParameter.build();
        return param;
    }
}
