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
import de.iip_ecosphere.platform.transport.serialization.InputTypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * Adapts a basic input translator/serializer for reuse.
 * 
 * @param <S> the source type (see {@link InputTypeTranslator})
 *
 * @author Holger Eichelberger, SSE
 */
public class ConnectorInputTypeAdapter<S> implements ConnectorInputTypeTranslator<S, byte[]> {

    private Serializer<S> serializer;
    private ModelAccess modelAccess;

    /**
     * Creates an instance.
     * 
     * @param serializer the serializer to adapt
     */
    public ConnectorInputTypeAdapter(Serializer<S> serializer) {
        this.serializer = serializer;
    }

    @Override
    public byte[] from(S data) throws IOException {
        return serializer.to(data);
    }

    @Override
    public ModelAccess getModelAccess() {
        return modelAccess;
    }

    @Override
    public void setModelAccess(ModelAccess modelAccess) {
        this.modelAccess = modelAccess;
    }
    
    @Override
    public Class<? extends byte[]> getSourceType() {
        return byte[].class;
    }

    @Override
    public Class<? extends S> getTargetType() {
        return serializer.getType();
    };
    
}
