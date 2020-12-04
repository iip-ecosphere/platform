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

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;

/**
 * Defines a basic channeled connector.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <D> the model data type (see @link {@link ModelAccess})
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractChannelConnector<O, I, CO, CI, D> extends AbstractConnector<O, I, CO, CI, D> {

    private String inputChannel;
    private String outputChannel;

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     */
    protected AbstractChannelConnector(ChannelProtocolAdapter<O, I, CO, CI, D> adapter) {
        super(adapter);
        this.inputChannel = adapter.getInputChannel();
        this.outputChannel = adapter.getOutputChannel();
    }

    /**
     * Returns the input channel name.
     * 
     * @return the input channel name
     */
    protected String getInputChannel() {
        return inputChannel;
    }

    /**
     * Returns the output channel name.
     * 
     * @return the output channel name
     */
    protected String getOutputChannel() {
        return outputChannel;
    }

}
