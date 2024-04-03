/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.types.common;

/**
 * Creates an {@link Element}.
 * 
 * @param <T> the value type
 * @author Holger Eichelberger, SSE
 */
public interface ElementCreator<T> {
    
    /**
     * Creates an element.
     * 
     * @return the element
     */
    public Element<T> createElement();
    
    /**
     * Returns the type.
     * 
     * @return the type
     */
    public Class<T> getType();
    
}
