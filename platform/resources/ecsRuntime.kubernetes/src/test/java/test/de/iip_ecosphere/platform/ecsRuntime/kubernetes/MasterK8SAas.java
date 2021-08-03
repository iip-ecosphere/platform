package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.WorkerAasCreator;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.AasK8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SJavaProxy;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.K8SRequest;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.MasterAasCreator;
import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.ProxyType;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Create client side aas for the worker node.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class MasterK8SAas {

    private static String serverIP;
    private static String serverPort;
    private static int aasPort;
    private int vabPort;
    
    /**
     * Creates a Client K8S aas instance.
     * 
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     * @param vabPort the vab port
     * @param aasPort the aas port
     * 
     */
    public MasterK8SAas(String serverIP, String serverPort, int vabPort, int aasPort) {
        MasterK8SAas.serverIP = serverIP;
        MasterK8SAas.serverPort = serverPort;
        this.vabPort = vabPort;
        MasterK8SAas.aasPort = aasPort;
    }

    /**
     * Returns the IP Address of the server.
     * 
     * @return the IP Address of the server
     */   
    public String getServerIP() {
        return serverIP;
    }
    
    /**
     * Set the IP Address of the server.
     *
     * @param serverIP the IP Address of the server
     */
    public void setServerIP(String serverIP) {
        MasterK8SAas.serverIP = serverIP;
    }

    /**
     * Returns the port of the server (either the Aas port or K8S apiserver port).
     * 
     * @return the port of the server (either the Aas port or K8S apiserver port)
     */   
    public String getServerPort() {
        return serverPort;
    }

    /**
     * Set the port of the server (either the Aas port or K8S apiserver port).
     *
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     */
    public void setServerPort(String serverPort) {
        MasterK8SAas.serverPort = serverPort;
    }

    /**
     * Returns the vab port.
     * 
     * @return the vab port
     */
    public int getVabPort() {
        return vabPort;
    }

    /**
     * Set the vab port.
     *
     * @param vabPort the vab port
     */
    public void setVabPort(int vabPort) {
        this.vabPort = vabPort;
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
        MasterK8SAas.aasPort = aasPort;
    }
    
    /**
     * Start the local aas.
     * 
     * @return the running servers
     * 
     */
    public ArrayList<Server> startLocalAas() {

        ServerAddress vabServer = new ServerAddress(Schema.HTTP, serverIP, vabPort);
        ServerAddress aasServer = new ServerAddress(Schema.HTTP, serverIP, aasPort);
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);
        
        Aas aas = MasterAasCreator.createAas(vabServer);
        
        ProtocolServerBuilder pBuilder = AasFactory.getInstance()
            .createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, vabServer.getPort());
        pBuilder.defineProperty(MasterAasCreator.AAS_SUBMODEL_PROPERTY_NAME, () -> "K8SAasProperty", null);
        pBuilder.defineProperty(MasterAasCreator.AAS_SUBMODEL_PROPERTY_VERSION, () -> "0.0.1", null);
        pBuilder.defineProperty(MasterAasCreator.AAS_SUBMODEL_PROPERTY_DESCRIPTION, () -> "K8S AAS", null);
        pBuilder.defineOperation(MasterAasCreator.AAS_SUBMODEL_OPERATION_SEND_TO_K8S, params -> sendToK8S(params));
        Server server = pBuilder.build();
        server.start();
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
        
        ArrayList<Server> servers = new ArrayList<Server>();
        servers.add(httpServer);
        servers.add(server);
        
        return servers;
    }
    
    /**
     * get response to request.
     * 
     * @param params the call parameters, only the first is evaluated
     * 
     * @return the response string
     * 
     */
    private static Object sendToK8S(Object[] params) {

        String requestString = null;

        if (params.length > 0 && params[0] != null) {
            requestString = params[0].toString();
        }

        K8SJavaProxy aasK8SJavaProxy = new AasK8SJavaProxy(ProxyType.MasterProxy, aasPort, serverIP, serverPort);
        
        K8SRequest request = new K8SRequest();
        byte[] requestByte = request.convertBase64StringToByte(requestString);
        
        request = aasK8SJavaProxy.createK8SRequest(requestByte);
        
//        request.convertStringToRequest(requestString);
        //aasK8SJavaProxy.createK8SRequest(requestString.getBytes(StandardCharsets.UTF_8));
        
        String response = null;
        try {
            response = aasK8SJavaProxy.executeK8SJavaClientRequest(request);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

}
