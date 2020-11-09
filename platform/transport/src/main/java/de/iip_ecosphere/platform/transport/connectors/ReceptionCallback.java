package de.iip_ecosphere.platform.transport.connectors;

/**
 * Callback to notify a using implementation about the reception of data in a
 * {@link TransportConnector}.
 * 
 * @param <T> the type of data
 * @author Holger Eichelberger, SSE
 */
public interface ReceptionCallback<T> {

    /**
     * Notifies about the reception of a {@code} data value.
     * 
     * @param data the data value
     */
    public void received(T data);

    /**
     * Returns the type of the data.
     * 
     * @return the class representing the type of the data
     */
    public Class<T> getType();

}