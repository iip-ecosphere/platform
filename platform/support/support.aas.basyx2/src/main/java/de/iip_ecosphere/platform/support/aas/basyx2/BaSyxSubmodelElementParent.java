/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.util.List;

import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxSubmodelElement.PathFunction;

/**
 * Represents the parent element of a submodel element.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface BaSyxSubmodelElementParent {

    /**
     * Returns the short id of the element.
     * 
     * @return the short id
     */
    public String getIdShort();
        
    /**
     * Returns the parent element.
     * 
     * @return the parent element
     */
    public BaSyxSubmodelElementParent getParent();
    
    /**
     * Returns the path element to be used in {@link #processOnPath(List, boolean, PathFunction)}.
     * 
     * @return the path element
     */
    public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement getPathElement();

    /**
     * Iterates up the path up to the submodel and calls {@code function} on the aggregated path.
     * 
     * @param path the path accumulated so far
     * @param skipIfNoRepo skip executing function if there is no repository to pass into
     * @param function the function to call at the end of the path
     * @see #getPathElement()
     */
    public default org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement processOnPath(
        List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> path, 
        boolean skipIfNoRepo, PathFunction function) {
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement result = null;
        if (null != getParent()) {
            getParent().processOnPath(BaSyxSubmodelElement.composePath(path, getPathElement()), 
                skipIfNoRepo, function);
        }
        return result;
    }

}
