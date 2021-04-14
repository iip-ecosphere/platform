package de.iip_ecosphere.platform.support.iip_aas.config;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A proxy for {@link ServerAddress} with a protocol, as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProtocolAddressHolder extends ServerAddressHolder {

    private String protocol;
    
    /**
     * Creates an instance (deserialization).
     */
    public ProtocolAddressHolder() {
    }
    
    /**
     * Creates an instance.
     * 
     * @param schema the schema
     * @param host the host name
     * @param port the port
     * @param protocol a name denoting the protocol 
     */
    public ProtocolAddressHolder(Schema schema, String host, int port, String protocol) {
        super(schema, host, port);
        this.protocol = protocol;
    }

    /**
     * Creates an instance from a given instance (serialization).
     * 
     * @param address the instance to take data from
     * @param protocol the protocol
     */
    public ProtocolAddressHolder(ServerAddress address, String protocol) {
        super(address.getSchema(), address.getHost(), address.getPort());
        this.protocol = protocol;
    }

    /**
     * Returns the endpoint name/path.
     * 
     * @return the endpoint name/path
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Defines the protocol name. [required by data mapper]
     * 
     * @param protocol the protocol name
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

}
