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

import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;

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
        super(ensureAdapterSelector(selector, adapter), adapter);
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
     * @param adapter the protocol adapter
     * @return {@code selector} or a default selector instance
     */
    private static <O, I, CO, CI> ChannelAdapterSelector<O, I, CO, CI> ensureAdapterSelector(
        ChannelAdapterSelector<O, I, CO, CI> selector, ChannelProtocolAdapter<O, I, CO, CI>[] adapter) {
        ChannelAdapterSelector<O, I, CO, CI> result = selector;
        if (null == result) {
            result = new ChannelAdapterSelector<O, I, CO, CI>() {

                @Override
                public ChannelProtocolAdapter<O, I, CO, CI> selectSouthOutput(O data) {
                    return adapter[0];
                }

                @Override
                public ChannelProtocolAdapter<O, I, CO, CI> selectNorthInput(CI data) {
                    return adapter[0];
                }
            };
        }
        return result;
    }

    /**
     * Returns the adapter selector.
     * 
     * @return the selector
     */
    protected ChannelAdapterSelector<O, I, CO, CI> getSelector() {
        return selector;
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
