package de.iip_ecosphere.platform.examples.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.RestPhoenixEEMMixed;
import iip.datatypes.RestPhoenixEEMResponseTariffNumber;
import iip.datatypes.RestPhoenixEEMResponseTariffNumberImpl;
import iip.datatypes.RestPhoenixRwEEMMixed;
import iip.datatypes.RestPhoenixRwEEMMixedImpl;


public class GeneratedConnector {
    
    private static TestServerEEM server;

    /**
     * Runs the generated connector.
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the server cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        test();
    }
    
    /**
     * Try to receive Data from the connected machine.
     */
    private static void test() throws IOException {
        server = new TestServerEEM();
        server.start();
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicReference<RestPhoenixEEMMixed> restReference = new AtomicReference<RestPhoenixEEMMixed>();
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<RestPhoenixEEMMixed> cb = new ReceptionCallback<>() {

            @Override
            public void received(RestPhoenixEEMMixed data) {
                count.incrementAndGet();
                restReference.set(data);
                System.out.println("RECEIVED (" + count.get() + "): " + data);
                
            }

            @Override
            public Class<RestPhoenixEEMMixed> getType() {
                return RestPhoenixEEMMixed.class;
            }

        };
        
        Connector<Object, Object, RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> conn = createPlatformConnector(cb);
        conn.request(true);       
        RestPhoenixEEMMixed rest = restReference.get();        
        while (rest == null) {
            TimeUtils.sleep(10);
            rest = restReference.get();
        }
        
        assertNotNull(rest);
        assertEquals(3, rest.getTn1().getValue());
        assertEquals(null, rest.getTn2().getValue());
        assertEquals(50.000, rest.getF().getValue());
        assertEquals(229.845, rest.getU1().getValue());
        assertEquals(229.805, rest.getU2().getValue());
        assertEquals(229.853, rest.getU3().getValue());
        assertEquals(2.533, rest.getAll().getItems()[7].getValue());
        assertEquals(2.468, rest.getAll().getItems()[8].getValue());
        assertEquals(2.476, rest.getAll().getItems()[9].getValue());           
        assertEquals("Device information", rest.getInformation().getRootItems()[0].getDescription());
        assertEquals("Instantaneous values", rest.getInformation().getRootItems()[1].getDescription());
        assertEquals("EEM-MA370", rest.getInformation().getInfoItems()[0].getValue());
        assertEquals("2.0", rest.getInformation().getInfoItems()[1].getValue()); 

        RestPhoenixRwEEMMixed input = new RestPhoenixRwEEMMixedImpl();
        RestPhoenixEEMResponseTariffNumber tn1 =  rest.getTn1();
        tn1.setValue(1);
        input.setTn1(tn1);
        
        RestPhoenixEEMResponseTariffNumber tn2 = new RestPhoenixEEMResponseTariffNumberImpl();
        tn2.setContext("/api/v1/measurements/tn2");
        tn2.setId("tn2");
        tn2.setTimestamp("timestamp");
        tn2.setName("TN2");
        tn2.setValue(1);
        tn2.setDescription("Tariff Number 2");
        
        input.setTn2(tn2);
        
        conn.write(input);            
        conn.request(true);
        rest = restReference.get();

        assertEquals(1, rest.getTn1().getValue());    
        assertEquals("/api/v1/measurements/tn2", rest.getTn2().getContext());
        assertEquals("tn2", rest.getTn2().getId());
        assertEquals("timestamp", rest.getTn2().getTimestamp());
        assertEquals("TN2", rest.getTn2().getName());
        assertEquals(1, rest.getTn2().getValue());
        assertEquals("Tariff Number 2", rest.getTn2().getDescription());

        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);
        server.stop();
    }
    
    /**
     * Creates the platform connector to be tested.
     * 
     * @param callback the callback
     * @return the connector instance
     * @throws IOException if creating the connector fails
     */
    public static Connector<Object, Object, RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> createPlatformConnector(
        ReceptionCallback<RestPhoenixEEMMixed> callback) throws IOException {
        ConnectorParameter param = ConnectorFactory.createConnectorParameter("myRestConnEEMMixed");
        Connector<Object, Object, RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> conn = ConnectorFactory.createConnector(
            "myRestConnEEMMixed", param, Object.class, Object.class, RestPhoenixEEMMixed.class, 
            RestPhoenixRwEEMMixed.class);
        // see iip.nodes.MyRestConnEEMMixedExampleExtension.java
        conn.setInstanceIdentification("myRestConnEEMMixed example"); 
        conn.connect(adjustConnectorParameter(param));
        conn.setReceptionCallback(callback);
        return conn;
    }
    
    /**
     * Adjust the ConnectorParameter.
     * 
     * @param param ConnectorParameter to adjust
     * @return
     */
    public static ConnectorParameter adjustConnectorParameter(ConnectorParameter param) {
        
        Endpoint endpoint = new Endpoint(Schema.HTTP, "localhost", 8080, "TestServerEEM/api/");
        //Endpoint endpoint = new Endpoint(Schema.HTTP, "192.168.1.140/rpc/");
        
        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(param);
        testParameter.setEndpointPath(endpoint.toUri());
        //testParameter.setEndpointPath("http:/" + endpoint.getEndpoint());
        return testParameter.build();
        
    }

    /**
     * Poor man's assert. Original was intertwined between test and non-test classes.
     * 
     * @param expected the expected value
     * @param value the actual value
     */
    public static void assertNotNull(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
    }
    
    /**
     * Poor man's assert. Original was intertwined between test and non-test classes.
     * 
     * @param expected the expected value
     * @param value the actual value
     */
    public static void assertEquals(Object expected, Object value) {
        if (expected == null) {
            if (value != null) {
                throw new IllegalArgumentException("Values do not match: " + expected + " " + value);
            }
        } else {
            if (expected instanceof Double && value instanceof Double) {
                double eDbl = (Double) expected;
                double vDbl = (Double) value;
                if (Math.abs(eDbl - vDbl) > 0.01) {
                    throw new IllegalArgumentException("Values differ more than 0.01: " + expected + " " + value);
                }
            }
            if (expected instanceof Float && value instanceof Float) {
                float eFlt = (Float) expected;
                float vFlt = (Float) value;
                if (Math.abs(eFlt - vFlt) > 0.01) {
                    throw new IllegalArgumentException("Values differ more than 0.01: " + expected + " " + value);
                }
            }
            if (!expected.equals(value)) {
                throw new IllegalArgumentException("Values do not match: " + expected + " " + value);
            }
        }
    }

}
