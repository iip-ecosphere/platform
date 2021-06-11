package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Extended the http apache base request to add body to the delete http apache request.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    /**
     * Creates a HttpDeleteWithBody instance.
     * 
     * @param uri the URL as String
     */
    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    /**
     * Creates a HttpDeleteWithBody instance.
     * 
     * @param uri the URL as URI
     */
    public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * Creates a HttpDeleteWithBody instance.
     */
    public HttpDeleteWithBody() {
        super();
    }
    
    /**
     * Returns the method name of the request.
     * 
     * @return the protocol of the request
     */
    public String getMethod() {
        return METHOD_NAME;
    }
}
