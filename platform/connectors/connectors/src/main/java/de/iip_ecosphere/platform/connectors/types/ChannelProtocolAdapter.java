package de.iip_ecosphere.platform.connectors.types;

/**
 * Adapts a channeled protocol from/to an underlying machine/platform.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ChannelProtocolAdapter<O, I, CO, CI>  extends ProtocolAdapter<O, I, CO, CI> {

    /**
     * Returns the name of the input channel. Further semantics is implied/restrictions are imposed by the 
     * underlying protocol.
     * 
     * @return the name of the input channel
     */
    public String getInputChannel();
    
    /**
     * Returns the name of the output channel. Further semantics is implied/restrictions are imposed by the 
     * underlying protocol.
     * 
     * @return the name of the output channel
     */
    public String getOutputChannel();
    
}
