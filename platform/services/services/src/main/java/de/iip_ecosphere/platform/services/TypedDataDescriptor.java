/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services;

/**
 * Describes a typed data (element).
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TypedDataDescriptor {

    /**
     * The name of the element.
     * 
     * @return the name
     */
    public String getName();

    /**
     * The type of the element.
     * 
     * @return the type, either a standard java class or a dynamic proxy for types declared by the services that are
     *   not available in this (execution/platform) environment
     */
    public Class<?> getType();
    
    /**
     * The description of the element.
     * 
     * @return the description, may be empty
     */
    public String getDescription();

}
