package de.iip_ecosphere.platform.support.iip_aas.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A proxy for {@link ServerAddress} as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerAddressHolder {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private int port; // negative leads to ephemerial
    private String host;
    private Schema schema = Schema.IGNORE;
    private boolean running = false;


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
     * Creates an instance by copying data from a given instance.
     * 
     * @param holder the holder to copy from
     */
    public ServerAddressHolder(ServerAddressHolder holder) {
        port = holder.port;
        host = holder.host;
        schema = holder.schema;
        running = holder.running;
    }
    
    /**
     * Returns the port value.
     * 
     * @return the port (may be negative, indicates ephemerial)
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Defines the {@link #port} value.  [required by data mapper]
     * 
     * @param port the new value of {@link #port} (may be negative, indicates ephemerial)
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
     * Returns whether the port is set to be determined dynamically.
     * 
     * @return {@code true} for ephemeral, {@code false} else
     */
    @JsonIgnore
    public boolean isEphmemeral() {
        return port < 0;
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
     * Returns whether the server is already running and shall not be started.
     * 
     * @return whether we can assume that it is already running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Changes whether the server is already running. [snakeyaml]
     * 
     * @param running whether we can assume that it is already running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    /**
     * Returns a server address instance constructed from the data in this instance.
     * 
     * @return the server address
     */
    @JsonIgnore
    public ServerAddress getServerAddress() {
        if (getPort() < 0) {
            setPort(NetUtils.getEphemeralPort());
        }
        return new ServerAddress(getSchema(), getHost(), getPort());
    }

    /**
     * Reads a {@link ServerAddress} from a JSON string.
     * 
     * @param json the JSON value, usually a String
     * @return the server address or <b>null</b> if reading fails
     * @see #toJson(ServerAddress)
     */
    public static ServerAddress serverAddressFromJson(Object json) {
        ServerAddress result = null;
        if (null != json) {
            try {
                ServerAddressHolder tmp = MAPPER.readValue(json.toString(), ServerAddressHolder.class);
                result = new ServerAddress(tmp.getSchema(), tmp.getHost(), tmp.getPort());
            } catch (JsonProcessingException e) {
                // result = null;
            }
        }
        return result;        
    }
    
    /**
     * Turns a {@link ServerAddress} into JSON.
     * 
     * @param address the address (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     * @see #serverAddressFromJson(Object)
     */
    public static String toJson(ServerAddress address) {
        String result = "";
        if (null != address) {
            try {
                ServerAddressHolder tmp = new ServerAddressHolder(address);
                result = MAPPER.writeValueAsString(tmp);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }

}