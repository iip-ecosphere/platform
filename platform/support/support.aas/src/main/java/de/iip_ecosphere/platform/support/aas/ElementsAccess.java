/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
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
 * Common (idShort-based) access operations for model elements.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ElementsAccess {

    /**
     * Returns the short id of the element.
     * 
     * @return the short id
     */
    public String getIdShort();
    
    /**
     * Returns a sub-model element collection with the given name.
     * 
     * @param idShort the short id of the property
     * @return the sub-model collection element, <b>null</b> for none
     */
    public SubmodelElementCollection getSubmodelElementCollection(String idShort);
    
    /**
     * Returns a data element with the given name.
     * 
     * @param idShort the short id of the data element
     * @return the data element, <b>null</b> for none
     */
    public DataElement getDataElement(String idShort);

    /**
     * Returns a property with the given name.
     * 
     * @param idShort the short id of the property
     * @return the property, <b>null</b> for none
     */
    public Property getProperty(String idShort);

    /**
     * Returns an operation with the given name.
     * 
     * @param idShort the short id of the operation
     * @return the operation, <b>null</b> for none
     */
    public Operation getOperation(String idShort);

    /**
     * Returns a reference element with the given name.
     * 
     * @param idShort the short id of the reference element
     * @return the property, <b>null</b> for none
     */
    public ReferenceElement getReferenceElement(String idShort);

}
