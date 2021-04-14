package de.iip_ecosphere.platform.support.iip_aas.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A proxy for {@link ServerAddress} as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerAddressHolder {
    
    private int port;
    private String host;
    private Schema schema;

    /**
     * Creates an instance (deserialization).
     */
    public ServerAddressHolder() {
    }
    
    /**
     * Creates an instance.
     * 
     * @param schema the schema
     * @param host the host name
     * @param port the port
     */
    public ServerAddressHolder(Schema schema, String host, int port) {
        this.schema = schema;
        this.host = host;
        this.port = port;
    }

    /**
     * Creates an instance from a given instance (serialization).
     * 
     * @param addr the instance to take data from
     */
    public ServerAddressHolder(ServerAddress addr) {
        port = addr.getPort();
        host = addr.getHost();
        schema = addr.getSchema();
    }
    
    /**
     * Returns the port value.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Defines the {@link #port} value.  [required by data mapper]
     * 
     * @param port the new value of {@link #port}
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Returns the host value.
     * 
     * @return the host
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Defines the {@link #host} value.  [required by data mapper]
     * 
     * @param host the new value of {@link #host}
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Returns the schema value.
     * 
     * @return the schema
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Defines the {@link #schema} value.  [required by data mapper]
     * 
     * @param schema the new value of {@link #schema}
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }
    
    /**
     * Returns a server address instance constructed from the data in this instance.
     * 
     * @return the server address
     */
    @JsonIgnore
    public ServerAddress getServerAddress() {
        return new ServerAddress(schema, host, port);
    }
    
}