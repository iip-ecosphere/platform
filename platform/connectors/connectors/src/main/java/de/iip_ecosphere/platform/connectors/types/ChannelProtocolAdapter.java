package de.iip_ecosphere.platform.connectors.types;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Adapts a channeled protocol from/to an underlying machine/platform.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <D> the model data type (see @link {@link ModelAccess})
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ChannelProtocolAdapter<O, I, CO, CI, D>  extends ProtocolAdapter<O, I, CO, CI, D> {

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
