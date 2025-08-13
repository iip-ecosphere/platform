package de.iip_ecosphere.platform.support.iip_aas.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.IdentityStoreAuthenticationDescriptor;

/**
 * A proxy for {@link Endpoint} as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EndpointHolder extends TlsServerAddressHolder {

    private String path = "";
    private String idStorePrefix = null;
    private boolean ignorePath = false;
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
     * Creates an instance by copying data from a given instance. Does not copy the {@link #validator}. Takes over
     * whether paths shall be ignored.
     * 
     * @param holder the holder to copy from
     */
    public EndpointHolder(EndpointHolder holder) {
        super(holder);
        path = holder.path;
        ignorePath = holder.ignorePath;
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
        return ignorePath ? "" : validator.validatePath(path, this);
    }
    
    /**
     * Allows ignoring the specified path, then always returning an empty path.
     * 
     * @param ignorePath whether the path shall be ignored
     * @return <b>this</b> for chaining
     */
    public EndpointHolder ignorePath(boolean ignorePath) {
        this.ignorePath = ignorePath;
        return this;
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

    /**
     * Returns the authentication descriptor.
     * 
     * @return the authentication descriptor, <b>null</b> for none
     */
    public AuthenticationDescriptor getAuthentication() {
        AuthenticationDescriptor result = null;
        if (idStorePrefix != null) {
            if (idStorePrefix.length() > 0) {
                result = new IdentityStoreAuthenticationDescriptor();
            } else {
                result = new IdentityStoreAuthenticationDescriptor(idStorePrefix);
            }
        }
        return result;
    }

}
