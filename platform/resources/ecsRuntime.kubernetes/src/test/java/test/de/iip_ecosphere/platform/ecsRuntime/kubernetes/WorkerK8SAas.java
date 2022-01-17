package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy.WorkerAasCreator;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Create client side aas for the worker node.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class WorkerK8SAas {

    private static String serverIP;
    private static String serverPort;
    private static Submodel submodel;
    private int vabPort;
    private int aasPort;
    
    /**
     * Creates a Client K8S aas instance.
     * 
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     * @param vabPort the vab port
     * @param aasPort the aas port
     * 
     */
    public WorkerK8SAas(String serverIP, String serverPort, int vabPort, int aasPort) {
        WorkerK8SAas.serverIP = serverIP;
        WorkerK8SAas.serverPort = serverPort;
        this.vabPort = vabPort;
        this.aasPort = aasPort;
        
        ServerAddress aasServer = new ServerAddress(Schema.HTTP, serverIP, Integer.parseInt(serverPort));
        
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

//        System.out.println(aasServerRegistry.getEndpoint());
        
        AasFactory factory = AasFactory.getInstance();
        Aas aas = null;
        try {
            aas = factory.obtainRegistry(aasServerRegistry).retrieveAas("urn:::AAS:::MasterK8SAas#");
        } catch (IOException e) {
            e.printStackTrace();
        }

        submodel = aas.getSubmodel("MasterK8SAasService");
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
        WorkerK8SAas.serverIP = serverIP;
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
        WorkerK8SAas.serverPort = serverPort;
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
        this.aasPort = aasPort;
    }
    
    /**
     * Start the local aas.
     * 
     * @return the running servers
     * 
     */
    public ArrayList<Server> startLocalAas() {

        ServerAddress vabServer = new ServerAddress(Schema.HTTP, vabPort);
        ServerAddress aasServer = new ServerAddress(Schema.HTTP, aasPort);
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);
        
        Aas aas = WorkerAasCreator.createAas(vabServer);
        
        ProtocolServerBuilder pBuilder = AasFactory.getInstance()
            .createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, vabServer.getPort());
        pBuilder.defineProperty(WorkerAasCreator.AAS_SUBMODEL_PROPERTY_NAME, () -> "K8SAasProperty", null);
        pBuilder.defineProperty(WorkerAasCreator.AAS_SUBMODEL_PROPERTY_VERSION, () -> "0.0.1", null);
        pBuilder.defineProperty(WorkerAasCreator.AAS_SUBMODEL_PROPERTY_DESCRIPTION, () -> "K8S AAS", null);
        pBuilder.defineOperation(WorkerAasCreator.AAS_SUBMODEL_OPERATION_SEND_TO_AAS, params -> sendToAAS(params));
        pBuilder.defineOperation(WorkerAasCreator.AAS_SUBMODEL_OPERATION_SEND_WATCH_TO_AAS,
            params -> sendWatchToAAS(params));
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
    private static Object sendToAAS(Object[] params) {
        
        String response = null;
        String requestString = null;
        try {
            if (params.length > 0 && params[0] != null) {
                requestString = params[0].toString();
            }
            
            Operation operation = submodel.getOperation("sendToK8S");
            response = (String) operation.invoke(requestString);
            
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * get response to request.
     * 
     * @param params the call parameters, only the first is evaluated
     * 
     * @return the response string
     * 
     */
    private static Object sendWatchToAAS(Object[] params) {
        
        String response = null;
        String requestString = null;
        try {
            if (params.length > 0 && params[0] != null) {
                requestString = params[0].toString();
            }
                
            Operation operation = submodel.getOperation("sendWatchToK8S");
            response = (String) operation.invoke(requestString);
            
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        
        return response;
    }

}
