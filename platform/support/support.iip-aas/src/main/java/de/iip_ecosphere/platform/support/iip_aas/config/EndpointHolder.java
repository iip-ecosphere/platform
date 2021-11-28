package de.iip_ecosphere.platform.support.iip_aas.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;

/**
 * A proxy for {@link Endpoint} as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EndpointHolder extends TlsServerAddressHolder {

    private String path = "";
    
    /**
     * Creates an instance (deserialization).
     */
    public EndpointHolder() {
    }
    
    /**
     * Creates an instance.
     * 
     * @param schema the schema
     * @param host the host name
     * @param port the port
     * @param path the path denoting the endpoint
     */
    public EndpointHolder(Schema schema, String host, int port, String path) {
        super(schema, host, port);
        this.path = path;
    }

    /**
     * Creates an instance from a given instance (serialization).
     * 
     * @param endpoint the instance to take data from
     */
    public EndpointHolder(Endpoint endpoint) {
        super(endpoint.getSchema(), endpoint.getHost(), endpoint.getPort());
        this.path = endpoint.getEndpoint();
    }

    /**
     * Returns the endpoint name/path.
     * 
     * @return the endpoint name/path
     */
    public String getPath() {
        return path;
    }

    /**
     * Defines the endpoint name/path. [required by data mapper]
     * 
     * @param path the endpoint name/path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns an endpoint instance constructed from the data in this instance.
     * 
     * @return the endpoint
     */
    @JsonIgnore
    public Endpoint getEndpoint() {
        return new Endpoint(getServerAddress(), path);
    }

}
