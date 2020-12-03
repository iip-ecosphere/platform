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

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Adapts a protocol from/to an underlying machine/platform.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <D> the model data type (see @link {@link ModelAccess})
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ProtocolAdapter <O, I, CO, CI, D> {
    
    /**
     * Adapts the input from the IIP-Ecosphere platform to the underlying machine/platform.
     * 
     * @param data the data to be adapted
     * @return the adapted data
     * @throws IOException in case that the adaptation fails
     */
    public I adaptInput(CI data) throws IOException;

    /**
     * Adapts the output from the underlying machine/platform to the IIP-Ecosphere platform.
     * 
     * @param data the data to be adapted
     * @return the adapted data
     * @throws IOException in case that the adaptation fails
     */
    public CO adaptOutput(O data) throws IOException;
    
    /**
     * Returns the instance abstracting the access to the underlying model.
     * 
     * @return the instance, may be <b>null</b> if {@link MachineConnector#hasModel()} is {@code false}
     */
    public ModelAccess<D> getModelAccess();

    /**
     * Defines the model access. Handle with care, shall be called by connector only.
     * 
     * @param modelAccess the model access
     */
    public void setModelAccess(ModelAccess<D> modelAccess);
    
    /**
     * Called to initialize the model access, e.g., to setup notifications. Called only, when the connector is 
     * connected.
     * 
     * @throws IOException in case the initialization fails, e.g., monitors cannot be set up
     */
    public void initializeModelAccess() throws IOException;
    
}
