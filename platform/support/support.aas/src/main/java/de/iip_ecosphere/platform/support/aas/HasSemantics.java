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

package de.iip_ecosphere.platform.support.aas;

/**
 * Marks an element that has semantics.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface HasSemantics {


    /**
     * Returns the semantic id of the element.
     * 
     * @return the semantic id in textual format, e.g., with identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     * @see #getSemanticId(boolean)
     */
    public default String getSemanticId() {
        return getSemanticId(false);
    }

    /**
     * Returns the semantic id of the element.
     * 
     * @param stripPrefix if the plain semantic id or the prefix shall also be emitted
     * @return the semantic id in textual format, e.g., with/out identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     * @see #getSemanticId()
     */
    public String getSemanticId(boolean stripPrefix);
    
}
