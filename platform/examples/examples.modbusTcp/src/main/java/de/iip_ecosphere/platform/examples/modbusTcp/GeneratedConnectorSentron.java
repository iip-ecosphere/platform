package de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.services.environment.metricsProvider.LogRunnable;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.ModbusSiemensRwSentron;
import iip.datatypes.ModbusSiemensSentron;
import iip.nodes.MyModbusSentronConnExample;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Runs the generated Sentron connector.
 * 
 * @author Christian Nikolajew
 *
 */
public class GeneratedConnectorSentron {
    
    public static final String TOTAL_REQUEST_TIME = "totalRequestTime";
    
    private static final int MAX = 1000;
    private static MetricsProvider metrics = new MetricsProvider(new SimpleMeterRegistry());
    private static ModbusServer server;
    
    private static Clock clock;
    private static LogRunnable logger;

    /**
     * Runs the generated Sentron connector. Currently not integrated with test as the
     * server must be online (external, not guaranteed).
     * 
     * @param args the command line arguments, ignored
     * @throws IOException in case that the server cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        
        //test(false);
        performanceTest(false);
        
    }
    
    /**
     * Try to receive Data from the connected machine until the user ends the test.
     * 
     * @param startServer true = Start with TestServer, false = Start without TestServer
     * @throws IOException if the connector fails (preliminary)
     */
    public static void test(boolean startServer) throws IOException {
        
        if (startServer) {
            server = new ModbusServer(MyModbusSentronConnExample.createConnectorParameter());
            server.start();
        }
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<ModbusSiemensSentron> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusSiemensSentron data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data.getPowerConsumption());
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusSiemensSentron> getType() {
                return ModbusSiemensSentron.class;
            }

        };
        ModbusTcpIpConnector<ModbusSiemensSentron, ModbusSiemensRwSentron> conn = createPlatformConnector(cb);
       
        boolean run = true;
        
        do {
            
            conn.request(true);
            TimeUtils.sleep(3);
            
        } while (run);
        
        
        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);
        
        if (startServer) {
            server.stop();
        }   
    }
    
    /**
     *  Try to receive Data from the connected machine until MAX is reached and logs the totalRequestTime.
     * 
     * @param startServer true = Start with TestServer, false = Start without TestServer
     * @throws IOException if the connector fails (preliminary)
     */
    public static void performanceTest(boolean startServer) throws IOException {
        
        clock = metrics.getClock();
        File log = new File("totalRequestTimeGeneratedSentronConnector.txt");
        logger = new LogRunnable(log);
        new Thread(logger).start();
        
        if (startServer) {
            server = new ModbusServer(MyModbusSentronConnExample.createConnectorParameter());
            server.start();
        }
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        AtomicInteger count = new AtomicInteger(0);
        ReceptionCallback<ModbusSiemensSentron> cb = new ReceptionCallback<>() {

            @Override
            public void received(ModbusSiemensSentron data) {
                System.out.println("RECEIVED (" + count.get() + "): " + data.getPowerConsumption());
                count.incrementAndGet();
            }

            @Override
            public Class<ModbusSiemensSentron> getType() {
                return ModbusSiemensSentron.class;
            }

        };
        ModbusTcpIpConnector<ModbusSiemensSentron, ModbusSiemensRwSentron> conn = createPlatformConnector(cb);
       
        for (int i = 0; i < MAX; i++) {
            
            final long s = clock.monotonicTime();
            conn.request(true);
            final long duration = clock.monotonicTime() - s;
            metrics.recordWithTimer(TOTAL_REQUEST_TIME, duration, TimeUnit.NANOSECONDS);
            logger.log(TOTAL_REQUEST_TIME, duration);
            
            
            TimeUtils.sleep(3);
        }
        
        System.out.println("Sleeping to flush...");
        TimeUtils.sleep(1000);
        System.out.println("Disconnecting...");
        conn.disconnect();
        System.out.println("Received: " + count);
        
        if (startServer) {
            server.stop();
        }    
    }
    
    /**
     * Creates the platform connector to be tested.
     * 
     * @param callback the callback
     * @return the connector instance
     * @throws IOException if creating the connector fails
     */
    public static ModbusTcpIpConnector<ModbusSiemensSentron, ModbusSiemensRwSentron> createPlatformConnector(
            ReceptionCallback<ModbusSiemensSentron> callback) throws IOException {
        ModbusTcpIpConnector<ModbusSiemensSentron, ModbusSiemensRwSentron> conn = new ModbusTcpIpConnector<>(
                MyModbusSentronConnExample.createConnectorAdapter(
                        null, new File("modbusTestGeneratedConnectorSentron.txt")));
        conn.connect(MyModbusSentronConnExample.createConnectorParameter());
        conn.setReceptionCallback(callback);
        return conn;
    }
}
