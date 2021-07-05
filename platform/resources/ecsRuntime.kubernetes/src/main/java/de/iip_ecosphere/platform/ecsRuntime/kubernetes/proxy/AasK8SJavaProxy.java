package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.ClientProtocolException;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * The Implementation of http apache for the Abstract class AbstractK8SJavaProxy.
 * K8S (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public class AasK8SJavaProxy extends AbstractK8SJavaProxy {
    
    private int aasPort;
    
    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param aasPort the aas port
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     *  
     */
    public AasK8SJavaProxy(ProxyType proxyType, int aasPort, String serverIP, String serverPort) {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort));
        this.aasPort = aasPort;
    }

    /**
     * Returns the aas port.
     * 
     * @return the aas port
     */
    public int getAasPort() {
        return aasPort;
    }

    /**
     * Set the aas port.
     *
     * @param aasPort the aas port
     */
    public void setAasPort(int aasPort) {
        this.aasPort = aasPort;
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
        try {
            ServerAddress aasServer = new ServerAddress(Schema.HTTP, aasPort);
            Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

            System.out.println(aasServerRegistry.getEndpoint());
            
            AasFactory factory = AasFactory.getInstance();
            Aas aas = factory.obtainRegistry(aasServerRegistry).retrieveAas(WorkerAasCreator.URN_AAS);

            Submodel submodel = aas.getSubmodel(WorkerAasCreator.AAS_SUBMODEL_NAME);
            
            Operation operation = submodel.getOperation(WorkerAasCreator.AAS_SUBMODEL_OPERATION_SEND_TO_AAS);
            response = (String) operation.invoke(request.convertToString());
            
        } catch (IOException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
