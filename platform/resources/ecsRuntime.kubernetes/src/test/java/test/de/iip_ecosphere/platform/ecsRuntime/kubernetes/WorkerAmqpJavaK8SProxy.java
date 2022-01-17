package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.connectors.rabbitmq.RabbitMqAmqpTransportFactoryDescriptor;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;
import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;

public class WorkerAmqpJavaK8SProxy {
  
    private static int localPort = 6443;
    private static int mqttPort = 9911;
    private static String serverIP = "192.168.81.212";
    private static String serverPort = "9922";
    private static boolean tlsCheck = false;

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
        WorkerAmqpJavaK8SProxy.localPort = localPort;
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
        WorkerAmqpJavaK8SProxy.serverIP = serverIP;
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
        WorkerAmqpJavaK8SProxy.serverPort = serverPort;
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
        WorkerAmqpJavaK8SProxy.mqttPort = mqttPort;
    }

    /**
     * The main method to run the server proxy.
     * 
     * @param args the main method arguments
     * 
     */
    public static void main(String[] args) {
        
        try {
            TransportFactory.setMainImplementation(RabbitMqAmqpTransportFactoryDescriptor.MAIN);
            
            ConnectorCreator old = TransportFactory.setMainImplementation(new ConnectorCreator() {

                @Override
                public TransportConnector createConnector() {
                    return new FakeAuthConnector();
                }

                @Override
                public String getName() {
                    return FakeAuthConnector.NAME;
                }

            });
            
            TransportParameterConfigurer configurer = null;
            if (tlsCheck) {
                File secCfg = new File("./src/test/AMQP/secCfg");
                configurer = new TransportParameterConfigurer() {
                    
                    @Override
                    public void configure(TransportParameterBuilder builder) {
                        builder.setKeystore(new File(secCfg, "keystore.jks"), TestQpidServer.KEYSTORE_PASSWORD);
                    }
                };
            }
            
            TransportConnector cl1 = TransportFactory.createConnector();
            TransportK8STLS transportK8STLS = new TransportK8STLS(tlsCheck, configurer);
            
            K8SJavaProxy mqttK8SJavaProxy = new TransportK8SJavaProxy(ProxyType.WorkerProxy, serverIP, serverPort,
                    transportK8STLS);
            
            startMultiThreaded(mqttK8SJavaProxy, localPort);
        } catch (UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                | CertificateException | InvalidKeySpecException | IOException e) {
            System.err.println("Exception in the starting the multi-threads method");
            e.printStackTrace();
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
            TransportFactory.setMainImplementation(RabbitMqAmqpTransportFactoryDescriptor.MAIN);
            
            ConnectorCreator old = TransportFactory.setMainImplementation(new ConnectorCreator() {

                @Override
                public TransportConnector createConnector() {
                    return new FakeAuthConnector();
                }

                @Override
                public String getName() {
                    return FakeAuthConnector.NAME;
                }

            });
            
            TransportParameterConfigurer configurer = null;
            if (tlsCheck) {
                File secCfg = new File("./src/test/AMQP/secCfg");
                configurer = new TransportParameterConfigurer() {
                    
                    @Override
                    public void configure(TransportParameterBuilder builder) {
                        builder.setKeystore(new File(secCfg, "keystore.jks"), TestQpidServer.KEYSTORE_PASSWORD);
                    }
                };
            }
            
            TransportConnector cl1 = TransportFactory.createConnector();
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

        ServerSocket serverSocket = mqttK8SJavaProxy.getServerSocket(localPort, null, null, null, tlsCheck);

        System.out.println("Started multi-threaded server at localhost port " + localPort);

        final Charset encoding = StandardCharsets.UTF_8;

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