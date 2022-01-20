package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import okhttp3.Response;

/**
 * An interface to create K8S (Kubernetes) proxy.
 * 
 * @author Ahmad Alamoush, SSE
 */

public interface K8SJavaProxy {

    /**
     * Create server socket for specified port on localhost to receive new requests.
     * If certificatePath or keyPath or algo is null, Then it will use apiserver.key
     * and apiserver.crt and RSA by default.
     * 
     * @param localPort       the port on the localhost
     * @param certificatePath the path of certificate used to create the socket
     * @param keyPath         the path of key used to create the socket
     * @param algo            the algorithm used for the key
     * @param tlsCheck        check to use tls security
     * 
     * @return the created server socket for the specified port on localhost
     */
    public ServerSocket getServerSocket(int localPort, String certificatePath, String keyPath, String algo,
            boolean tlsCheck) throws IOException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, InvalidKeySpecException, KeyManagementException;

    /**
     * Extract the request as array of bytes from the InputStream of the localhost
     * server socket.
     *
     * @param reader the InputStream for the port on the localhost
     * 
     * @return the request as array of bytes
     */
    public byte[] extractK8SRequestByte(InputStream reader) throws IOException;

    /**
     * Create K8S request object from the actual array of bytes for the received
     * request.
     *
     * @param requestByte the request as array of bytes
     * 
     * @return the new created K8S request object (K8SRequest)
     */
    public K8SRequest createK8SRequest(byte[] requestByte);

    /**
     * Send the K8S request object to the MasterProxy or K8S apiserver.
     *
     * @param writer  is the output buffer to send watch request stream
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy or K8S apiserver for sent request
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public byte[] sendK8SRequest(BufferedOutputStream writer, K8SRequest request) throws IOException,
            KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException;

    /**
     * Execute GET request and send it to the MasterProxy.
     *
     * @param writer  is the output buffer to send watch request stream
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent GET request
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public byte[] executeK8SGet(BufferedOutputStream writer, K8SRequest request) throws ClientProtocolException,
            IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException;

    /**
     * Execute POST request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent POST request
     */
    public byte[] executeK8SPost(K8SRequest request) throws ClientProtocolException, IOException;

    /**
     * Execute PUT request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent PUT request
     */
    public byte[] executeK8SPut(K8SRequest request) throws ClientProtocolException, IOException;

    /**
     * Execute PATCH request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent PATCH request
     */
    public byte[] executeK8SPatch(K8SRequest request) throws ClientProtocolException, IOException;

    /**
     * Execute DELETE request and send it to the MasterProxy.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from the MasterProxy for sent DELETE request
     */
    public byte[] executeK8SDelete(K8SRequest request) throws ClientProtocolException, IOException;

    /**
     * Execute the request and send it to K8S apiserver.
     *
     * @param writer  is the output buffer to send watch request stream
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from K8S apiserver for request as string
     */
    public byte[] executeK8SJavaClientRequest(BufferedOutputStream writer, K8SRequest request) throws IOException;

    /**
     * Execute the request and send it to K8S apiserver.
     *
     * @param request the K8S request object (K8SRequest)
     * 
     * @return the response from K8S apiserver for request as okhttp3.response
     */
    public Response executeWatchK8SJavaClientRequest(K8SRequest request) throws IOException;

    /**
     * Format the response from the MasterProxy.
     *
     * @param request      the K8S request object (K8SRequest)
     * @param httpResponse the response from the MasterProxy
     * 
     * @return the formated response
     */
    public byte[] formatK8SResponse(K8SRequest request, HttpResponse httpResponse) throws ParseException, IOException;

    /**
     * Format the response from the MasterProxy.
     *
     * @param writer       is the output buffer to send watch request stream
     * @param request      the K8S request object (K8SRequest)
     * @param httpResponse the response from the MasterProxy
     * 
     * @return the formated response
     */
    public byte[] formatWatchK8SResponse(BufferedOutputStream writer, K8SRequest request, HttpResponse httpResponse)
            throws ParseException, IOException;

    /**
     * Format the response from K8S apiserver.
     *
     * @param request  the K8S request object (K8SRequest)
     * @param response the response from the K8S apiserver
     * 
     * @return the formated response
     */
    public byte[] formatK8SResponse(K8SRequest request, Response response) throws IOException;

    /**
     * Format the response from K8S apiserver.
     *
     * @param request  the K8S request object (K8SRequest)
     * @param response the response from the K8S apiserver
     * @param writer  is the output buffer to send watch request stream
     * 
     * @return the formated response
     */
    public byte[] formatWatchK8SResponse(BufferedOutputStream writer, K8SRequest request, Response response)
            throws IOException;

}
