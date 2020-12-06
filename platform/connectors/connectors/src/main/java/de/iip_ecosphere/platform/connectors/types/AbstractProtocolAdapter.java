/********************************************************************************
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

import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Basic protocol adapter implementation.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractProtocolAdapter<O, I, CO, CI> implements ProtocolAdapter<O, I, CO, CI> {
    
    private ModelAccess modelAccess;
    
    /**
     * Returns the instance abstracting the access to the underlying model.
     * 
     * @return the instance, may be <b>null</b> if {@link MachineConnector#hasModel()} is {@code false}
     */
    public final ModelAccess getModelAccess() {
        return modelAccess;
    }

    /**
     * Defines the model access. Handle with care, shall be called by connector only.
     * 
     * @param modelAccess the model access
     */
    public void setModelAccess(ModelAccess modelAccess) {
        this.modelAccess = modelAccess;
    }
    
}
