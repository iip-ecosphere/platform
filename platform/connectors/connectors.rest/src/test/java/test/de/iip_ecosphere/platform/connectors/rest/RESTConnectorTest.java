package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
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
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.ConnectorTest;

public class RESTConnectorTest {

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

        RESTConnector<RESTMeasurement, RESTCommand> connector = new SpecificRESTConnectorSingle(getProtocolAdapter());

        try {
            connector.connect(getConnectorParameter(RequestType.Single));
            AtomicReference<RESTMeasurement> restReference = new AtomicReference<RESTMeasurement>();
            connector.setReceptionCallback(createCallback(restReference));

            ConnectorTest.assertInstance(connector, true);
            RESTMeasurement rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World", rest.getStringValue());
            Assert.assertEquals((short) 1, rest.getShortValue());
            Assert.assertEquals((int) 1000, rest.getIntValue());
            Assert.assertEquals((long) 1000000, rest.getLongValue());
            Assert.assertTrue((float) Math.PI == rest.getFloatValue());
            Assert.assertTrue((double) Math.PI == rest.getDoubleValue());

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

        RESTConnector<RESTMeasurement, RESTCommand> connector = new SpecificRESTConnectorSingle(getProtocolAdapter());

        try {
            connector.connect(getConnectorParameter(RequestType.SingleWP));
            AtomicReference<RESTMeasurement> restReference = new AtomicReference<RESTMeasurement>();
            connector.setReceptionCallback(createCallback(restReference));

            ConnectorTest.assertInstance(connector, true);

            RESTMeasurement rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World", rest.getStringValue());
            Assert.assertEquals((short) 1, rest.getShortValue());
            Assert.assertEquals((int) 1000, rest.getIntValue());
            Assert.assertEquals((long) 1000000, rest.getLongValue());
            Assert.assertTrue((float) Math.PI == rest.getFloatValue());
            Assert.assertTrue((double) Math.PI == rest.getDoubleValue());

            System.out.println("");
            LOGGER.info("testRequestTypeSingleWP() -> success" + "\n");
            connector.disconnect();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Test with RequestType = Set.
     */
    private void testRequestTypeSet() {
        LOGGER.info("RESTConnectorTest -> testWithPolling() -> testRequestTypeSet()");

        ActiveAasBase.setNotificationMode(NotificationMode.NONE);

        RESTConnector<RESTMeasurement, RESTCommand> connector = new SpecificRESTConnectorSet(getProtocolAdapter());

        try {
            connector.connect(getConnectorParameter(RequestType.Set));
            AtomicReference<RESTMeasurement> restReference = new AtomicReference<RESTMeasurement>();
            connector.setReceptionCallback(createCallback(restReference));

            ConnectorTest.assertInstance(connector, true);

            RESTMeasurement rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World", rest.getStringValue());
            Assert.assertEquals((short) 1, rest.getShortValue());
            Assert.assertEquals((int) 1000, rest.getIntValue());
            Assert.assertEquals((long) 1000000, rest.getLongValue());
            Assert.assertTrue((float) Math.PI == rest.getFloatValue());
            Assert.assertTrue((double) Math.PI == rest.getDoubleValue());

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

        RESTConnector<RESTMeasurement, RESTCommand> connector = new SpecificRESTConnectorSet(getProtocolAdapter());

        try {
            connector.connect(getConnectorParameter(RequestType.SetWP));
            AtomicReference<RESTMeasurement> restReference = new AtomicReference<RESTMeasurement>();
            connector.setReceptionCallback(createCallback(restReference));

            ConnectorTest.assertInstance(connector, true);

            RESTMeasurement rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals("Hello World", rest.getStringValue());
            Assert.assertEquals((short) 1, rest.getShortValue());
            Assert.assertEquals((int) 1000, rest.getIntValue());
            Assert.assertEquals((long) 1000000, rest.getLongValue());
            Assert.assertTrue((float) Math.PI == rest.getFloatValue());
            Assert.assertTrue((double) Math.PI == rest.getDoubleValue());

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
    private ProtocolAdapter<RESTItem, Object, RESTMeasurement, RESTCommand> getProtocolAdapter() {

        ProtocolAdapter<RESTItem, Object, RESTMeasurement, RESTCommand> adapter = 
                new TranslatingProtocolAdapter<RESTItem, Object, RESTMeasurement, RESTCommand>(
                new RESTMeasurementOutputTranslator<RESTItem>(false, RESTItem.class),
                new RESTCommandInputTranslator<Object>(Object.class));

        return adapter;
    }

    /**
     * Creates and returns a ReceptionCallback<RESTMeasurement> for the Connector.
     * 
     * @param restRef AtomicReference<RESTMeasurement> to set received data
     * @return ReceptionCallback<RESTMeasurement> callback
     */
    private ReceptionCallback<RESTMeasurement> createCallback(AtomicReference<RESTMeasurement> restRef) {

        ReceptionCallback<RESTMeasurement> callback = new ReceptionCallback<RESTMeasurement>() {
            // connector.setReceptionCallback(new ReceptionCallback<RESTMeasurement>() {

            @Override
            public void received(RESTMeasurement data) {
                restRef.set(data);
                // count.incrementAndGet();
            }

            @Override
            public Class<RESTMeasurement> getType() {
                return RESTMeasurement.class;
            }
        };

        return callback;
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
    protected ConnectorParameter getConnectorParameter(RequestType type) {

        Endpoint endpoint = null;
        String endpoints = null;

        if (type == RequestType.Single) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/endpoints/");
            endpoints = testServer.getEndpointDescriptionSingle();
        } else if (type == RequestType.SingleWP) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/endpoints/single");
            endpoints = testServer.getEndpointDescriptionSingleWP();
        } else if (type == RequestType.Set) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/endpoints/");
            endpoints = testServer.getEndpointDescriptionSet();
        } else if (type == RequestType.SetWP) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServer/api/endpoints/set");
            endpoints = testServer.getEndpointDescriptionSetWP();
        }

        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(endpoint);
        testParameter.setApplicationInformation("App_Id", "App_Description");
        testParameter.setEndpointPath(endpoint.toUri());

        testParameter.setSpecificSetting("Endpoints", endpoints);
        testParameter.setSpecificSetting("RequestType", type);

        return testParameter.build();
    }

}
