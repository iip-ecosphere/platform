package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorExtensionDescriptor.DefaultConnectorExtension;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;

public class RESTConnectorTest {

    private static final String CONN_SET_ID = "set";
    private static final String CONN_SINGLE_ID = "single";
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTConnectorTest.class);
    private static TestServer testServer;

    /**
     * Creates an instance of this test.
     */
    public RESTConnectorTest() {

    }

    /**
     * Sets the test up by starting an embedded REST server.
     */
    @BeforeClass
    public static void init() {
        testServer = new TestServer();
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

        testRequestTypeSingle();
        testRequestTypeSingleWP();
        testRequestTypeSet();
        testRequestTypeSetWP();

    }

    /**
     * Test with RequestType = Single.
     */
    private void testRequestTypeSingle() {
        LOGGER.info("RESTConnectorTest -> testWithPolling() -> testRequestTypeSingle()");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);

        RESTConnector<MachineOutputSingle, MachineInputSingle> connector = new RESTConnector<>(
                getProtocolAdapterSingle());
        connector.setInstanceIdentification(CONN_SINGLE_ID);

        try {
            connector.connect(getConnectorParameter("single"));
            AtomicReference<MachineOutputSingle> restReference = new AtomicReference<MachineOutputSingle>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackSingle(restReference, count));

            ConnectorTest.assertInstance(connector, true);
            MachineOutputSingle rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World!", rest.getStringValue().getValue());
            Assert.assertEquals(1, rest.getShortValue().getValue());
            Assert.assertEquals(100, rest.getIntegerValue().getValue());
            Assert.assertEquals(10000, rest.getLongValue().getValue());

            float diffFloat = ((float) Math.PI) - ((Number) rest.getFloatValue().getValue()).floatValue();
            Assert.assertTrue(diffFloat < 0.001);

            double diffDouble = ((double) Math.PI) - ((double) rest.getDoubleValue().getValue());
            Assert.assertTrue(diffDouble < 0.001);

            MachineInputSingle input = new MachineInputSingle();
            input.setStringValue("New String Value");
            connector.write(input);

            int currentCount = count.get();
            int targetCount = currentCount + 1;

            while (currentCount < targetCount) {
                TimeUtils.sleep(10);
                rest = restReference.get();
                currentCount = count.get();
            }

            Assert.assertEquals("New String Value", rest.getStringValue().getValue());

            System.out.println("");
            LOGGER.info("testRequestTypeSingle() -> success" + "\n");
            connector.disconnect();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Test with RequestType = SingleWP.
     */
    private void testRequestTypeSingleWP() {
        LOGGER.info("RESTConnectorTest -> testWithPolling() -> testRequestTypeSingleWP()");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);

        RESTConnector<MachineOutputSingle, MachineInputSingle> connector = new RESTConnector<>(
                getProtocolAdapterSingle());
        connector.setInstanceIdentification(CONN_SINGLE_ID);

        try {
            connector.connect(getConnectorParameter("singleWP"));
            AtomicReference<MachineOutputSingle> restReference = new AtomicReference<MachineOutputSingle>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackSingle(restReference, count));

            ConnectorTest.assertInstance(connector, true);

            MachineOutputSingle rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("New String Value", rest.getStringValue().getValue());
            Assert.assertEquals(1, rest.getShortValue().getValue());
            Assert.assertEquals(100, rest.getIntegerValue().getValue());
            Assert.assertEquals(10000, rest.getLongValue().getValue());

            float diffFloat = ((float) Math.PI) - ((Number) rest.getFloatValue().getValue()).floatValue();
            Assert.assertTrue(diffFloat < 0.001);

            double diffDouble = ((double) Math.PI) - ((double) rest.getDoubleValue().getValue());
            Assert.assertTrue(diffDouble < 0.001);

            MachineInputSingle input = new MachineInputSingle();
            input.setStringValue("Hello World!");
            connector.write(input);

            int currentCount = count.get();
            int targetCount = currentCount + 1;

            while (currentCount < targetCount) {
                TimeUtils.sleep(5);
                rest = restReference.get();
                currentCount = count.get();
            }

            Assert.assertEquals("Hello World!", rest.getStringValue().getValue());

            System.out.println("");
            LOGGER.info("testRequestTypeSingleWP() -> success" + "\n");
            connector.disconnect();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    
    
    /**
     * Connector extension providing the response classes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class SetTestConnectorExtension extends DefaultConnectorExtension<Class<?>[]> {

        /**
         * Creates an instance. [JSL]
         */
        public SetTestConnectorExtension() {
            super(CONN_SET_ID, () -> new Class[]{TestServerResponseSetRestType.class});
        }
        
    }

    /**
     * Connector extension providing the response classes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class SingleTestConnectorExtension extends DefaultConnectorExtension<Class<?>[]> {

        /**
         * Creates an instance. [JSL]
         */
        public SingleTestConnectorExtension() {
            super(CONN_SINGLE_ID, () -> new Class[]{TestServerResponsSingleRestType.class});
        }
        
    }

    /**
     * Test with RequestType = Set.
     */
    private void testRequestTypeSet() {
        LOGGER.info("RESTConnectorTest -> testWithPolling() -> testRequestTypeSet()");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);

        RESTConnector<MachineOutputSet, MachineInputSet> connector = 
                new RESTConnector<>(getProtocolAdapterSet());
        connector.setInstanceIdentification(CONN_SET_ID);

        try {
            connector.connect(getConnectorParameter("set"));
            AtomicReference<MachineOutputSet> restReference = new AtomicReference<MachineOutputSet>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackSet(restReference, count));

            ConnectorTest.assertInstance(connector, true);

            MachineOutputSet rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World!", rest.getStringValue().getValue());
            Assert.assertEquals(1, rest.getShortValue().getValue());
            Assert.assertEquals(100, rest.getIntegerValue().getValue());
            Assert.assertEquals(10000, rest.getLongValue().getValue());

            float diffFloat = ((float) Math.PI) - ((Number) rest.getFloatValue().getValue()).floatValue();
            Assert.assertTrue(diffFloat < 0.001);

            double diffDouble = ((double) Math.PI) - ((double) rest.getDoubleValue().getValue());
            Assert.assertTrue(diffDouble < 0.001);

            System.out.println("");
            LOGGER.info("testRequestTypeSet() -> success" + "\n");
            connector.disconnect();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Test with RequestType = SetWP.
     */
    private void testRequestTypeSetWP() {
        LOGGER.info("RESTConnectorTest -> testWithPolling() -> testRequestTypeSetWP()");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);

        RESTConnector<MachineOutputSet, MachineInputSet> connector = 
                new RESTConnector<>(getProtocolAdapterSet());
        connector.setInstanceIdentification(CONN_SET_ID);

        try {
            connector.connect(getConnectorParameter("setWP"));
            AtomicReference<MachineOutputSet> restReference = new AtomicReference<MachineOutputSet>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackSet(restReference, count));

            ConnectorTest.assertInstance(connector, true);

            MachineOutputSet rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World!", rest.getStringValue().getValue());
            Assert.assertEquals(1, rest.getShortValue().getValue());
            Assert.assertEquals(100, rest.getIntegerValue().getValue());
            Assert.assertEquals(10000, rest.getLongValue().getValue());

            float diffFloat = ((float) Math.PI) - ((Number) rest.getFloatValue().getValue()).floatValue();
            Assert.assertTrue(diffFloat < 0.001);

            double diffDouble = ((double) Math.PI) - ((double) rest.getDoubleValue().getValue());
            Assert.assertTrue(diffDouble < 0.001);

            System.out.println("");
            LOGGER.info("testRequestTypeSetWP() -> success" + "\n");
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
    private ProtocolAdapter<RESTItem, Object, MachineOutputSingle, MachineInputSingle> getProtocolAdapterSingle() {

        ProtocolAdapter<RESTItem, Object, MachineOutputSingle, MachineInputSingle> adapter = 
                new TranslatingProtocolAdapter<RESTItem, Object, MachineOutputSingle, MachineInputSingle>(
                new MachineOutputTranslatorSingle<RESTItem>(false, RESTItem.class),
                new MachineInputTranslatorSingle<Object>());

        return adapter;
    }

    /**
     * Creates and returns a ProtocolAdapter for testing.
     * 
     * @return ProtocolAdapter for testing
     */
    private ProtocolAdapter<RESTItem, Object, MachineOutputSet, MachineInputSet> getProtocolAdapterSet() {

        ProtocolAdapter<RESTItem, Object, MachineOutputSet, MachineInputSet> adapter = 
                new TranslatingProtocolAdapter<RESTItem, Object, MachineOutputSet, MachineInputSet>(
                new MachineOutputTranslatorSet<RESTItem>(false, RESTItem.class), 
                new MachineInputTranslatorSet<Object>());

        return adapter;
    }

    /**
     * Creates and returns a ReceptionCallback&lt;MachineOutputSingle&gt; for the
     * Connector.
     * 
     * @param restRef AtomicReference&lt;MachineOutputSingle&gt; to set received data
     * @return ReceptionCallback&lt;MachineOutputSingle&gt; callback
     */
    private ReceptionCallback<MachineOutputSingle> createCallbackSingle(AtomicReference<MachineOutputSingle> restRef,
            AtomicInteger count) {

        ReceptionCallback<MachineOutputSingle> callback = new ReceptionCallback<MachineOutputSingle>() {

            @Override
            public void received(MachineOutputSingle data) {
                restRef.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<MachineOutputSingle> getType() {
                return MachineOutputSingle.class;
            }
        };

        return callback;
    }

    /**
     * Creates and returns a ReceptionCallback&lt;MachineOutputSet&gt; for the Connector.
     * 
     * @param restRef AtomicReference&lt;MachineOutputSet&gt; to set received data
     * @return ReceptionCallback&lt;MachineOutputSet&gt; callback
     */
    private ReceptionCallback<MachineOutputSet> createCallbackSet(AtomicReference<MachineOutputSet> restRef,
            AtomicInteger count) {

        ReceptionCallback<MachineOutputSet> callback = new ReceptionCallback<MachineOutputSet>() {

            @Override
            public void received(MachineOutputSet data) {
                restRef.set(data);
                count.incrementAndGet();
            }

            @Override
            public Class<MachineOutputSet> getType() {
                return MachineOutputSet.class;
            }
        };

        return callback;
    }

    /**
     * Returns the connector descriptor.
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
            endpoints = testServer.getEndpointDescriptionSingle();
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/");
        } else if (type.equals("singleWP")) {
            endpoints = testServer.getEndpointDescriptionSingleWP();
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/");
        } else if (type.equals("set")) {
            endpoints = testServer.getEndpointDescriptionSet();
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/");
        } else if (type.equals("setWP")) {
            endpoints = testServer.getEndpointDescriptionSetWP();
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/");
        }

        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(endpoint);
        testParameter.setApplicationInformation("App_Id", "App_Description");
        testParameter.setEndpointPath(endpoint.toUri());
        testParameter.setSpecificSetting("SERVER_STRUCTURE", endpoints);

        return testParameter.build();
    }

}
