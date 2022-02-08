package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.bouncycastle.util.Arrays;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.TransportMessage;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import okhttp3.Response;
import okio.Buffer;

public class TransportK8S {

    private static String serverIP;
    private static String serverPort;
    private static K8SJavaProxy transportK8SJavaProxy;
    private static boolean isStopped = false;
    private ServerAddress addr;
    private ProxyType proxyType;
    
    
    /**
     * Creates a transport K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param addr the address of the transport proxy
     * @param serverPort  the port of the server
     * @param serverIP  the IP Address of the server
     * @param tlsCheck check to use tls security
     *  
     */
    public TransportK8S(ProxyType proxyType, ServerAddress addr, String serverIP, String serverPort, boolean tlsCheck) {
        super();
        this.addr = addr;
        TransportK8S.serverIP = serverIP;
        TransportK8S.serverPort = serverPort;
        this.proxyType = proxyType;
        transportK8SJavaProxy = new TransportK8SJavaProxy(ProxyType.MasterProxy, serverIP, serverPort, tlsCheck);
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
        TransportK8S.serverIP = serverIP;
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
        TransportK8S.serverPort = serverPort;
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
     * Returns the Transport stop status.
     * 
     * @return the Transport stop status
     */
    public boolean isStopped() {
        return isStopped;
    }

    /**
     * Set the Transport stop status.
     *
     * @param isStopped the Transport stop status
     */
    public void setStopped(boolean isStopped) {
        TransportK8S.isStopped = isStopped;
    }

    /**
     * The start method to run the server proxy.
     * 
     * @param transportK8STLS the tls security information
     * 
     */
    public void start(TransportK8STLS transportK8STLS) {
        ConcurrentLinkedDeque<TransportMessage> transportMessagesList = new ConcurrentLinkedDeque<TransportMessage>();
        SerializerRegistry.registerSerializer(TransportMessageJsonSerializer.class);
        TransportParameterBuilder tpb1 = TransportParameterBuilder.newBuilder(addr).setApplicationId("cl1");
        if (transportK8STLS.isTlsCheck()) {
            transportK8STLS.getConfigurer().configure(tpb1);
        }
        TransportParameter param1 = tpb1.build();
        TransportConnector cl1 = TransportFactory.createConnector();
        try {
            cl1.connect(param1);
            final String stream1 = cl1.composeStreamName("", "stream1");
            final String watchStream1 = cl1.composeStreamName("", "watchStream1");
            final CallbackMessage cb1 = new CallbackMessage();
            final CallbackMessage watchcb1 = new CallbackMessage();
            cl1.setReceptionCallback(stream1, cb1);
            cl1.setReceptionCallback(watchStream1, watchcb1);
            Thread requestMessageAddingThread = new Thread() {
                public void run() {
                    while (true) {
                        if (isStopped) {
                            break;
                        }
                        if (!cb1.dequeIsEmpty()) {
                            transportMessagesList.add(cb1.getData());
                        } else if (!watchcb1.dequeIsEmpty()) {
                            transportMessagesList.add(watchcb1.getData());
                        } else {
                            TimeUtils.sleep(1);
                        }
                    }
                }
            };
            requestMessageAddingThread.start();   
            Thread requestThread = new Thread() {
                public void run() {
                    while (true) {
                        if (isStopped) {
                            break;
                        }
                        if (transportMessagesList.isEmpty()) {
                            TimeUtils.sleep(1);
                        } else {
                            TransportMessage tempMessage = transportMessagesList.removeFirst();
                            Thread requestThread = new Thread() {
                                public void run() {
                                    try {
                                        TransportMessage responseMessage = new TransportMessage("Empty", null,
                                                "Empty");
                                        final String stream2 = cl1.composeStreamName("", tempMessage.getStreamId());
                                        responseMessage.setStreamId(tempMessage.getStreamId());
                                        responseMessage.setRequestWatch(tempMessage.getRequestWatch());
                                        if (proxyType == ProxyType.MasterProxy) {
                                            if (tempMessage.getRequestWatch().equals("Yes")) {
                                                responseMessage.setMessageByte(sendWatchToK8S(tempMessage, cl1));
                                            } else {
                                                responseMessage.setMessageByte(sendToK8S(tempMessage));
                                            }
                                        } else {
                                            responseMessage.setMessageByte(sendToMasterTransport(tempMessage));
                                        }
                                        cl1.asyncSend(stream2, responseMessage);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            requestThread.start();
                        }
                    }
                }
            };
            requestThread.start();
            File file = new File("ServerReady.k8s"); 
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param received transport Message
     * 
     * @return the response from the master transport
     * 
     */
    private static byte[] sendToMasterTransport(TransportMessage received) {
        byte[] response = null; 
        
//        ConnectorCreator old = TransportFactory.setMainImplementation(new ConnectorCreator() {
//
//            @Override
//            public TransportConnector createConnector() {
//                return new FakeAuthConnector();
//            }
//
//            @Override
//            public String getName() {
//                return FakeAuthConnector.NAME;
//            }
//
//        });

        ServerAddress addr = new ServerAddress(Schema.IGNORE, serverIP, Integer.parseInt(serverPort)); 

        TransportMessage message1 = new TransportMessage("PCstream", received.getMessageByte(),
                received.getRequestWatch());
        message1.generateStreamIdNo();
        
        SerializerRegistry.registerSerializer(TransportMessageJsonSerializer.class);
        TransportParameter param1 = TransportParameterBuilder.newBuilder(addr)
                .setApplicationId("cl1").build();
        TransportConnector cl1 = TransportFactory.createConnector();
        try {
            cl1.connect(param1);
            
            final String stream1 = cl1.composeStreamName("", "stream1");
            final String stream2 = cl1.composeStreamName("", message1.getStreamId());
            
            cl1.asyncSend(stream1, message1);
                        
            final CallbackMessage cb1 = new CallbackMessage();
            cl1.setReceptionCallback(stream2, cb1);
            
            while (cb1.dequeIsEmpty()) {
                TimeUtils.sleep(1);
            }

            response = cb1.getData().getMessageByte();
            
            cl1.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        TransportFactory.setMainImplementation(old);
        SerializerRegistry.unregisterSerializer(TransportMessageJsonSerializer.class);
        
        return response;
        
    }
 
    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param received transport Message
     * 
     * @return the response from the K8S apiserver;
     */
    private byte[] sendToK8S(TransportMessage received) {        
        K8SRequest request = new K8SRequest();
        
        byte[] requestByte = received.getMessageByte();
        
        request = transportK8SJavaProxy.createK8SRequest(requestByte);

        byte[] response = null;
        try {
            response = transportK8SJavaProxy.sendK8SRequest(null, request);            
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                | CertificateException e) {
            e.printStackTrace();
        }
        
        return response;
    }
    
    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param received transport Message
     * @param connector the connector to send the watch stream messages
     * 
     * @return the response from the K8S apiserver;
     * @throws IOException 
     */
    private byte[] sendWatchToK8S(TransportMessage received, TransportConnector connector) throws IOException {        
        K8SRequest request = new K8SRequest();
        byte[] requestByte = received.getMessageByte();
        request = transportK8SJavaProxy.createK8SRequest(requestByte);
        byte[] responseBody = new byte[0];
        String responseString = "";
        Response response = transportK8SJavaProxy.executeWatchK8SJavaClientRequest(request);
        String formattedHeaderResponse = request.getProtocol() + " " + response.code() + " " + response.message()
                + "\r\n" + response.headers().toString() + "\r\n";
        responseBody = formattedHeaderResponse.getBytes();
        final String stream2 = connector.composeStreamName("", received.getStreamId());
        try {
            if (!response.body().source().exhausted()) {
                if (formattedHeaderResponse.contains("application/vnd.kubernetes.protobuf")) {
                    Buffer buffer = new Buffer();
                    response.body().source().read(buffer, 4096);
                    responseBody = buffer.readByteArray();
                } else {
                    long count = response.body().source().indexOf((byte) '\n');
                    responseBody = response.body().source().readByteArray(count + 1);
                }
                responseBody = getResponseMessage(responseBody, formattedHeaderResponse); 
                TransportMessage responseMessage = new TransportMessage(received.getStreamId(), responseBody,
                        received.getRequestWatch());
                connector.syncSend(stream2, responseMessage);
            }
        } catch (IOException e) {
            if (e.getMessage().contentEquals("timeout") || e.getMessage().contentEquals("Read timed out")) {
                response.body().close();
                responseBody = (formattedHeaderResponse + "0\r\n" + "\r\n").getBytes();
                return responseBody;
            } else {
                e.printStackTrace();
            }
        }
        if (!responseString.contains("code\":410")) {
            while (true) {
                responseString = "";
                responseBody = new byte[0];
                try {
                    if (!response.body().source().exhausted()) {
                        if (formattedHeaderResponse.contains("application/vnd.kubernetes.protobuf")) {
                            Buffer buffer = new Buffer();
                            response.body().source().read(buffer, 4096);
                            responseBody = buffer.readByteArray();
                        } else {
                            long count = response.body().source().indexOf((byte) '\n');
                            responseBody = response.body().source().readByteArray(count + 1);
                        }
                        responseBody = getResponseMessage(responseBody, null); 
                        TransportMessage responseMessage = new TransportMessage(received.getStreamId(), responseBody,
                                received.getRequestWatch());
                        connector.syncSend(stream2, responseMessage);
                    } else {
                        response.body().close();
                        return ("0\r\n" + "\r\n").getBytes();
                    }
                } catch (IOException e) {
                    if (e.getMessage().contentEquals("timeout") || e.getMessage().contentEquals("Read timed out")) {
                        response.body().close();
                        return ("0\r\n" + "\r\n").getBytes();
                    } else {
                        e.printStackTrace();
                        return ("0\r\n" + "\r\n").getBytes();
                    }
                }
            }
        } else {
            responseBody = ("0\r\n" + "\r\n").getBytes();
        }
        if (responseBody == null || responseBody.length == 0) {
            responseBody = ("0\r\n" + "\r\n").getBytes();
            System.out.println("Empty response AAS execute");
        }
        response.body().close();
        return responseBody; 
    }

    /**
     * Prepare the response for each message in the stream.
     * 
     * @param responseBody the response to prepare
     * @param formattedHeaderResponse the response header if it is the first response
     * 
     * @return the responseBody the prepared response to send
     */
    private byte[] getResponseMessage(byte[] responseBody, String formattedHeaderResponse) {
        byte[] firstPart = (Integer.toHexString(responseBody.length) + "\r\n").getBytes();
        responseBody = Arrays.concatenate(responseBody, "\r\n".getBytes());
        responseBody = Arrays.concatenate(firstPart, responseBody);
        if (formattedHeaderResponse != null) {
            responseBody = Arrays.concatenate(formattedHeaderResponse.getBytes(), responseBody);
        }
        
        return responseBody;
    }
    
//    private class RunRequest implements Runnable {
//
//        private MqttMessage mqttMessage;
//        private TransportConnector cl1;
//        
//        public RunRequest(MqttMessage mqttMessage, TransportConnector cl1) {
//            this.mqttMessage = mqttMessage;
//            this.cl1 = cl1;
//        }
//
//        public void run() {
//            
//            MqttMessage responseMessage = new MqttMessage("Empty", "Empty", "Empty");
//            final String stream2 = cl1.composeStreamName("", mqttMessage.getStreamId());
//            responseMessage.setStreamId(mqttMessage.getStreamId());
//            responseMessage.setRequestWatch(mqttMessage.getRequestWatch());
//            
//            if (proxyType == ProxyType.MasterProxy) {
//                
//                if (mqttMessage.getRequestWatch().equals("Yes")) {
//                    responseMessage.setResponse(sendWatchToK8S(mqttMessage, cl1));
//                } else {
//                    responseMessage.setResponse(sendToK8S(mqttMessage));
//                }
//            } else {
//                responseMessage.setResponse(sendToMasterMqtt(mqttMessage));
//            }
//            try {
//                cl1.syncSend(stream2, responseMessage);
//                System.out.println("Sent Response");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//     }
}
