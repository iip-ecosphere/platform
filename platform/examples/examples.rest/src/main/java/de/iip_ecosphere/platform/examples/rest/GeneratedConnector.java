package de.iip_ecosphere.platform.examples.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.rest.RESTConnector;
import de.iip_ecosphere.platform.connectors.rest.RESTItem;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.RestPhoenixEEMMixed;
import iip.datatypes.RestPhoenixRwEEMMixed;
import iip.nodes.MyRestConnEEMMixedExample;
import iip.nodes.MyRestConnEEMMixedExampleImpl;
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
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<RestPhoenixEEMMixed> cb = new ReceptionCallback<>() {

            @Override
            public void received(RestPhoenixEEMMixed data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data);
                count.incrementAndGet();
            }

            @Override
            public Class<RestPhoenixEEMMixed> getType() {
                return RestPhoenixEEMMixed.class;
            }

        };
        
        RESTConnector<RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> conn = createPlatformConnector(cb);
        conn.request(true);
        
//        boolean run = true;
//        
//        do {
//            
//            conn.request(true);
//            TimeUtils.sleep(3);
//            
//        } while(run);

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
        
        RESTConnector<RestPhoenixEEMMixed, RestPhoenixRwEEMMixed> conn = 
                new MyRestConnEEMMixedExampleImpl(adapter);
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
        
        ConnectorParameterBuilder testParameter = ConnectorParameterBuilder.newBuilder(param);
        testParameter.setEndpointPath(endpoint.toUri());
        return testParameter.build();
        
    }
}
