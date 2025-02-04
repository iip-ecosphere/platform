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

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.examples.rest.TestServerResponsTariffNumber;
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

public class RESTConnectorTestEEM {

    private static final Logger LOGGER = LoggerFactory.getLogger(RESTConnectorTestEEM.class);
    private static TestServerEEM testServer;

    /**
     * Creates an instance of this test.
     */
    public RESTConnectorTestEEM() {

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
        LOGGER.info("RESTConnectorTest -> testWithPolling()");
        
        testRequestTypeMixed();

    }

    /**
     * Test with RequestType = Mixed.
     */
    private void testRequestTypeMixed() {
        LOGGER.info("RESTConnectorTest -> testWithPolling() -> testRequestTypeMixed()");
        ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        
        RESTConnector<MachineOutputMixed, MachineInputMixed> connector = new SpecificRESTConnectorMixed(
                getProtocolAdapterMixed());
        
        try {
            connector.connect(getConnectorParameter("mixed"));
            
            AtomicReference<MachineOutputMixed> restReference = new AtomicReference<MachineOutputMixed>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackMixed(restReference, count));
            
                  
            MachineOutputMixed rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }
            
            Assert.assertNotNull(rest);
            Assert.assertEquals(3, rest.getTn().getValueToWrite());
            Assert.assertEquals(50.000, rest.getF().getValueToWrite());
            Assert.assertEquals(229.845, rest.getU1().getValueToWrite());
            Assert.assertEquals(229.805, rest.getU2().getValueToWrite());
            Assert.assertEquals(229.853, rest.getU3().getValueToWrite());
            Assert.assertEquals(2.533, rest.getI1().getValue());
            Assert.assertEquals(2.468, rest.getI2().getValue());
            Assert.assertEquals(2.476, rest.getI3().getValue());
            
            Assert.assertEquals("Device information", rest.getRoot1().getDescription());
            Assert.assertEquals("Instantaneous values", rest.getRoot2().getDescription());
            Assert.assertEquals("EEM-MA370", rest.getInfo1().getValue());
            Assert.assertEquals("2.0", rest.getInfo2().getValue());
            
            
            MachineInputMixed input = new MachineInputMixed();

            TestServerResponsTariffNumber tn = rest.getTn();
            tn.setValue(1);
            input.setTn(tn);
            connector.write(input);
            
            int currentCount = count.get();
            int targetCount = currentCount + 1;

            while (currentCount < targetCount) {
                TimeUtils.sleep(10);
                rest = restReference.get();
                currentCount = count.get();
            }

            Assert.assertEquals(1, rest.getTn().getValueToWrite());
 
            System.out.println("");
            LOGGER.info("testRequestTypeMixed() -> success" + "\n");
            connector.disconnect();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the connector descriptor for
     * {@link #createConnector(ProtocolAdapter)}.
     * 
     * @return the connector descriptor
     */
    protected Class<? extends ConnectorDescriptor> getConnectorDescriptor() {
        return RESTConnector.Descriptor.class;
    }

    /**
     * Returns the connector parameters for
     * {@link Connector#connect(ConnectorParameter)}.
     * 
     * @return the connector parameters
     */
    protected ConnectorParameter getConnectorParameter(String type) {

        Endpoint endpoint = null;
        String endpoints = null;

        if (type.equals("single")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/");
            endpoints = testServer.getEndpointDescriptionSingle();
        } else if (type.equals("singleWP")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/");
            endpoints = testServer.getEndpointDescriptionSingleWP();
        } else if (type.equals("set")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/measurements/");
            endpoints = testServer.getEndpointDescriptionSet();
        } else if (type.equals("setWP")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/measurements/set");
            endpoints = testServer.getEndpointDescriptionSetWP();
        } else if (type.equals("mixed")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/");
            endpoints = testServer.getEndpointDescriptionMixed();
        }

        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(endpoint);
        testParameter.setApplicationInformation("App_Id", "App_Description");
        testParameter.setEndpointPath(endpoint.toUri());

        testParameter.setSpecificSetting("Endpoints", endpoints);
        testParameter.setSpecificSetting("RequestType", type);

        return testParameter.build();
    }
    
    /**
     * Creates and returns a ProtocolAdapter for testing.
     * 
     * @return ProtocolAdapter for testing
     */
    private ProtocolAdapter<RESTItem, Object, MachineOutputMixed, MachineInputMixed> getProtocolAdapterMixed() {

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
    private ReceptionCallback<MachineOutputMixed> createCallbackMixed(AtomicReference<MachineOutputMixed> restRef, 
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
}
