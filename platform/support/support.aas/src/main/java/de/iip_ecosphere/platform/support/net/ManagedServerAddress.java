package de.iip_ecosphere.platform.support.net;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A specialized managed {@link ServerAddress} containing information whether the address/port is already
 * known to the management approach.
 */
public class ManagedServerAddress extends ServerAddress {

    private boolean isNew;

    /**
     * Creates a new managed server address instance.
     * 
     * @param schema the schema
     * @param host the hostname (turned to {@link #LOCALHOST} if <b>null</b> or empty)
     * @param port the port number
     * @param isNew {@code true} for is new, {@code false} for already known
     */
    public ManagedServerAddress(Schema schema, String host, int port, boolean isNew) {
        super(schema, host, port);
        this.isNew = isNew;
    }

    /**
     * Creates a new managed server address instance.
     * 
     * @param address the address to take port, host and schema from
     * @param isNew {@code true} for is new, {@code false} for already known
     */
    public ManagedServerAddress(ServerAddress address, boolean isNew) {
        this(address.getSchema(), address.getHost(), address.getPort(), isNew);
    }
    
    /**
     * Returns whether this server address is new or already known.
     * 
     * @return {@code true} for is new, {@code false} for already known
     */
    public boolean isNew() {
        return isNew;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof ManagedServerAddress) {
            ManagedServerAddress o = (ManagedServerAddress) other;
            result = super.equals(other) && isNew == o.isNew; 
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ Boolean.hashCode(isNew);
    }

}
