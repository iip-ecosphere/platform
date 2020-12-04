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

import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * An abstract basic implementation of the {@link ConnectorOutputTypeTranslator} to store the {@link ModelAccess}.
 * 
 * @param <S> the source type (see {@link ConnectorOutputTypeTranslator})
 * @param <T> the target type (see {@link ConnectorOutputTypeTranslator})
 * @param <D> the protocol-specific data type for values (see {@link ModelAccess})
 *  
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractConnectorOutputTypeTranslator<S, T, D> implements ConnectorOutputTypeTranslator<S, T, D> {

    private ModelAccess<D> modelAccess;
    
    @Override
    public ModelAccess<D> getModelAccess() {
        return modelAccess;
    }

    @Override
    public void setModelAccess(ModelAccess<D> modelAccess) {
        this.modelAccess = modelAccess;
    }

}
