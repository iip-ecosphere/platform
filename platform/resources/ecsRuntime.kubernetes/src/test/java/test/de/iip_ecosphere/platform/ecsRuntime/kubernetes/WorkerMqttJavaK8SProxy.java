package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.mqttv5.PahoMqttV5TransportConnectorFactoryDescriptor;
import test.de.iip_ecosphere.platform.test.mqtt.hivemq.TestHiveMqServer;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;

public class WorkerMqttJavaK8SProxy {
  
    private static int localPort = 6443;
    private static int mqttPort = 9911;
    private static String serverIP = "Empty";
    private static String serverPort = "9922";
    private static boolean tlsCheck = false;
    private static ArrayList<ServerSocket> serverSocketList = new ArrayList<ServerSocket>();
    private static ArrayList<TransportK8SJavaProxy> k8SJavaProxyList = new ArrayList<TransportK8SJavaProxy>();

    //    private static ConcurrentLinkedDeque<Integer> requestDeque = new ConcurrentLinkedDeque<Integer>();
    
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
        WorkerMqttJavaK8SProxy.localPort = localPort;
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
        WorkerMqttJavaK8SProxy.serverIP = serverIP;
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
        WorkerMqttJavaK8SProxy.serverPort = serverPort;
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
        WorkerMqttJavaK8SProxy.mqttPort = mqttPort;
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
        
        Thread requestThread = new Thread() { 
            public void run() {
                try {            
                    TransportFactory.setMainImplementation(PahoMqttV5TransportConnectorFactoryDescriptor.MAIN);
                    
                    TransportParameterConfigurer configurer = null;
                    if (tlsCheck) {
//                        File secCfg = new File("./src/test/MQTT/secCfg");
                        configurer = new TransportParameterConfigurer() {
                            
                            @Override
                            public void configure(TransportParameterBuilder builder) {
                                builder.setKeystoreKey("mqttKeyStore");
//                              builder.setKeystore(new File(secCfg, "client-trust-store.jks"), 
//                                        TestHiveMqServer.KEYSTORE_PASSWORD);
                                builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
                                builder.setActionTimeout(3000);
                            }
                        };
                    }
                    
                    TransportK8STLS transportK8STLS = new TransportK8STLS(tlsCheck, configurer);
                    
                    K8SJavaProxy mqttK8SJavaProxy = new TransportK8SJavaProxy(ProxyType.WorkerProxy, serverIP,
                            serverPort, transportK8STLS);
                    
                    k8SJavaProxyList.add((TransportK8SJavaProxy) mqttK8SJavaProxy);
                    
                    startMultiThreaded(mqttK8SJavaProxy, localPort);
                } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException
                        | KeyStoreException | CertificateException | InvalidKeySpecException | IOException e) {
                    System.err.println("Exception in the starting the multi-threads method");
                    e.printStackTrace();
                }
            }
        };
        requestThread.start();
        
        while (true) {
            if (new File("/tmp/EndClientRun.k8s").exists()) {
                try {
                    k8SJavaProxyList.get(0).getNormalcl1().disconnect();
                    serverSocketList.get(0).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            TimeUtils.sleep(1);
        }
    }

    /**
     * The main method to run the test server proxy.
     * 
     */
    @Test(timeout = 100 * 1000)
    public void mainTest() {
        tlsCheck = Boolean.valueOf(System.getProperty("tlsCheck"));

        try {            
            TransportFactory.setMainImplementation(PahoMqttV5TransportConnectorFactoryDescriptor.MAIN);
//            TransportConnector cl1 = TransportFactory.createConnector();
            
            TransportParameterConfigurer configurer = null;
            if (tlsCheck) {
//                File secCfg = new File("./src/test/MQTT/secCfg");
                configurer = new TransportParameterConfigurer() {
                    
                    @Override
                    public void configure(TransportParameterBuilder builder) {
                        builder.setKeystoreKey("mqttKeyStore");
//                      builder.setKeystore(new File(secCfg, "client-trust-store.jks"), 
//                                TestHiveMqServer.KEYSTORE_PASSWORD);
                        builder.setKeyAlias(TestHiveMqServer.KEY_ALIAS);
                        builder.setActionTimeout(3000);
                    }
                };
            }
            
            TransportK8STLS transportK8STLS = new TransportK8STLS(tlsCheck, configurer);
            
            K8SJavaProxy mqttK8SJavaProxy = new TransportK8SJavaProxy(ProxyType.WorkerProxy, serverIP, serverPort,
                    transportK8STLS);
            
            startMultiThreaded(mqttK8SJavaProxy, localPort);
        } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                | CertificateException | InvalidKeySpecException | IOException e) {
            System.err.println("Exception in the starting the multi-threads method");
            e.printStackTrace();
        }
        
        while (true) {
            TimeUtils.sleep(1);
        }
    }
    
    /**
     * Start multi-threads method to receive and process requests.
     * 
     * @param mqttK8SJavaProxy  the proxy used to receive the new requests
     * @param localPort         is the port on the localhost to receive the new
     *                          requests
     * 
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnrecoverableKeyException
     * 
     */
    public static void startMultiThreaded(final K8SJavaProxy mqttK8SJavaProxy, int localPort)
            throws UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, InvalidKeySpecException, IOException {
        
        if (new File("/tmp/EndClientRun.k8s").exists()) {
            System.out.println("/tmp/EndClientRun.k8s is exist and stop the Client");
            return;
        }
        
        ServerSocket serverSocket = mqttK8SJavaProxy.getServerSocket(localPort, null, null, null, tlsCheck);
        serverSocketList.add(serverSocket);
        
        System.out.println("Started multi-threaded server at localhost port " + localPort);

//        final Charset encoding = StandardCharsets.UTF_8;

        File file = new File("ClientReady.k8s"); 
        file.createNewFile();
        
        while (true) {
            final Socket socket = serverSocket.accept();
//            System.out.println("Accept socket");

            Thread requestThread = new Thread() {
                public void run() {
                    InputStream reader = null;
                    BufferedOutputStream writer = null;

                    try {
                        while (true) {
                            reader = socket.getInputStream();
                            writer = new BufferedOutputStream(socket.getOutputStream());

                            byte[] requestByte = mqttK8SJavaProxy.extractK8SRequestByte(reader);

                            if (requestByte != null) {

                                K8SRequest request = mqttK8SJavaProxy.createK8SRequest(requestByte);
                                byte[] responseString = mqttK8SJavaProxy.sendK8SRequest(writer, request);
                                
                                if (responseString.length == 0) {
                                    break;
                                }
                                
                                writer.write(responseString);
                                writer.flush();
                                
                                if (request.getPath().contains("&watch=true")) {
                                    break;
                                }
                            } else {
                                break;
                            }
                       
                        }
                    
                    } catch (SocketException e) {
                        if (e.getMessage().contentEquals("Socket input is already shutdown")) {
                            System.out.println(e.getMessage());
                        } else {
                            System.err.println("SocketException while creating response");
                            e.printStackTrace();
                        }
                    } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                            | CertificateException e) {
                            System.err.println("Exception while creating response");
                            e.printStackTrace();
                            System.out.println("socket thread ends Throwable");
                    } finally {
                        try {
                            writer.close();
                            reader.close();
                        } catch (IOException e) {
                            System.err.println("Could not close the streams");
                            e.printStackTrace();
                        }
                    }
                }
            };
            requestThread.start();
        }
    }
}