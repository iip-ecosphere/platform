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
    private transient EndpointValidator validator = new BasicEndpointValidator();
    
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
     * Creates an instance by copying data from a given instance. Does not copy the {@link #validator}.
     * 
     * @param holder the holder to copy from
     */
    public EndpointHolder(EndpointHolder holder) {
        super(holder);
        path = holder.path;
    }
    
    /**
     * Defines an endpoint validator which may change some of the getter results.
     * 
     * @param validator the validator
     */
    public void setValidator(EndpointValidator validator) {
        if (null != validator) {
            this.validator = validator;
        }
    }

    /**
     * Returns the endpoint name/path.
     * 
     * @return the endpoint name/path
     */
    public String getPath() {
        return validator.validatePath(path, this);
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
     * Returns the port value.
     * 
     * @return the port (may be negative, indicates ephemerial)
     */
    public int getPort() {
        return validator.validatePort(super.getPort(), this);
    }
    
    /**
     * Returns the host value.
     * 
     * @return the host
     */
    public String getHost() {
        return validator.validateHost(super.getHost(), this);
    }
    
    /**
     * Returns the schema value.
     * 
     * @return the schema
     */
    public Schema getSchema() {
        return validator.validateSchema(super.getSchema(), this);
    }

    /**
     * Returns an endpoint instance constructed from the data in this instance.
     * 
     * @return the endpoint
     */
    @JsonIgnore
    public Endpoint getEndpoint() {
        return new Endpoint(getServerAddress(), getPath());
    }

}
