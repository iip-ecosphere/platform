package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.util.Arrays;

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
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.RegistryDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import okhttp3.Response;
import okio.Buffer;

/**
 * Create client side aas for the worker node.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class MasterK8SAas {

    private static String serverIP;
    private static String serverPort;
    private static int aasPort;
    private static Map<String, Response> requestMapIDs = new ConcurrentHashMap<>();
    private static K8SJavaProxy aasK8SJavaProxy;
    private int vabPort;

    /**
     * Creates a Client K8S aas instance.
     * 
     * @param serverIP   the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S
     *                   apiserver port)
     * @param vabPort    the vab port
     * @param aasPort    the aas port
     * @param tlsCheck check to use tls security
     * 
     */
    public MasterK8SAas(String serverIP, String serverPort, int vabPort, int aasPort, boolean tlsCheck) {
        MasterK8SAas.serverIP = serverIP;
        MasterK8SAas.serverPort = serverPort;
        this.vabPort = vabPort;
        MasterK8SAas.aasPort = aasPort;
        aasK8SJavaProxy = new AasK8SJavaProxy(ProxyType.MasterProxy, aasPort, serverIP, serverPort, tlsCheck);
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
     * @param serverPort the port of the server (either the Aas port or K8S
     *                   apiserver port)
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
        pBuilder.defineOperation(MasterAasCreator.AAS_SUBMODEL_OPERATION_SEND_WATCH_TO_K8S,
            params -> sendWatchToK8S(params));
        Server server = pBuilder.build();
        server.start();

        Server httpServer = AasFactory.getInstance().createDeploymentRecipe(aasServerBase)
                .addInMemoryRegistry(aasServerRegistry.getEndpoint()).deploy(aas).createServer().start();

        ArrayList<Server> servers = new ArrayList<Server>();
        servers.add(httpServer);
        servers.add(server);

        return servers;
    }

    /**
     * Start the local TLS aas.
     * 
     * @return the running servers
     * @throws IOException 
     * 
     */
    public ArrayList<Server> startLocalTLSAas() throws IOException {

        File keyPath = new File("./src/test/resources/keystore.jks");
        KeyStoreDescriptor kstore = new KeyStoreDescriptor(keyPath, "a1234567", "tomcat");
                
        ServerAddress vabServer = new ServerAddress(Schema.HTTPS, serverIP, vabPort);
        ServerAddress aasServer = new ServerAddress(Schema.HTTP, serverIP, aasPort);
        ServerAddress aasServerS = new ServerAddress(Schema.HTTPS, serverIP, 1234);

        Aas aas = MasterAasCreator.createAas(vabServer);

        ProtocolServerBuilder pBuilder = AasFactory.getInstance()
                .createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL, vabServer.getPort());
        pBuilder.defineProperty(MasterAasCreator.AAS_SUBMODEL_PROPERTY_NAME, () -> "K8SAasProperty", null);
        pBuilder.defineProperty(MasterAasCreator.AAS_SUBMODEL_PROPERTY_VERSION, () -> "0.0.1", null);
        pBuilder.defineProperty(MasterAasCreator.AAS_SUBMODEL_PROPERTY_DESCRIPTION, () -> "K8S AAS", null);
        pBuilder.defineOperation(MasterAasCreator.AAS_SUBMODEL_OPERATION_SEND_TO_K8S, params -> sendToK8S(params));
        pBuilder.defineOperation(MasterAasCreator.AAS_SUBMODEL_OPERATION_SEND_WATCH_TO_K8S,
            params -> sendWatchToK8S(params));
        Server server = pBuilder.build();
        server.start();

        AasFactory factory = AasFactory.getInstance();

        ServerRecipe srcp = factory.createServerRecipe();
        
        // start a registry server
        Endpoint regEp = new Endpoint(aasServer, "registry");
        Server regServer = srcp.createRegistryServer(regEp, LocalPersistenceType.INMEMORY, kstore).start();
        
        // Start target deployment server and connect to the registry
        Endpoint serverEp = new Endpoint(aasServerS, "cloud");
        RegistryDeploymentRecipe regD = factory.createDeploymentRecipe(serverEp, kstore)
            .setRegistryUrl(regEp);
        Registry reg = regD.obtainRegistry();
        AasServer cloudServer = regD.createServer().start();
        
        reg.createAas(aas, serverEp.toUri());
        reg.createSubmodel(aas, aas.getSubmodel("MasterK8SAasService"));

        ArrayList<Server> servers = new ArrayList<Server>();
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

//        K8SJavaProxy aasK8SJavaProxy = new AasK8SJavaProxy(ProxyType.MasterProxy, aasPort, serverIP, serverPort);

        K8SRequest request = new K8SRequest();
        byte[] requestByte = request.convertBase64StringToByte(requestString);

        request = aasK8SJavaProxy.createK8SRequest(requestByte);

//        request.convertStringToRequest(requestString);
        // aasK8SJavaProxy.createK8SRequest(requestString.getBytes(StandardCharsets.UTF_8));

        byte[] response = null;
        try {
            response = aasK8SJavaProxy.executeK8SJavaClientRequest(null, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String responseBase64String = request.convertByteArrayToBase64String(response);
        return responseBase64String;
    }

    /**
     * get response to request.
     * 
     * @param params the call parameters, only the first is evaluated
     * 
     * @return the response string
     * 
     */
    private static Object sendWatchToK8S(Object[] params) {
        String requestString = null;
        if (params.length > 0 && params[0] != null) {
            requestString = params[0].toString();
        }
        K8SRequest request = new K8SRequest();
        String[] requestArray = requestString.split("\\*");
        String requestID = request.convertBase64StringToString(requestArray[0]);
        byte[] requestByte = request.convertBase64StringToByte(requestArray[1]);
        Response response = null;
        byte[] responseBody = new byte[0];
        try {
            if (requestMapIDs.containsKey(requestID)) {
                response = requestMapIDs.get(requestID);
                if (!response.body().source().exhausted()) {
                    String formattedHeaderResponse = request.getProtocol() + " " + response.code() + " "
                            + response.message() + "\r\n" + response.headers().toString() + "\r\n";
                    if (formattedHeaderResponse.contains("application/vnd.kubernetes.protobuf")) {
                        Buffer buffer = new Buffer();
                        response.body().source().read(buffer, 4096);
                        responseBody = buffer.readByteArray();
                    } else {
                        long count = response.body().source().indexOf((byte) '\n');
                        responseBody = response.body().source().readByteArray(count + 1);
                    }
                    byte[] firstPart = (Integer.toHexString(responseBody.length) + "\r\n").getBytes();
                    responseBody = Arrays.concatenate(responseBody, "\r\n".getBytes());
                    responseBody = Arrays.concatenate(firstPart, responseBody);
                }
            } else {
                request = aasK8SJavaProxy.createK8SRequest(requestByte);
                response = aasK8SJavaProxy.executeWatchK8SJavaClientRequest(request);
                String formattedResponse = request.getProtocol() + " " + response.code() + " " + response.message()
                        + "\r\n" + response.headers().toString() + "\r\n";
                responseBody = formattedResponse.getBytes();
                if (!response.body().source().exhausted()) {
                    if (formattedResponse.contains("application/vnd.kubernetes.protobuf")) {
                        Buffer buffer = new Buffer();
                        response.body().source().read(buffer, 4096);
                        responseBody = buffer.readByteArray();
                    } else {
                        long count = response.body().source().indexOf((byte) '\n');
                        responseBody = response.body().source().readByteArray(count + 1);
                    }
                    byte[] firstPart = (Integer.toHexString(responseBody.length) + "\r\n").getBytes();
                    responseBody = Arrays.concatenate(responseBody, "\r\n".getBytes());
                    responseBody = Arrays.concatenate(firstPart, responseBody);
                    responseBody = Arrays.concatenate(formattedResponse.getBytes(), responseBody);
                }
                requestMapIDs.put(requestID, response);
            }
            if (responseBody == null || responseBody.length == 0) {
                responseBody = ("0\r\n" + "\r\n").getBytes();
                response.body().close();
                requestMapIDs.remove(requestID);
            }
        } catch (SocketTimeoutException e) {
            if (e.getMessage().contentEquals("timeout") || e.getMessage().contentEquals("Read timed out")) {
                if (responseBody.length == 0) {
                    responseBody = ("0\r\n" + "\r\n").getBytes();
                } else {
                    responseBody = Arrays.concatenate(responseBody, ("0\r\n" + "\r\n").getBytes());
                }
                requestMapIDs.remove(requestID);
                response.body().close();
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            responseBody = ("0\r\n" + "\r\n").getBytes();
            response.body().close();
            requestMapIDs.remove(requestID);
        }
        if (responseBody == null || responseBody.length == 0) {
            responseBody = ("0\r\n" + "\r\n").getBytes();
            System.out.println("Empty response AAS execute");
        }
        return request.convertByteArrayToBase64String(responseBody);
    }
}
