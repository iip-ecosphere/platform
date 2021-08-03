package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;
import java.util.ArrayList;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.MqttMessage;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

public class MqttK8S {

    private static String serverIP;
    private static String serverPort;
    private ServerAddress addr;
    private ProxyType proxyType;
    
    /**
     * Creates a mqtt K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param addr the address of the mqtt proxy
     * @param serverPort  the port of the server
     * @param serverIP  the IP Address of the server
     *  
     */
    public MqttK8S(ProxyType proxyType, ServerAddress addr, String serverIP, String serverPort) {
        super();
        this.addr = addr;
        MqttK8S.serverIP = serverIP;
        MqttK8S.serverPort = serverPort;
        this.proxyType = proxyType;
    }

    /**
     * Returns the server address.
     * 
     * @return the server address
     */
    public ServerAddress getAddr() {
        return addr;
    }

    /**
     * Set the server address.
     *
     * @param addr the server address
     */
    public void setAddr(ServerAddress addr) {
        this.addr = addr;
    }
    
    /**
     * Returns the server IP address.
     * 
     * @return the server IP address
     */
    public String getServerIP() {
        return serverIP;
    }

    /**
     * Set the server IP address.
     *
     * @param serverIP the server IP address
     */
    public void setServerIP(String serverIP) {
        MqttK8S.serverIP = serverIP;
    }
    
    /**
     * Returns the server port.
     * 
     * @return the server port
     */
    public String getServerPort() {
        return serverPort;
    }

    /**
     * Set the server port.
     *
     * @param serverPort the server port
     */
    public void setServerPort(String serverPort) {
        MqttK8S.serverPort = serverPort;
    }

    /**
     * Returns the proxyType of K8S java proxy.
     * 
     * @return the proxyType of K8S java proxy
     */
    public ProxyType getProxyType() {
        return proxyType;
    }

    /**
     * Set the proxyType of K8S java proxy.
     *
     * @param proxyType the proxyType of K8S java proxy
     */
    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }
    
    /**
     * The start method to run the server proxy.
     * 
     */
    public void start() {
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
        
        TestQpidServer server = new TestQpidServer(addr);
        server.start();
                
        ArrayList<MqttMessage> mqttMessagesList = new ArrayList<MqttMessage>();
        
        SerializerRegistry.registerSerializer(MqttMessageJsonSerializer.class);
        TransportParameter param1 = TransportParameterBuilder.newBuilder(addr)
                .setApplicationId("cl1").build();
        TransportConnector cl1 = TransportFactory.createConnector();
        try {
            cl1.connect(param1);            
            final String stream1 = cl1.composeStreamName("", "stream1");
            
            final CallbackMessage cb1 = new CallbackMessage();
            cl1.setReceptionCallback(stream1, cb1);
            MqttMessage responseMessage = new MqttMessage("Empty", "Empty");
            
            Thread requestThread = new Thread() {
                public void run() {
                    while (true) {
                        if (mqttMessagesList.isEmpty()) {
                            TimeUtils.sleep(100);
                        } else {
                            MqttMessage tempMessage = mqttMessagesList.remove(0);
                            final String stream2 = cl1.composeStreamName("", tempMessage.getStreamId());
                            responseMessage.setStreamId(tempMessage.getStreamId());

                            if (proxyType == ProxyType.MasterProxy) {
                                responseMessage.setResponse(sendToK8S(tempMessage));
                            } else {
                                responseMessage.setResponse(sendToMasterMqtt(tempMessage));
                            }
                            try {
                                cl1.asyncSend(stream2, responseMessage);
                                System.out.println("Sent Response");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            };
            requestThread.start();
            
            Thread requestMessageAddingThread = new Thread() {
                public void run() {
                    while (true) {
                        if (cb1.dequeIsEmpty()) {
                            TimeUtils.sleep(10);
                        } else {
                            mqttMessagesList.add(cb1.getData());
                        }
                    }
                }

            };
            requestMessageAddingThread.start();   
//            cl1.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TransportFactory.setMainImplementation(old);
        SerializerRegistry.unregisterSerializer(MqttMessageJsonSerializer.class);
    }
    
    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param received mqtt Message
     * 
     * @return the response from the master Mqtt
     * 
     */
    private static String sendToMasterMqtt(MqttMessage received) {
        String response = null;
        
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

        ServerAddress addr = new ServerAddress(Schema.IGNORE, serverIP, Integer.parseInt(serverPort)); 

        MqttMessage message1 = new MqttMessage("PCstream", received.getMessageTxt());
        message1.generateStreamIdNo();
        
        SerializerRegistry.registerSerializer(MqttMessageJsonSerializer.class);
        TransportParameter param1 = TransportParameterBuilder.newBuilder(addr)
                .setApplicationId("cl1").build();
        TransportConnector cl1 = TransportFactory.createConnector();
        try {
            cl1.connect(param1);
            
            final String stream1 = cl1.composeStreamName("", "stream1");
            final String stream2 = cl1.composeStreamName("", message1.getStreamId());
            
            cl1.asyncSend(stream1, message1);
            
            System.out.println("Sent Request");
            
            final CallbackMessage cb1 = new CallbackMessage();
            cl1.setReceptionCallback(stream2, cb1);
            
            while (cb1.dequeIsEmpty()) {
                TimeUtils.sleep(100);
            }

            response = cb1.getData().getMessageTxt();
            
            cl1.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TransportFactory.setMainImplementation(old);
        SerializerRegistry.unregisterSerializer(MqttMessageJsonSerializer.class);
        
//        received.setData(null);
        return response;
        
    }
 
    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param received mqtt Message
     * 
     * @return the response from the K8S apiserver;
     */
    private String sendToK8S(MqttMessage received) {        
        K8SJavaProxy mqttK8SJavaProxy = new MqttK8SJavaProxy(ProxyType.MasterProxy, serverIP, serverPort);
        
        K8SRequest request = new K8SRequest();
        
        byte[] requestByte = request.convertBase64StringToByte(received.getMessageTxt());
        
        request = mqttK8SJavaProxy.createK8SRequest(requestByte);
//        request.convertStringToRequest(received.getMessageTxt());
        //aasK8SJavaProxy.createK8SRequest(requestString.getBytes(StandardCharsets.UTF_8));
        
        String response = null;
        try {
            response = mqttK8SJavaProxy.sendK8SRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
//        received.setData(null);
        
        return response;
    }
}
