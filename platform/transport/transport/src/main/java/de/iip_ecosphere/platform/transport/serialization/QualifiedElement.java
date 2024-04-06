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

package de.iip_ecosphere.platform.transport.serialization;

/**
 * Represents a qualified element, a value with qualifier. May be used for AAS value transport. 
 * Shall be serializable if needed.
 * 
 * @param <T> the element type
 * @author Holger Eichelberger, SSE
 */
public interface QualifiedElement<T> {

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
     * Returns the actual qualifier of the element. A qualifier may be a semantic id.
     * 
     * @return the qualifier, may be <b>null</b> for none
     */
    public String getQualifier();

    /**
     * Changes the actual qualifier of the element. A qualifier may be a semantic id.
     * 
     * @param qualifier the qualifier, may be <b>null</b> for none
     */
    public void setQualifier(String qualifier);
    
}
