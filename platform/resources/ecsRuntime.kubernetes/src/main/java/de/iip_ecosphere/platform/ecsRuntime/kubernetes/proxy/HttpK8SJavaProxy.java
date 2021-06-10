package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * The Implementation of http apache for the Abstract class AbstractK8SJavaProxy.
 * K8S (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public class HttpK8SJavaProxy extends AbstractK8SJavaProxy {

    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or WorkerProxy.
     * If it is MasterProxy then IP address and port will be to the K8S apiserver address and port
     * If it is WorkerProxy then IP address and port will be to the MasterProxy address and port
     * 
     * @param proxyType the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP the IP Address of the server (either the MasterProxy or K8S apiserver)
     * @param serverPort the port of the server (either the MasterProxy or K8S apiserver)
     * 
     */
    public HttpK8SJavaProxy(ProxyType proxyType, String serverIP, String serverPort) {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort));
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
        String url = getServerAddress() + request.getPath();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGetRequest = new HttpGet(url);

        for (String[] value : request.getHeaders().values()) {
            httpGetRequest.addHeader(value[0], value[1]);
        }

        HttpResponse httpResponse = httpClient.execute(httpGetRequest);

        String response = formatK8SResponse(request, httpResponse);
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
        String url = getServerAddress() + request.getPath();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPostRequest = new HttpPost(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpPostRequest.addHeader(value[0], value[1]);
            }
        }
        httpPostRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpPostRequest);

        String response = formatK8SResponse(request, httpResponse);
        return response;
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
        String url = getServerAddress() + request.getPath();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPutRequest = new HttpPut(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpPutRequest.addHeader(value[0], value[1]);
            }
        }
        httpPutRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpPutRequest);

        String response = formatK8SResponse(request, httpResponse);
        return response;
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
        String url = getServerAddress() + request.getPath();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatchRequest = new HttpPatch(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpPatchRequest.addHeader(value[0], value[1]);
            }
        }
        httpPatchRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpPatchRequest);

        String response = formatK8SResponse(request, httpResponse);
        return response;
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
        String url = getServerAddress() + request.getPath();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDeleteWithBody httpDeleteRequest = new HttpDeleteWithBody(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpDeleteRequest.addHeader(value[0], value[1]);
            }
        }
        httpDeleteRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpDeleteRequest);

        String response = formatK8SResponse(request, httpResponse);
        return response;
    }

}
