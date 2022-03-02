package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.BufferedOutputStream;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.AbstractK8SJavaProxy;
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

/**
 * The Implementation of transport for the Abstract class AbstractK8SJavaProxy.
 * K8S (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public class TransportK8SJavaProxy extends AbstractK8SJavaProxy {
    
//    private String serverIP;
//    private String serverPort;
    private TransportConnector normalcl1;
    private TransportParameter param1;
    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     * @param tlsCheck check to use tls security
     *  
     */
    public TransportK8SJavaProxy(ProxyType proxyType, String serverIP, String serverPort, boolean tlsCheck) {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort, tlsCheck));
    }
    
    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     * @param transportK8STLS the tls security information
     * 
     * @throws IOException 
     *  
     */
    public TransportK8SJavaProxy(ProxyType proxyType, String serverIP, String serverPort,
            TransportK8STLS transportK8STLS) throws IOException {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort, transportK8STLS.isTlsCheck()));
//        this.transportPort = transportPort; 
//        this.serverIP = serverIP;
//        this.serverPort = serverPort;

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
        
        ServerAddress addr = new ServerAddress(Schema.HTTP, serverIP, Integer.parseInt(serverPort));
        TransportParameterBuilder tpb1 = TransportParameterBuilder.newBuilder(addr).setApplicationId("cl1");
        if (transportK8STLS.isTlsCheck()) {
            transportK8STLS.getConfigurer().configure(tpb1);
        }
        param1 = tpb1.build();
        
        normalcl1 = TransportFactory.createConnector();
        normalcl1.connect(param1);
        
    }

//    /**
//     * Returns the server IP.
//     * 
//     * @return the server IP
//     */
//    public String getServerIP() {
//        return serverIP;
//    }
//
//    /**
//     * Set the server IP.
//     *
//     * @param serverIP the server IP
//     */
//    public void setServerIP(String serverIP) {
//        this.serverIP = serverIP;
//    }
    
//    /**
//     * Returns the aas port.
//     * 
//     * @return the aas port
//     */
//    public String getServerPort() {
//        return serverPort;
//    }
//
//    /**
//     * Set the server port.
//     *
//     * @param serverPort the server port
//     */
//    public void setServerPort(String serverPort) {
//        this.serverPort = serverPort;
//    }
    
    /**
     * Returns the transport connector.
     * 
     * @return the transport connector
     */
    public TransportConnector getNormalcl1() {
        return normalcl1;
    }

    /**
     * Set the transport connector.
     *
     * @param normalcl1 the transport connector
     */
    public void setNormalcl1(TransportConnector normalcl1) {
        this.normalcl1 = normalcl1;
    }

    @Override
    public byte[] sendK8SRequest(BufferedOutputStream writer, K8SRequest request) throws IOException {

        byte[] response = null;

        if (getProxyType() == ProxyType.MasterProxy) {
            response = executeK8SJavaClientRequest(writer, request);
        } else {
            response = executeK8SGet(writer, request);
        }

        return response;
    }
    
    /**
     * Execute GET request and send it to the MasterProxy.
     *
     * @param writer       is the output buffer to send watch request stream
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent GET request
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public byte[] executeK8SGet(BufferedOutputStream writer, K8SRequest request)
            throws ClientProtocolException, IOException {
        
//        String response = null;
        byte[] responseByte = null;
        
        TransportMessage message1 = new TransportMessage(request.getPathNoParameter(), request.getRequestByte(),
                request.getPath().contains("&watch=true") ? "Yes" : "No");
        message1.generateStreamIdNo();
        
        SerializerRegistry.registerSerializer(TransportMessageJsonSerializer.class);

        try {
            TransportConnector cl1 = normalcl1;
            final String stream1;
            if (message1.getRequestWatch().equals("Yes")) {
                stream1 = cl1.composeStreamName("", "watchStream1");
            } else {
                stream1 = cl1.composeStreamName("", "stream1");
            }
            final String stream2 = cl1.composeStreamName("", message1.getStreamId());
            
            final CallbackMessage cb1 = new CallbackMessage();
            cl1.setReceptionCallback(stream2, cb1);
            
            cl1.syncSend(stream1, message1);

            int y = 0;
            while (cb1.dequeIsEmpty()) {
                y++;
                if (y % 30000 == 0) {
                    System.out.println(message1.getStreamId() + " Still Outter waiting Path>> " + request.getMethod()
                            + request.getPath());
                    return null;
                }
                TimeUtils.sleep(1);
            }
            
            responseByte = cb1.getData().getMessageByte();
            String responseString = new String(responseByte);
//            System.out.println(responseString);
            
            if (message1.getRequestWatch().equals("Yes")) {
                while (!responseString.contains("0\r\n" + "\r\n")) {
                    writer.write(responseByte);
                    writer.flush();
                    
                    int x = 0;
                    while (cb1.dequeIsEmpty()) {
                        x++;
                        if (x % 10000 == 0) {
                            System.out.println(message1.getStreamId() + " Still Inner waiting Path>> "
                                    + request.getMethod() + request.getPath());
                            return ("0\r\n" + "\r\n").getBytes();
                        }
                        
                        TimeUtils.sleep(1);
                    }
                    
                    responseByte = cb1.getData().getMessageByte();
                    responseString = new String(responseByte);
                }
            }
            cl1.unsubscribe(stream2, false);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        SerializerRegistry.unregisterSerializer(TransportMessageJsonSerializer.class);

        return responseByte;
    }

    /**
     * Execute POST request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent POST request
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public byte[] executeK8SPost(K8SRequest request) throws ClientProtocolException, IOException {
        
        return null;
    }

    /**
     * Execute PUT request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent PUT request
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public byte[] executeK8SPut(K8SRequest request) throws ClientProtocolException, IOException {
        
        return null;
    }

    /**
     * Execute PATCH request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent PATCH request
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public byte[] executeK8SPatch(K8SRequest request) throws ClientProtocolException, IOException {

        return null;
    }

    /**
     * Execute DELETE request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent DELETE request
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public byte[] executeK8SDelete(K8SRequest request) throws ClientProtocolException, IOException {

        return null;
    }

}
