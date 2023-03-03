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

package de.iip_ecosphere.platform.connectors.types;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Uses two {@link TypeTranslator} instances for the protocol adaptation, optionally passing on the reception 
 * channel to a given {@link ChanneledConnectorOutputTypeTranslator}.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public class TranslatingProtocolAdapter<O, I, CO, CI> extends AbstractProtocolAdapter<O, I, CO, CI> {

    private ConnectorOutputTypeTranslator<O, CO> outputTranslator;
    private ChanneledConnectorOutputTypeTranslator<O, CO> channeledOutputTranslator;
    private ConnectorInputTypeTranslator<CI, I> inputTranslator;
    
    /**
     * Creates a translating protocol adapter.
     * 
     * @param outputTranslator the output translator, may be a {@link ChanneledConnectorOutputTypeTranslator}
     * @param inputTranslator the input translator
     */
    @SuppressWarnings("unchecked")
    public TranslatingProtocolAdapter(ConnectorOutputTypeTranslator<O, CO> outputTranslator, 
        ConnectorInputTypeTranslator<CI, I> inputTranslator) {
        this.outputTranslator = outputTranslator;
        if (outputTranslator instanceof ChanneledConnectorOutputTypeTranslator) {
            channeledOutputTranslator = (ChanneledConnectorOutputTypeTranslator<O, CO>) outputTranslator;
        }
        this.inputTranslator = inputTranslator;
    }
    
    @Override
    public I adaptInput(CI data) throws IOException {
        return inputTranslator.from(data);
    }

    @Override
    public CO adaptOutput(String channel, O data) throws IOException {
        return null != channeledOutputTranslator 
            ? channeledOutputTranslator.to(channel, data) : outputTranslator.to(data);
    }

    @Override
    public void setModelAccess(ModelAccess modelAccess) {
        super.setModelAccess(modelAccess);
        outputTranslator.setModelAccess(modelAccess);
        inputTranslator.setModelAccess(modelAccess);
    }

    @Override
    public void initializeModelAccess() throws IOException {
        outputTranslator.initializeModelAccess();
    }
    
    @Override
    public Class<? extends I> getProtocolInputType() {
        return inputTranslator.getSourceType();
    }
    
    @Override
    public Class<? extends CI> getConnectorInputType() {
        return inputTranslator.getTargetType();
    }
    
    @Override
    public Class<? extends O> getProtocolOutputType() {
        return outputTranslator.getSourceType();
    }
    
    @Override
    public Class<? extends CO> getConnectorOutputType() {
        return outputTranslator.getTargetType();
    }
    
}
