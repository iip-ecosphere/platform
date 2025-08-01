package de.iip_ecosphere.platform.examples.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.examples.rest.mixed.MachineInputMixed;
import de.iip_ecosphere.platform.examples.rest.mixed.MachineOutputMixed;
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
import iip.nodes.MyRestConnEEMMixedExample;
import test.de.iip_ecosphere.platform.examples.rest.TestServerEEM;


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
        
        RESTConnector<RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> conn = createPlatformConnector(cb);
        conn.request(true);       
        RestPhoenixEEMMixed rest = restReference.get();        
        while (rest == null) {
            TimeUtils.sleep(10);
            rest = restReference.get();
        }
        
        Assert.assertNotNull(rest);
        Assert.assertEquals(3, rest.getTn1().getValue());
        Assert.assertEquals(null, rest.getTn2().getValue());
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

        Assert.assertEquals(1, rest.getTn1().getValue());    
        Assert.assertEquals("/api/v1/measurements/tn2", rest.getTn2().getContext());
        Assert.assertEquals("tn2", rest.getTn2().getId());
        Assert.assertEquals("timestamp", rest.getTn2().getTimestamp());
        Assert.assertEquals("TN2", rest.getTn2().getName());
        Assert.assertEquals(1, rest.getTn2().getValue());
        Assert.assertEquals("Tariff Number 2", rest.getTn2().getDescription());

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
    @SuppressWarnings("unchecked")
    public static RESTConnector<RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> createPlatformConnector(
            ReceptionCallback<RestPhoenixEEMMixed> callback) throws IOException {
        TranslatingProtocolAdapter<RESTItem, Object, RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> adapter = 
                MyRestConnEEMMixedExample.createConnectorAdapter();
        
        RESTConnector<RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> conn = new RESTConnector<>(adapter);
        // see iip.nodes.MyRestConnEEMMixedExampleExtension.java
        conn.setInstanceIdentification("myRestConnEEMMixed example"); 
        conn.connect(adjustConnectorParameter(MyRestConnEEMMixedExample.createConnectorParameter()));
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
    

}
