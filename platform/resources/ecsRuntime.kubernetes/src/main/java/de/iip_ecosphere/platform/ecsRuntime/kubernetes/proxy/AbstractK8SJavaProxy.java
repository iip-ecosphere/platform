package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.SSLUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

/**
 * An abstract for K8SJavaProxy Interface to make shared methods. K8S
 * (Kubernetes)
 * 
 * @author Ahmad Alamoush, SSE
 */
public abstract class AbstractK8SJavaProxy implements K8SJavaProxy {

    private ProxyType proxyType;
    private String serverAddress;

    /**
     * Creates a K8S java proxy instance, it will be either MasterProxy or
     * WorkerProxy. If it is MasterProxy then serverAddress will be to the K8S
     * apiserver address and port If it is WorkerProxy then serverAddress will be to
     * the MasterProxy address and port
     * 
     * @param proxyType     the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverAddress the address of the MasterProxy or K8S apiserver
     * 
     */
    public AbstractK8SJavaProxy(ProxyType proxyType, String serverAddress) {
        this.proxyType = proxyType;
        this.serverAddress = serverAddress;
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
     * Returns the serverAddress which K8S java proxy is connect to.
     * 
     * @return the serverAddress which K8S java proxy is connect to
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Set the serverAddress which K8S java proxy is connect to.
     *
     * @param serverAddress the serverAddress which K8S java proxy is connect to
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Create server socket for specified port on localhost to receive new requests.
     *
     * @param localPort       the port on the localhost
     * @param certificatePath the path of certificate used to create the socket.
     * @param keyPath         the path of key used to create the socket
     * @param algo            the algorithm used for the key
     * 
     * @return the created server socket for the specified port on localhost
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public ServerSocket getServerSocket(int localPort, String certificatePath, String keyPath, String algo)
            throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, InvalidKeySpecException, KeyManagementException {

        if (proxyType.equals(ProxyType.MasterProxy)) {
            return new ServerSocket(localPort);
        }

        String keyFile = null;
        String certificateFile = null;
        String algorithm = null;

        if (certificatePath == null || keyPath == null || algo == null) {
            keyFile = "apiserver.key";
            certificateFile = "apiserver.crt";
            algorithm = "RSA";
        }

        byte[] certificate = Files.readAllBytes(Paths.get(certificateFile));
        byte[] key = Files.readAllBytes(Paths.get(keyFile));

        final KeyManager[] keyManagers = SSLUtils.keyManagers(certificate, key, algorithm, "", null, null);

        SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(keyManagers, null, null);

        ServerSocket serverSocket = sslContext.getServerSocketFactory().createServerSocket(localPort);

        return serverSocket;
    }

    /**
     * Extract the request as array of bytes from the InputStream of the localhost
     * server socket.
     *
     * @param reader the InputStream for the port on the localhost
     * 
     * @return the request as array of bytes
     * @throws IOException
     */
    public byte[] extractK8SRequestByte(InputStream reader) throws IOException {
        byte[] request = new byte[4096];
        int bytesRead = 0;
        bytesRead = reader.read(request);

        if (bytesRead == -1) {
            return null;
        }

        byte[] requestByte = new byte[bytesRead];
        System.arraycopy(request, 0, requestByte, 0, bytesRead);
        String requestFirstString = new String(request);
        int requestLength = 0;
        boolean isHeader = true;
        int headerSize = 0;
        for (String string : requestFirstString.split("\r\n")) {
            if (string.toUpperCase().contains("CONTENT-LENGTH")) {
                requestLength = Integer.parseInt(string.substring(16));
            }
            if (isHeader) {
                if (string.getBytes().length > 0) {
                    headerSize = headerSize + string.getBytes().length + "\r\n".getBytes().length;
                } else {
                    headerSize = headerSize + string.getBytes().length + "\r\n".getBytes().length;
                    isHeader = false;
                }
            }
        }

        while (requestLength > bytesRead - headerSize) {
            bytesRead = reader.read(request);
            byte[] tempRequestByte = new byte[bytesRead + requestByte.length];
            System.arraycopy(requestByte, 0, tempRequestByte, 0, requestByte.length);
            System.arraycopy(request, 0, tempRequestByte, requestByte.length, bytesRead);
            bytesRead = tempRequestByte.length;
            requestByte = tempRequestByte;
        }

        return requestByte;
    }

    /**
     * Create K8S request object from the actual array of bytes for the received
     * request.
     *
     * @param requestByte the request as array of bytes
     * 
     * @return the new created K8S request object (K8SRequest)
     */
    public K8SRequest createK8SRequest(byte[] requestByte) {

        String requestString = new String(requestByte);
        K8SRequest request = new K8SRequest();
        request.setRequestByte(requestByte);
        
        Map<String, String[]> requestHeaders = new HashMap<String, String[]>();
        int requestLength = 0;
        int count = 0;

        for (String string : requestString.split("\r\n")) {
            if (string.equals("")) {
                break;
            }

            if (count == 0) {
                String[] requestLine = string.split(" ");

                request.setMethod(requestLine[0]);
                request.setPath(requestLine[1].replace("&watch=true", ""));
                request.setProtocol(requestLine[2]);
                count++;
            } else {
                if (!string.contains("Host")) {
                    if (string.toUpperCase().contains("CONTENT-LENGTH")) {
                        requestLength = Integer.parseInt(string.substring(16));
                    }
                    String key = string.substring(0, string.indexOf(":"));
                    String[] header = {key, string.substring(string.indexOf(":") + 2)};
                    if (string.contains("Accept: ")) {
                        header[1] = "application/json";
                    }
                    requestHeaders.put(key.toUpperCase(), header);
                }
            }
        }

        request.setHeaders(requestHeaders);

        if (!request.getMethod().equals("GET")) {
            byte[] payloadByte = new byte[requestLength];
            int startBodyByte = requestByte.length - requestLength;

            System.arraycopy(requestByte, startBodyByte, payloadByte, 0, requestLength);

            request.setPayload(payloadByte);
        }

        return request;
    }

    /**
     * Send the K8S request object to the MasterProxy or K8S apiserver.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy or K8S apiserver for sent request
     * @throws IOException
     */
    public String sendK8SRequest(K8SRequest request) throws IOException {

        String response = null;

        if (getProxyType() == ProxyType.MasterProxy) {
            response = executeK8SJavaClientRequest(request);
        } else {
            if (request.getMethod().equals("GET")) {
                response = executeK8SGet(request);
            } else if (request.getMethod().equals("POST")) {
                response = executeK8SPost(request);
            } else if (request.getMethod().equals("PUT")) {
                response = executeK8SPut(request);
            } else if (request.getMethod().equals("PATCH")) {
                response = executeK8SPatch(request);
            } else if (request.getMethod().equals("DELETE")) {
                response = executeK8SDelete(request);
            }
        }

        return response;
    }

    /**
     * Execute the request and send it to K8S apiserver.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from K8S apiserver for sent DELETE request
     * @throws IOException
     */
    public String executeK8SJavaClientRequest(K8SRequest request) throws IOException {
        File confFile = new File("admin.conf");

        ApiClient client = Config.fromConfig(confFile.toString());
        Configuration.setDefaultApiClient(client);
        OkHttpClient client11 = client.getHttpClient();

        String url = serverAddress + request.getPath();

        Request javaK8SRequest = null;
        Builder requestBuilder = null;

        if (request.getMethod().equals("GET")) {
            requestBuilder = new Request.Builder().url(url);
            for (String[] value : request.getHeaders().values()) {
                requestBuilder.addHeader(value[0], value[1]);
            }
            javaK8SRequest = requestBuilder.build();
        } else {
            requestBuilder = new Request.Builder().url(url);
            for (String[] value : request.getHeaders().values()) {
                requestBuilder.addHeader(value[0], value[1]);
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse(request.getHeaders().get("CONTENT-TYPE")[1]),
                    request.getPayload());
            requestBuilder.method(request.getMethod(), requestBody);
            javaK8SRequest = requestBuilder.build();
        }

        Response response = client11.newCall(javaK8SRequest).execute();

        String formattedResponse = formatK8SResponse(request, response);

        return formattedResponse;
    }

    /**
     * Format the response from the MasterProxy.
     *
     * @param request      the K8S request object (K8SRequest)
     * @param httpResponse the response from the MasterProxy
     * 
     * @return the formated response
     * @throws IOException
     * @throws ParseException
     */
    public String formatK8SResponse(K8SRequest request, HttpResponse httpResponse) throws ParseException, IOException {

        String formattedResponse = request.getProtocol() + " " + httpResponse.getStatusLine().getStatusCode() + "\r\n";

        for (Header header : httpResponse.getAllHeaders()) {
            formattedResponse = formattedResponse + header.toString() + "\r\n";
        }

        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            String result = EntityUtils.toString(entity);
            formattedResponse = formattedResponse + "\r\n" + result;
        }

        System.out.println(formattedResponse);

        return formattedResponse;
    }

    /**
     * Format the response from K8S apiserver.
     *
     * @param request  the K8S request object (K8SRequest)
     * @param response the response from the K8S apiserver
     * 
     * @return the formated response
     * @throws IOException
     */
    public String formatK8SResponse(K8SRequest request, Response response) throws IOException {
        String responseBody = response.body().string();

        int byteI = responseBody.getBytes().length;
        String bodyLength = "Content-Length: ".concat(Integer.toString(byteI));

        String formattedResponse = request.getProtocol() + " " + response.code() + " " + response.message() + "\r\n"
                + response.headers().toString().replace("Transfer-Encoding: chunked", bodyLength);

        if (!formattedResponse.toUpperCase().contains("CONTENT-LENGTH")) {
            formattedResponse = formattedResponse + bodyLength + "\r\n";
        }

        formattedResponse = formattedResponse + "\r\n" + responseBody;

        response.body().close();

        return formattedResponse;
    }

    /**
     * Create the server address and port string which is either the MasterProxy or
     * K8S apiserver. If it is MasterProxy then serverAddress will be to the K8S
     * apiserver address and port If it is WorkerProxy then serverAddress will be to
     * the MasterProxy address and port
     * 
     * @param proxyType  the type of the proxy (MasterProxy or WorkerProxy)
     * @param serverIP   the IP Address of the server (either the MasterProxy or K8S
     *                   apiserver)
     * @param serverPort the port of the server (either the MasterProxy or K8S
     *                   apiserver)
     * 
     * @return the server address and port as String
     */
    protected static String getServerAddress(ProxyType proxyType, String serverIP, String serverPort) {
        if (proxyType == ProxyType.MasterProxy) {
            return "https://" + serverIP + ":" + serverPort;
        } else {
            return "http://" + serverIP + ":" + serverPort;
        }
    }
}
