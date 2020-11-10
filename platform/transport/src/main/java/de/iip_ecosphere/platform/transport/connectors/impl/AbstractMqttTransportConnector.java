package de.iip_ecosphere.platform.transport.connectors.impl;

/**
 * An abstract MQTT transport connector.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractMqttTransportConnector extends AbstractTransportConnector {

    /**
     * Composes a hierarchical stream name (in the syntax/semantics of the
     * connector).
     * 
     * @param parent the parent name (may be {@link #EMPTY_PARENT} for top-level streams)
     * @param name   the name of the stream
     * @return the composed name
     * @throws IllegalArgumentException in case that the stream name is (structurally) illegal
     */
    public static String composeNames(String parent, String name) {
        return parent != null && parent.length() > 0 ? parent + "/" + name : name;
    }

    @Override
    public String composeStreamName(String parent, String name) {
        return composeNames(parent, name);
    }

}
