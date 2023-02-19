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

import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;

/**
 * Selects a protocol adapter based on the given machine data.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public interface AdapterSelector<O, I, CO, CI> {

    /**
     * The name of the default channel, in particular for an information-model
     * non-multi-channel connector. If there are multiple channels, this may map
     * into the first registered adapter.
     */
    public static final String DEFAULT_CHANNEL = "";

    /**
     * Returns the responsible protocol adapter for southbound output.
     * 
     * @param channel the channel {@code data} was received from, may be {@link Ab
     * @param data the data object
     * @return the protocol adapter (must not be <b>null</b>)
     */
    public ProtocolAdapter<O, I, CO, CI> selectSouthOutput(String channel, O data); 

    /**
     * Returns the responsible protocol adapter for northbound input. So far, the channel is implicit.
     * 
     * @param data the data object
     * @return the protocol adapter (must not be <b>null</b>)
     */
    public ProtocolAdapter<O, I, CO, CI> selectNorthInput(CI data); 
    
    /**
     * Provides access to adapter data.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @author Holger Eichelberger, SSE
     */
    public interface AdapterProvider<O, I, CO, CI> {

        /**
         * Returns the total number of adapters.
         * 
         * @return the total number
         */
        public int getAdapterCount();
        
        /**
         * Returns the specified adapter.
         * 
         * @param index the 0-based index
         * @return the adapter
         * @throws IndexOutOfBoundsException of {@code index} not in [0;{@link #getAdapterCount()}-1]
         */
        public ProtocolAdapter<O, I, CO, CI> getAdapter(int index);

    }
    
    /**
     * Initializes the adapter selector.
     * 
     * @param provider the adapter information provider
     */
    public void init(AdapterProvider<O, I, CO, CI> provider);

}
