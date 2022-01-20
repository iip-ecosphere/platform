package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

/**
 * The Implementation of http apache for the Abstract class
 * AbstractK8SJavaProxy. K8S (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public class HttpK8SJavaProxy extends AbstractK8SJavaProxy {

    private CloseableHttpClient httpClient;
    private boolean tlsCheck;
    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or
     * WorkerProxy. If it is MasterProxy then IP address and port will be to the K8S
     * apiserver address and port If it is WorkerProxy then IP address and port will
     * be to the MasterProxy address and port
     * 
     * @param proxyType  the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP   the IP Address of the server (either the MasterProxy or K8S
     *                   apiserver)
     * @param serverPort the port of the server (either the MasterProxy or K8S
     *                   apiserver)
     * @param tlsCheck        check to use tls security
     * @throws IOException 
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     * 
     */
    public HttpK8SJavaProxy(ProxyType proxyType, String serverIP, String serverPort, boolean tlsCheck)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException {
        super(proxyType, getServerAddress(proxyType, serverIP, serverPort, tlsCheck));
        this.tlsCheck = tlsCheck;
        
        if (tlsCheck) {
            httpClient = getSSLHttpClients(); 
        } else {
            httpClient = HttpClients.createDefault();
        }
    }

    /**
     * Execute GET request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * @param writer is the output buffer to send watch request stream
     * 
     * @return the response from the MasterProxy for sent GET request
     * @throws IOException
     * @throws ClientProtocolException
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public byte[] executeK8SGet(BufferedOutputStream writer, K8SRequest request) throws ClientProtocolException,
            IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {
        String url = getServerAddress() + request.getPath();

//        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGetRequest = new HttpGet(url);

        for (String[] value : request.getHeaders().values()) {
            httpGetRequest.addHeader(value[0], value[1]);
        }

        byte[] response = null;
        if (request.getPath().contains("&watch=true")) {
            CloseableHttpClient httpNewClient = null;
            if (tlsCheck) {
                httpNewClient = getSSLHttpClients();
            } else {
                httpNewClient = HttpClients.createDefault();
            }
            HttpResponse httpResponse = httpNewClient.execute(httpGetRequest);
            response = formatWatchK8SResponse(writer, request, httpResponse);
            httpNewClient.close();
        } else {
            HttpResponse httpResponse = httpClient.execute(httpGetRequest);
            response = formatK8SResponse(request, httpResponse);
        }
//        String requestString = new String(response);
//        System.out.println(requestString); 
//        String response = formatK8SResponse(request, httpResponse);

        httpGetRequest.releaseConnection();
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
    public byte[] executeK8SPost(K8SRequest request) throws ClientProtocolException, IOException {
        String url = getServerAddress() + request.getPath();

//        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPostRequest = new HttpPost(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpPostRequest.addHeader(value[0], value[1]);
            }
        }
        httpPostRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpPostRequest);

        byte[] response = formatK8SResponse(request, httpResponse);

        httpPostRequest.releaseConnection();
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
    public byte[] executeK8SPut(K8SRequest request) throws ClientProtocolException, IOException {
        String url = getServerAddress() + request.getPath();

//        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPutRequest = new HttpPut(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpPutRequest.addHeader(value[0], value[1]);
            }
        }
        httpPutRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpPutRequest);

        byte[] response = formatK8SResponse(request, httpResponse);

        httpPutRequest.releaseConnection();
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
    public byte[] executeK8SPatch(K8SRequest request) throws ClientProtocolException, IOException {
        String url = getServerAddress() + request.getPath();

//        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatchRequest = new HttpPatch(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpPatchRequest.addHeader(value[0], value[1]);
            }
        }
        httpPatchRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpPatchRequest);

        byte[] response = formatK8SResponse(request, httpResponse);

        httpPatchRequest.releaseConnection();
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
    public byte[] executeK8SDelete(K8SRequest request) throws ClientProtocolException, IOException {
        String url = getServerAddress() + request.getPath();

//        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDeleteWithBody httpDeleteRequest = new HttpDeleteWithBody(url);

        for (String[] value : request.getHeaders().values()) {
            if (!value[0].toUpperCase().contains("CONTENT-LENGTH")) {
                httpDeleteRequest.addHeader(value[0], value[1]);
            }
        }
        httpDeleteRequest.setEntity(new ByteArrayEntity(request.getPayload()));

        HttpResponse httpResponse = httpClient.execute(httpDeleteRequest);

        byte[] response = formatK8SResponse(request, httpResponse);

        httpDeleteRequest.releaseConnection();
        return response;
    }

    /**
     * Get new connection with SSL security.
     * 
     * @return httpClient the secured connection
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws KeyManagementException
     */
    private CloseableHttpClient getSSLHttpClients() throws NoSuchAlgorithmException, KeyStoreException,
            CertificateException, IOException, KeyManagementException {
        SSLContextBuilder sslBuilder = SSLContexts.custom();
        File file = new File("./src/test/resources/keystore.jks");

        sslBuilder = sslBuilder.loadTrustMaterial(file, "a1234567".toCharArray());
        SSLContext sslcontext = sslBuilder.build();

        SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslcontext,
                new NoopHostnameVerifier());

        HttpClientBuilder clientbuilder = HttpClients.custom();

        clientbuilder = clientbuilder.setSSLSocketFactory(sslConSocFactory);

        CloseableHttpClient httpClient = clientbuilder.build();

        return httpClient;
    }
}
