package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.AbstractK8SJavaProxy;
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

/**
 * The Implementation of Mqtt for the Abstract class AbstractK8SJavaProxy.
 * K8S (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public class MqttK8SJavaProxy extends AbstractK8SJavaProxy {
    
    private String serverIP;
    private String serverPort;
    private int mqttPort;

    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     *  
     */
    public MqttK8SJavaProxy(ProxyType proxyType, String serverIP, String serverPort) {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort));
    }
    
    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     * @param mqttPort the local mqtt server port
     *  
     */
    public MqttK8SJavaProxy(ProxyType proxyType, String serverIP, String serverPort, int mqttPort) {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort));
        this.mqttPort = mqttPort;
    }

    /**
     * Returns the server IP.
     * 
     * @return the server IP
     */
    public String getServerIP() {
        return serverIP;
    }

    /**
     * Set the server IP.
     *
     * @param serverIP the server IP
     */
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }
    
    /**
     * Returns the aas port.
     * 
     * @return the aas port
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
        this.serverPort = serverPort;
    }
    
    @Override
    public String sendK8SRequest(K8SRequest request) throws IOException {

        String response = null;

        if (getProxyType() == ProxyType.MasterProxy) {
            response = executeK8SJavaClientRequest(request);
        } else {
            response = executeK8SGet(request);
        }

        return response;
    }
    
    /**
     * Execute GET request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent GET request
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public String executeK8SGet(K8SRequest request) throws ClientProtocolException, IOException {
        
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

        ServerAddress addr = new ServerAddress(Schema.IGNORE, mqttPort); 

        MqttMessage message1 = new MqttMessage("PCstream", request.convertToBase64String());
        message1.generateStreamIdNo();
        
        SerializerRegistry.registerSerializer(MqttMessageJsonSerializer.class);
        TransportParameter param1 = TransportParameterBuilder.newBuilder(addr)
                .setApplicationId("cl1").build();
        TransportConnector cl1 = TransportFactory.createConnector();
        try {
            cl1.connect(param1);
            
            final String stream1 = cl1.composeStreamName("", "stream1");
            final String stream2 = cl1.composeStreamName("", message1.getStreamId());
            
            cl1.syncSend(stream1, message1);
            
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

        return response;
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
    public String executeK8SPost(K8SRequest request) throws ClientProtocolException, IOException {
        
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
    public String executeK8SPut(K8SRequest request) throws ClientProtocolException, IOException {
        
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
    public String executeK8SPatch(K8SRequest request) throws ClientProtocolException, IOException {

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
    public String executeK8SDelete(K8SRequest request) throws ClientProtocolException, IOException {

        return null;
    }

}
