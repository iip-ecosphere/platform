package de.iip_ecosphere.platform.connectors;

import de.iip_ecosphere.platform.connectors.AdapterSelector.AdapterProvider;
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

    @Override
    public ChannelProtocolAdapter<O, I, CO, CI> selectSouthOutput(String channel, O data); 

    @Override
    public ChannelProtocolAdapter<O, I, CO, CI> selectNorthInput(CI data); 

    /**
     * Refines {@link AdapterProvider}.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @author Holger Eichelberger, SSE
     */
    public interface ChannelAdapterProvider<O, I, CO, CI> extends AdapterProvider<O, I, CO, CI> {

        @Override
        public ChannelProtocolAdapter<O, I, CO, CI> getAdapter(int index);

    }

    /**
     * Initializes the adapter selector.
     * 
     * @param provider the adapter information provider
     */
    public void init(ChannelAdapterProvider<O, I, CO, CI> provider);
    
    /**
     * Initializes the adapter selector.
     * 
     * @param provider the adapter information provider 
     * @throws IllegalArgumentException if {@code provider} not of type {@link ChannelAdapterProvider}
     */
    public default void init(AdapterProvider<O, I, CO, CI> provider) {
        if (!(provider instanceof ChannelAdapterProvider)) {
            throw new IllegalArgumentException("provider must be of type " + ChannelAdapterProvider.class.getName());
        }
        init((ChannelAdapterProvider<O, I, CO, CI>) provider);
    }

}
