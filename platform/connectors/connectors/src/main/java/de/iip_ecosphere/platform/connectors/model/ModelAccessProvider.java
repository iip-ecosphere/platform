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

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Refines the {@link TypeTranslator} for the use with machine connectors.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ModelAccessProvider {

    /**
     * Returns the model access instance to be used during type translations.
     * 
     * @return the model access instance, may be <b>null</b> (see {@link MachineConnector#hasModel()})
     */
    public ModelAccess getModelAccess();

    /**
     * Defines the model access. Handle with care, shall be called (indirectly) by the connector only.
     * 
     * @param modelAccess the model access
     */
    public void setModelAccess(ModelAccess modelAccess);
    
    
    /**
     * A simple (optional) function that may throw an {@link IOException}. {@link IndexOutOfBoundsException}
     * is also considered as serializer parsers may throw that also.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface IOVoidFunction {
        
        /**
         * Executes the function.
         * 
         * @param modelAccess the model access
         * @throws IOException may be thrown but also caught in {@link #optional(IOVoidFunction)}
         * @throws IndexOutOfBoundsException may be thrown but also caught in {@link #optional(IOVoidFunction)}
         */
        public void execute(ModelAccess modelAccess) throws IOException, IndexOutOfBoundsException;

    }

    /**
     * Executes {@code func} but consumes {@link IOException} as execution is considered optional.
     * 
     * @param modelAccess the model access to be passed into {@code func}
     * @param func the function to execute
     * @return {@code true} for success without exception, {@code false} for failed with caught exception
     */
    public static boolean optional(ModelAccess modelAccess, IOVoidFunction func) {
        boolean success = true;
        try {
            func.execute(modelAccess);
        } catch (IOException | IndexOutOfBoundsException e) {
            LoggerFactory.getLogger(IOVoidFunction.class).debug(
                "Function call failed, but considered optional. " + e.getMessage());
            success = false;
        }
        return success;
    }
    
}
