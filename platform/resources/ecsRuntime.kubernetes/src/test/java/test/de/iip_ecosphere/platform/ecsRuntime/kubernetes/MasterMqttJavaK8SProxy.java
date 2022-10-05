package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnectorFactoryDescriptor;
//import de.iip_ecosphere.platform.transport.TransportFactory;
//import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
//import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
//import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;
import test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;

public class MasterMqttJavaK8SProxy {
  
    private static int localPort = 6443;
    private static int mqttPort = 9922;
    private static String serverIP = "Empty";
    private static String serverPort = "6443";
    private static boolean tlsCheck = false;
    
    /** 
     * Returns the port on localhost to receive new requests.
     * 
     * @return the port on localhost to receive new requests
     */
    public static int getLocalPort() {
        return localPort;
    }

    /**
     * Set the port on localhost to receive new requests.
     *
     * @param localPort the port on localhost to receive new requests
     */
    public static void setLocalPort(int localPort) {
        MasterMqttJavaK8SProxy.localPort = localPort;
    }

    /**
     * Returns the IP Address of the server.
     * 
     * @return the IP Address of the server
     */
    public static String getServerIP() {
        return serverIP;
    }

    /**
     * Set the IP Address of the server.
     * 
     * @param serverIP the IP Address of the server 
     */
    public static void setServerIP(String serverIP) {
        MasterMqttJavaK8SProxy.serverIP = serverIP;
    }

    /**
     * Returns the port of the server (either the Aas port or K8S apiserver port).
     * 
     * @return the port of the server (either the Aas port or K8S apiserver port)
     */
    public static String getServerPort() {
        return serverPort;
    }

    /**
     * Set the port of the server (either the Aas port or K8S apiserver port).
     *
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port).
     */
    public static void setServerPort(String serverPort) {
        MasterMqttJavaK8SProxy.serverPort = serverPort;
    }

    /**
     * Returns the mqtt port.
     * 
     * @return the mqtt port
     */
    public int getMqttPort() {
        return mqttPort;
    }

    /**
     * Set the mqtt port.
     *
     * @param mqttPort the mqtt port
     */
    public void setMqttPort(int mqttPort) {
        MasterMqttJavaK8SProxy.mqttPort = mqttPort;
    }

    /**
     * The main method to run the server proxy.
     * 
     * @param args the main method arguments
     * 
     */
    public static void main(String[] args) {
        
        if (args.length > 0) {
            serverIP = args[0];
            System.out.println("Api Server IP:" + serverIP);
        } else {
            System.out.println("No Api Server IP passed");
        }
        
        if (args.length > 1) {
            tlsCheck = Boolean.parseBoolean(args[1]);
            if (tlsCheck) {
                System.out.println("Security option Enabled");
            } else {
                System.out.println("Security option Disabled");
            }
        } else {
            System.out.println("No security option passed, default false");
        }
        
        if (new File("/tmp/EndServerRun.k8s").exists()) {
            System.out.println("/tmp/EndServerRun.k8s is exist and stop the Client");
            return;
        }
        
        ServerAddress addr = new ServerAddress(Schema.IGNORE, serverIP, mqttPort);
        
        TestHiveMqServer.setConfigDir(null);
        TestHiveMqServer server = new TestHiveMqServer(addr);
        TransportParameterConfigurer configurer = null;
        if (tlsCheck) {
            File secCfg = new File("./src/test/MQTT/secCfg");
            TestHiveMqServer.setConfigDir(secCfg);
            configurer = new TransportParameterConfigurer() {
                
                @Override
                public void configure(TransportParameterBuilder builder) {
                    builder.setKeystoreKey("mqttKeyStore");
//                  builder.setKeystore(new File(secCfg, "client-trust-store.jks"), TestHiveMqServer.KEYSTORE_PASSWORD);
                    builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
                    builder.setActionTimeout(3000);
                }
            };
        } else {
            TestHiveMqServer.setConfigDir(null);
        }
        server.start();
        TransportK8STLS transportK8STLS = new TransportK8STLS(tlsCheck, configurer);

        TransportK8S mqtt = new TransportK8S(ProxyType.MasterProxy, addr, serverIP, serverPort, tlsCheck);
        TransportFactory.setMainImplementation(PahoMqttV5TransportConnectorFactoryDescriptor.MAIN);
//        TransportConnector cl1 = TransportFactory.createConnector();
        
        mqtt.start(transportK8STLS);
        
        while (true) {
            if (new File("/tmp/EndServerRun.k8s").exists()) {
                try {
                    mqtt.setStopped(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.stop(true);
                break;
            }
            TimeUtils.sleep(1);
        }
    }

    /**
     * The main method to run the test server proxy.
     * 
     */
    @Test(timeout = 120 * 1000)
    public void mainTest() {
        tlsCheck = Boolean.valueOf(System.getProperty("tlsCheck"));

        ServerAddress addr = new ServerAddress(Schema.IGNORE, serverIP, mqttPort);
        
        TestHiveMqServer.setConfigDir(null);
        TestHiveMqServer server = new TestHiveMqServer(addr);
        TransportParameterConfigurer configurer = null;
        if (tlsCheck) {
            File secCfg = new File("./src/test/MQTT/secCfg");
            TestHiveMqServer.setConfigDir(secCfg);
            configurer = new TransportParameterConfigurer() {
                
                @Override
                public void configure(TransportParameterBuilder builder) {
                    builder.setKeystoreKey("mqttKeyStore");
//                  builder.setKeystore(new File(secCfg, "client-trust-store.jks"), TestHiveMqServer.KEYSTORE_PASSWORD);
                    builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
                    builder.setActionTimeout(3000);
                }
            };
        } else {
            TestHiveMqServer.setConfigDir(null);
        }
        server.start();
        TransportK8STLS transportK8STLS = new TransportK8STLS(tlsCheck, configurer);

        TransportK8S mqtt = new TransportK8S(ProxyType.MasterProxy, addr, serverIP, serverPort, tlsCheck);
        TransportFactory.setMainImplementation(PahoMqttV5TransportConnectorFactoryDescriptor.MAIN);
//        TransportConnector cl1 = TransportFactory.createConnector();
        
        mqtt.start(transportK8STLS);
        
        while (true) {
            TimeUtils.sleep(1);
        }
    }
//    /**
//     * Start multi-threads method to receive and process requests.
//     * 
//     * @param mqttK8SJavaProxy  the proxy used to receive the new requests
//     * @param localPort         is the port on the localhost to receive the new
//     *                          requests
//     * 
//     * @throws IOException
//     * @throws InvalidKeySpecException
//     * @throws CertificateException
//     * @throws KeyStoreException
//     * @throws NoSuchAlgorithmException
//     * @throws KeyManagementException
//     * @throws UnrecoverableKeyException
//     * 
//     */
//    public static void startMultiThreaded(final K8SJavaProxy mqttK8SJavaProxy, int localPort)
//            throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
//            CertificateException, InvalidKeySpecException, IOException {
//
//        ServerSocket serverSocket = mqttK8SJavaProxy.getServerSocket(localPort, null, null, null);
//
//        System.out.println("Started multi-threaded server at localhost port " + localPort);
//
//        final Charset encoding = StandardCharsets.UTF_8;
//
//        while (true) {
//            final Socket socket = serverSocket.accept();
//            System.out.println("Accept socket");
//
//            Thread requestThread = new Thread() {
//                public void run() {
//                    InputStream reader = null;
//                    BufferedOutputStream writer = null;
//
//                    try {
//                        reader = socket.getInputStream();
//
//                        writer = new BufferedOutputStream(socket.getOutputStream());
//                        byte[] requestByte = mqttK8SJavaProxy.extractK8SRequestByte(reader);
//
//                        if (requestByte.length == 0) {
//                            return;
//                        }
//
//                        K8SRequest request = mqttK8SJavaProxy.createK8SRequest(requestByte);
//                        byte[] responseString = mqttK8SJavaProxy.sendK8SRequest(writer, request);
//                        writer.write(responseString);
//                        writer.flush(); 
//                        System.out.println("socket thread ends normal");
//                    } catch (IOException e) {
//                        System.err.println("Exception while creating response");
//                        e.printStackTrace();
//                        System.out.println("socket thread ends Throwable");
//                    } finally {
//                        try {
//                            writer.close();
//                            reader.close();
//                            socket.close();
//                        } catch (IOException e) {
//                            System.err.println("Could not close the streams");
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//            requestThread.start();
//        }
//    }
}