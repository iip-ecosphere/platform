package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.util.Map;

/**
 * Details of the K8S (Kubernetes) request.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class K8SRequest {

    private String method;
    private String path;
    private String protocol;

    private byte[] payload;
    private Map<String, String[]> headers;

    /**
     * Creates a K8S Request instance.
     * 
     */
    public K8SRequest() {
    }

    /**
     * Returns the method of the request.
     * 
     * @return the method of the request
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Set the method of the request.
     *
     * @param method the method of the request.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the path of the request.
     * 
     * @return the path of the request
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path of the request.
     *
     * @param path the path of the request.
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Returns the protocol of the request.
     * 
     * @return the protocol of the request
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Set the protocol of the request.
     *
     * @param protocol the protocol of the request.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the payload of the request.
     * 
     * @return the payload of the request
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Set the payload of the request.
     *
     * @param payload the payload of the request.
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Returns the headers of the request.
     * 
     * @return the headers of the request
     */
    public Map<String, String[]> getHeaders() {
        return headers;
    }

    /**
     * Set the headers of the request.
     *
     * @param headers the headers of the request.
     */
    public void setHeaders(Map<String, String[]> headers) {
        this.headers = headers;
    }

}
