package de.iip_ecosphere.platform.transport.connectors;

/**
 * An abstract reception callback.
 * 
 * @param <T> the type of the data for the callback
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractReceptionCallback<T> implements ReceptionCallback<T> {

    private Class<T> type;

    /**
     * Creates the callback instance.
     * 
     * @param type the type of the data
     */
    protected AbstractReceptionCallback(Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
