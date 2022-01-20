package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
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
import okhttp3.Response;

/**
 * The Implementation of AAS for the Abstract class AbstractK8SJavaProxy.
 * K8S (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public class AasK8SJavaProxy extends AbstractK8SJavaProxy {
    
    private int aasPort;
    private Submodel submodel;
    private Aas aas;
    private Submodel watchSubmodel;
    private Aas watchAas;
    private boolean tlsCheck;
    private String serverIP;
    private String serverPort;
    
    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param aasPort the aas port
     * @param serverIP the IP Address of the server
     * @param serverPort the port of the server (either the Aas port or K8S apiserver port)
     * @param tlsCheck check to use tls security
     *  
     */
    public AasK8SJavaProxy(ProxyType proxyType, int aasPort, String serverIP, String serverPort, boolean tlsCheck) {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort, tlsCheck));
        this.aasPort = aasPort;
        this.tlsCheck = tlsCheck;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        
        if (proxyType.equals(ProxyType.WorkerProxy)) {
            ServerAddress aasServer = new ServerAddress(Schema.HTTP, serverIP, Integer.parseInt(serverPort));
            Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);
            
            AasFactory factory = AasFactory.getInstance();
            
            try {
                if (tlsCheck) {
                    aas = factory.obtainRegistry(aasServerRegistry, Schema.HTTPS)
                            .retrieveAas("urn:::AAS:::MasterK8SAas#");
                } else {
                    aas = factory.obtainRegistry(aasServerRegistry).retrieveAas("urn:::AAS:::MasterK8SAas#");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            submodel = aas.getSubmodel("MasterK8SAasService");
            
            AasFactory watchFactory = AasFactory.getInstance();
            
            try {
                if (tlsCheck) {
                    watchAas = watchFactory.obtainRegistry(aasServerRegistry, Schema.HTTPS)
                            .retrieveAas("urn:::AAS:::MasterK8SAas#");
                } else {
                    watchAas = watchFactory.obtainRegistry(aasServerRegistry).retrieveAas("urn:::AAS:::MasterK8SAas#");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            watchSubmodel = watchAas.getSubmodel("MasterK8SAasService");
        }

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
        
        String response = null;
        try {
            if (request.getPath().contains("&watch=true")) {
                                
                Operation operation = watchSubmodel.getOperation("sendWatchToK8S");

                String requestBase64String = request.convertToBase64StringWithID();
                response = (String) operation.invoke(requestBase64String);
                
                byte[] responseByte = request.convertBase64StringToByte(response);
                String responseString = new String(responseByte);
                
                while (!responseString.contains("0\r\n\r\n")) {
                    writer.write(responseByte);
                    writer.flush();
                    
                    response = (String) operation.invoke(requestBase64String);
                    
                    responseByte = request.convertBase64StringToByte(response);
                    responseString = new String(responseByte);
                }
                
            } else {
                Operation operation = submodel.getOperation("sendToK8S");
                response = (String) operation.invoke(request.convertToBase64String());
            }
                        
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        }

        return request.convertBase64StringToByte(response);
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

    /**
     * Format the response from K8S apiserver.
     *
     * @param request  the K8S request object (K8SRequest)
     * @param response the response from the K8S apiserver
     * @param writer
     * 
     * @return the formated response
     * @throws IOException
     */
    public String formatWatchK8SResponse(BufferedWriter writer, K8SRequest request, Response response)
            throws IOException {
        String responseBody = "";

        String formattedResponse = request.getProtocol() + " " + response.code() + " " + response.message() + "\r\n"
                + response.headers().toString();
        
        try {
            if (!response.body().source().exhausted()) {
                responseBody = response.body().source().readUtf8Line() + "\r\n";
                
                responseBody = Integer.toHexString(responseBody.length()) 
                        + "\r\n" 
                        + responseBody
                        + "\r\n";
                
                formattedResponse = formattedResponse + "\r\n" + responseBody;
                
                writer.write(formattedResponse);

                writer.flush();
            }
            while (!response.body().source().exhausted()) {
                responseBody = response.body().source().readUtf8Line() + "\r\n";
                
                responseBody = Integer.toHexString(responseBody.length()) 
                        + "\r\n" 
                        + responseBody
                        + "\r\n";
                
                writer.write(responseBody);

                writer.flush();
            }
        } catch (SocketTimeoutException e) {
            if (e.getMessage().contentEquals("timeout") || e.getMessage().contentEquals("Read timed out")) {
                System.out.println(e.getMessage());
            }
        } finally {
            response.body().close();
        }
        
        if (!responseBody.equals("")) {
            formattedResponse = "0\r\n" 
                              + "\r\n";
        } else {
            formattedResponse = formattedResponse 
                    + "\r\n" 
                    + "0\r\n" 
                    + "\r\n";
        }

//        response.body().close();
        
        return formattedResponse;
    }
}
