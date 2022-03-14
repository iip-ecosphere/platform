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

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;

/**
 * Represents an AAS sub-model.
 * 
 * @author Holger Eichelberger, SSE
*/
public interface Submodel extends Element, HasSemantics, Identifiable, Qualifiable, HasDataSpecification, HasKind, 
    ElementContainer, DeferredParent, ElementsAccess {

    /**
     * Encapsulated logic to build a sub-model.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SubmodelBuilder extends SubmodelElementContainerBuilder, DeferredBuilder<Submodel> {
        
        /**
         * Creates a reference on the sub-model under construction.
         * 
         * @return the reference
         */
        public Reference createReference();

    }

    /**
     * Returns a sub-model element with the given name.
     * 
     * @param idShort the short id of the property
     * @return the sub-model element, <b>null</b> for none
     */
    public SubmodelElement getSubmodelElement(String idShort);

    /**
     * Returns a sub-model elements collection builder either by providing access to an existing collection or through 
     * a builder to add a new sub-model elements collection (ultimately only if {@link Builder#build()} was called).
     * 
     * @param ordered whether the collection shall be ordered or not
     * @param allowDuplicates whether the collection allows duplicates or not
     * @param idShort the short id of the sub-model
     * @return the sub-model collection builder
     */
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
        boolean allowDuplicates);

    /**
     * Returns the reference to the AAS.
     * 
     * @return the reference
     */
    public Reference createReference();

    /**
     * Deletes a sub-model element.
     * 
     * @param elt the element to delete
     */
    public void delete(SubmodelElement elt);

}
