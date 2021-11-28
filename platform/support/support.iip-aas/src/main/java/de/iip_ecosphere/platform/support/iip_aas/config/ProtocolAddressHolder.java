package de.iip_ecosphere.platform.support.iip_aas.config;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A proxy for {@link ServerAddress} with a protocol, as we do not want to have setters there.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProtocolAddressHolder extends TlsServerAddressHolder {

    private String protocol;
    private String netmask = "";
    
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

    /**
     * Returns the netmask/network Java regex.
     * 
     * @return the netmask/network Java regex
     */
    public String getNetmask() {
        return netmask;
    }

    /**
     * Defines the netmask/network Java regex. [required by data mapper]
     * 
     * @param netmask the netmask
     */
    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

}
