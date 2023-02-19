/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * Defines a basic channeled connector.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractChannelConnector<O, I, CO, CI> extends AbstractConnector<O, I, CO, CI> {

    private String[] outputChannels;
    private ChannelAdapterSelector<O, I, CO, CI> selector;

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    protected AbstractChannelConnector(ChannelProtocolAdapter<O, I, CO, CI>... adapter) {
        this(null, adapter);
    }

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @param adapter the protocol adapter
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
     */
    @SafeVarargs
    protected AbstractChannelConnector(ChannelAdapterSelector<O, I, CO, CI> selector, 
        ChannelProtocolAdapter<O, I, CO, CI>... adapter) {
        super(ensureAdapterSelector(selector), adapter);
        this.selector = (ChannelAdapterSelector<O, I, CO, CI>) super.getSelector();
        outputChannels = new String[adapter.length];
        for (int a = 0; a < adapter.length; a++) {
            outputChannels[a] = adapter[a].getOutputChannel();
        }
    }
    
    /**
     * Ensures that there is at least a default first-adapter selector of the right type.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector for the first adapter)
     * @return {@code selector} or a default selector instance
     */
    private static <O, I, CO, CI> ChannelAdapterSelector<O, I, CO, CI> ensureAdapterSelector(
        ChannelAdapterSelector<O, I, CO, CI> selector) {
        ChannelAdapterSelector<O, I, CO, CI> result = selector;
        if (null == result) {
            result = new ChannelAdapterSelector<O, I, CO, CI>() {

                private Map<String, ChannelProtocolAdapter<O, I, CO, CI>> channelProvider = new HashMap<>();
                private ChannelProtocolAdapter<O, I, CO, CI> fallback;
                
                @Override
                public ChannelProtocolAdapter<O, I, CO, CI> selectSouthOutput(String channel, O data) {
                    ChannelProtocolAdapter<O, I, CO, CI> result = channelProvider.get(channel);
                    if (null == result) {
                        result = fallback;
                    }
                    return result;
                }

                @Override
                public ChannelProtocolAdapter<O, I, CO, CI> selectNorthInput(CI data) {
                    return fallback;
                }

                @Override
                public void init(ChannelAdapterProvider<O, I, CO, CI> provider) {
                    // this is a simple selector, we always return the "first", also for the channels
                    fallback = provider.getAdapter(0);
                    for (int i = 0, size = provider.getAdapterCount(); i < size; i++) {
                        ChannelProtocolAdapter<O, I, CO, CI> tmp = provider.getAdapter(i);
                        String outChannel = tmp.getOutputChannel();
                        if (!channelProvider.containsKey(outChannel)) {
                            channelProvider.put(outChannel, tmp);
                        }
                    }
                }
            };
        }
        return result;
    }
    
    /**
     * Refines the parent's {@link BasicAdapterProvider} to comply with/provider {@link ChannelProtocolAdapter}.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ChannelAdapterProvider extends BasicAdapterProvider 
        implements ChannelAdapterSelector.ChannelAdapterProvider<O, I, CO, CI> {

        @Override
        public ChannelProtocolAdapter<O, I, CO, CI> getAdapter(int index) {
            // we know from constructor what must be in as type
            return (ChannelProtocolAdapter<O, I, CO, CI>) super.getAdapter(index); 
        }

    }

    @Override
    protected void initSelector(AdapterSelector<O, I, CO, CI> selector) {
        selector.init(new ChannelAdapterProvider());
    }
    

    /**
     * Returns the adapter selector.
     * 
     * @return the selector
     */
    protected ChannelAdapterSelector<O, I, CO, CI> getSelector() {
        return selector;
    }
    
    /**
     * Explicitly requests reading data from the source. This is typically done by polling or
     * events, but, in seldom cases, may be needed manually.
     * 
     * @param channel the channel to assign the received data to, may be {@link #DEFAULT_CHANNEL}.
     * @param notifyCallback whether {@link #setReceptionCallback(ReceptionCallback) the reception callback} shall 
     *   be informed about new data
     * @return the data from the machine, <b>null</b> for none, i.e., also no call to 
     *   {@link #setReceptionCallback(ReceptionCallback) the reception callback}
     * @throws IOException in case that reading fails
     */
    public CO request(String channel, boolean notifyCallback) throws IOException {
        return super.request(channel, notifyCallback);
    }
    
    @Override
    public void write(CI data) throws IOException {
        ChannelProtocolAdapter<O, I, CO, CI> adapter = selector.selectNorthInput(data);
        writeImpl(adapter.adaptInput(data), adapter.getInputChannel());
    }

    @Override
    protected final void writeImpl(I data) throws IOException {
    }

    /**
     * Does the actual writing to the underlying machine/platform. Can be left empty if 
     * {@link MachineConnector#hasModel()}.
     * 
     * @param data the data to be send
     * @param channel the channel name to use
     * @throws IOException if sending fails
     */
    protected abstract void writeImpl(I data, String channel) throws IOException;

    /**
     * Returns the output channel names.
     * 
     * @return the output channel names
     */
    protected String[] getOutputChannels() {
        return outputChannels.clone();
    }

}
