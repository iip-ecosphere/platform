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
 * Uses two {@link TypeTranslator} instances for the protocol adaptation.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <D> the model data type (see @link {@link ModelAccess})
 * 
 * @author Holger Eichelberger, SSE
 */
public class TranslatingProtocolAdapter<O, I, CO, CI, D> extends AbstractProtocolAdapter<O, I, CO, CI, D> {

    private ConnectorOutputTypeTranslator<O, CO, D> outputTranslator;
    private ConnectorInputTypeTranslator<CI, I, D> inputTranslator;
    
    /**
     * Creates a translating protocol adapter.
     * 
     * @param outputTranslator the output translator
     * @param inputTranslator the input translator
     */
    public TranslatingProtocolAdapter(ConnectorOutputTypeTranslator<O, CO, D> outputTranslator, 
            ConnectorInputTypeTranslator<CI, I, D> inputTranslator) {
        this.outputTranslator = outputTranslator;
        this.inputTranslator = inputTranslator;
    }
    
    @Override
    public I adaptInput(CI data) throws IOException {
        return inputTranslator.from(data);
    }

    @Override
    public CO adaptOutput(O data) throws IOException {
        return outputTranslator.to(data);
    }

    @Override
    public void setModelAccess(ModelAccess<D> modelAccess) {
        super.setModelAccess(modelAccess);
        outputTranslator.setModelAccess(modelAccess);
        inputTranslator.setModelAccess(modelAccess);
    }

    @Override
    public void initializeModelAccess() throws IOException {
        outputTranslator.initializeModelAccess();
    }
    
}
