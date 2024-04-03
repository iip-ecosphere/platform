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
 * Represents an AAS element of type &lt;T&gt; to be used within common/connector operations. 
 * Shall be serializable if needed.
 * 
 * @param <T> the element type
 * @author Holger Eichelberger, SSE
 */
public interface Element<T> {

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public T getValue();
    
    /**
     * Sets the value.
     * 
     * @param value the value
     */
    public void setValue(T value);
    
    /**
     * Returns the actual semantic id of the element.
     * 
     * @return the semantic id in notation of the AAS abstraction, may be <b>null</b> for none
     */
    public String getSemanticId();

    /**
     * Changes the actual semantic id of the element.
     * 
     * @param semanticId the semantic id in notation of the AAS abstraction, may be <b>null</b> for none
     */
    public void setSemanticId(String semanticId);
    
}
