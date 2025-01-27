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
import de.iip_ecosphere.platform.examples.rest.set.MachineInputSet;
import de.iip_ecosphere.platform.examples.rest.set.MachineInputTranslatorSet;
import de.iip_ecosphere.platform.examples.rest.set.MachineOutputSet;
import de.iip_ecosphere.platform.examples.rest.set.MachineOutputTranslatorSet;
import de.iip_ecosphere.platform.examples.rest.set.SpecificRESTConnectorSet;
import de.iip_ecosphere.platform.examples.rest.single.MachineInputSingle;
import de.iip_ecosphere.platform.examples.rest.single.MachineInputTranslatorSingle;
import de.iip_ecosphere.platform.examples.rest.single.MachineOutputSingle;
import de.iip_ecosphere.platform.examples.rest.single.MachineOutputTranslatorSingle;
import de.iip_ecosphere.platform.examples.rest.single.SpecificRESTConnectorSingle;
import de.iip_ecosphere.platform.examples.rest.single.TestServerResponsSingleTN;
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

        RESTConnector<MachineOutputSingle, MachineInputSingle> connector = new SpecificRESTConnectorSingle(
                getProtocolAdapterSingle());
        try {
            connector.connect(getConnectorParameter("single"));
            AtomicReference<MachineOutputSingle> restReference = new AtomicReference<MachineOutputSingle>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackSingle(restReference, count));

            MachineOutputSingle rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals(50.000, rest.getF().getValue());
            Assert.assertEquals(229.845, rest.getU1().getValue());
            Assert.assertEquals(229.805, rest.getU2().getValue());
            Assert.assertEquals(229.853, rest.getU3().getValue());
            Assert.assertEquals(398.237, rest.getU12().getValue());
            Assert.assertEquals(398.078, rest.getU23().getValue());
            Assert.assertEquals(398.279, rest.getU31().getValue());
            Assert.assertEquals(2.533, rest.getI1().getValue());
            Assert.assertEquals(2.468, rest.getI2().getValue());
            Assert.assertEquals(2.476, rest.getI3().getValue());
            Assert.assertEquals(3, rest.getTn().getValue());
            
            MachineInputSingle input = new MachineInputSingle();
            
            TestServerResponsSingleTN tn = new TestServerResponsSingleTN();
            tn.setContext("/api/v1/measurements/tn");
            tn.setId("tn");
            tn.setTimestamp("timestamp");
            tn.setName("TN");
            tn.setValue(1);
            tn.setDescription("Tariff Number");
            
            input.setTn(tn);
            connector.write(input);

            int currentCount = count.get();
            int targetCount = currentCount + 1;

            while (currentCount < targetCount) {
                TimeUtils.sleep(10);
                rest = restReference.get();
                currentCount = count.get();
            }

            Assert.assertEquals(1, rest.getTn().getValue());


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

        RESTConnector<MachineOutputSingle, MachineInputSingle> connector = new SpecificRESTConnectorSingle(
                getProtocolAdapterSingle());
        try {
            connector.connect(getConnectorParameter("singleWP"));
            AtomicReference<MachineOutputSingle> restReference = new AtomicReference<MachineOutputSingle>();
            AtomicInteger count = new AtomicInteger(0);
            connector.setReceptionCallback(createCallbackSingle(restReference, count));

            MachineOutputSingle rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals(50.000, rest.getF().getValue());
            Assert.assertEquals(229.845, rest.getU1().getValue());
            Assert.assertEquals(229.805, rest.getU2().getValue());
            Assert.assertEquals(229.853, rest.getU3().getValue());
            Assert.assertEquals(398.237, rest.getU12().getValue());
            Assert.assertEquals(398.078, rest.getU23().getValue());
            Assert.assertEquals(398.279, rest.getU31().getValue());
            Assert.assertEquals(2.533, rest.getI1().getValue());
            Assert.assertEquals(2.468, rest.getI2().getValue());
            Assert.assertEquals(2.476, rest.getI3().getValue());

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

        RESTConnector<MachineOutputSet, MachineInputSet> connector = 
        		new SpecificRESTConnectorSet(getProtocolAdapterSet());

        try {
            connector.connect(getConnectorParameter("set"));
            AtomicReference<MachineOutputSet> restReference = new AtomicReference<MachineOutputSet>();
            connector.setReceptionCallback(createCallbackSet(restReference));

            MachineOutputSet rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals(50.000, rest.getF().getValue());
            Assert.assertEquals(229.845, rest.getU1().getValue());
            Assert.assertEquals(229.805, rest.getU2().getValue());
            Assert.assertEquals(229.853, rest.getU3().getValue());
            Assert.assertEquals(398.237, rest.getU12().getValue());
            Assert.assertEquals(398.078, rest.getU23().getValue());
            Assert.assertEquals(398.279, rest.getU31().getValue());
            Assert.assertEquals(2.533, rest.getI1().getValue());
            Assert.assertEquals(2.468, rest.getI2().getValue());
            Assert.assertEquals(2.476, rest.getI3().getValue());

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
        		new SpecificRESTConnectorSet(getProtocolAdapterSet());

        try {
            connector.connect(getConnectorParameter("setWP"));
            AtomicReference<MachineOutputSet> restReference = new AtomicReference<MachineOutputSet>();
            connector.setReceptionCallback(createCallbackSet(restReference));

            MachineOutputSet rest = restReference.get();

            while (rest == null) {
                TimeUtils.sleep(10);
                rest = restReference.get();
            }

            Assert.assertNotNull(rest);
            Assert.assertEquals(50.000, rest.getF().getValue());
            Assert.assertEquals(229.845, rest.getU1().getValue());
            Assert.assertEquals(229.805, rest.getU2().getValue());
            Assert.assertEquals(229.853, rest.getU3().getValue());
            Assert.assertEquals(398.237, rest.getU12().getValue());
            Assert.assertEquals(398.078, rest.getU23().getValue());
            Assert.assertEquals(398.279, rest.getU31().getValue());
            Assert.assertEquals(2.533, rest.getI1().getValue());
            Assert.assertEquals(2.468, rest.getI2().getValue());
            Assert.assertEquals(2.476, rest.getI3().getValue());

            System.out.println("");
            LOGGER.info("testRequestTypeSetWP() -> success" + "\n");
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
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/endpoints/");
            endpoints = testServer.getEndpointDescriptionSingle();
        } else if (type.equals("singleWP")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/endpoints/single");
            endpoints = testServer.getEndpointDescriptionSingleWP();
        } else if (type.equals("set")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/endpoints/");
            endpoints = testServer.getEndpointDescriptionSet();
        } else if (type.equals("setWP")) {
            endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/endpoints/set");
            endpoints = testServer.getEndpointDescriptionSetWP();
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
     * Creates and returns a ReceptionCallbac<MachineOutputSingle> for the
     * Connector.
     * 
     * @param restRef AtomicReference<MachineOutputSingle> to set received data
     * @return ReceptionCallback<MachineOutputSingle> callback
     */
    private ReceptionCallback<MachineOutputSingle> createCallbackSingle(AtomicReference<MachineOutputSingle> restRef,  AtomicInteger count) {

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
     * Creates and returns a ReceptionCallbac<MachineOutputSet> for the Connector.
     * 
     * @param restRef AtomicReference<MachineOutputSet> to set received data
     * @return ReceptionCallback<MachineOutputSet> callback
     */
    private ReceptionCallback<MachineOutputSet> createCallbackSet(AtomicReference<MachineOutputSet> restRef) {

        ReceptionCallback<MachineOutputSet> callback = new ReceptionCallback<MachineOutputSet>() {

            @Override
            public void received(MachineOutputSet data) {
                restRef.set(data);
            }

            @Override
            public Class<MachineOutputSet> getType() {
                return MachineOutputSet.class;
            }
        };

        return callback;
    }

}
