package de.iip_ecosphere.platform.connectors;

import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;

/**
 * Selects a protocol adapter based on the given machine data for channel connectors.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ChannelAdapterSelector<O, I, CO, CI> extends AdapterSelector<O, I, CO, CI> {

    /**
     * Returns the responsible protocol adapter for southbound input.
     * 
     * @param data the data object
     * @return the protocol adapter (must not be <b>null</b>)
     */
    public ChannelProtocolAdapter<O, I, CO, CI> selectSouthOutput(O data); 

    /**
     * Returns the responsible protocol adapter for northbound input.
     * 
     * @param data the data object
     * @return the protocol adapter (must not be <b>null</b>)
     */
    public ChannelProtocolAdapter<O, I, CO, CI> selectNorthInput(CI data); 

}
