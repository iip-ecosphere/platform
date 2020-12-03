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

package de.iip_ecosphere.platform.connectors.model;

import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Refines the {@link TypeTranslator} for the use with machine connectors.
 * 
 * @author Holger Eichelberger, SSE
 *
 * @param <D> the protocol-specific data type for values (see {@link ModelAccess}) 
 */
public interface ModelAccessProvider<D> {

    /**
     * Returns the model access instance to be used during type translations.
     * 
     * @return the model access instance, may be <b>null</b> (see {@link MachineConnector#hasModel()})
     */
    public ModelAccess<D> getModelAccess();

    /**
     * Defines the model access. Handle with care, shall be called (indirectly) by the connector only.
     * 
     * @param modelAccess the model access
     */
    public void setModelAccess(ModelAccess<D> modelAccess);
    
}
