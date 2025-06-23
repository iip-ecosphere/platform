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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Defines the interface of a sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SubmodelElementList extends SubmodelElement, ElementsAccess {
    
    /**
     * The submodel element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SubmodelElementListBuilder extends SubmodelElementContainerBuilder, 
        DeferredBuilder<SubmodelElementList>, RbacReceiver<SubmodelElementListBuilder> {
        
        /**
         * Creates a reference to the sub-model element collection created by this builder.
         * 
         * @return the reference
         */
        public Reference createReference();
        
        /**
         * Sets the semantic ID of the submodel element collection in terms of a reference.
         * 
         * @param refValue the reference value (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public SubmodelElementListBuilder setSemanticId(String refValue);
        
        @Override
        public default SubmodelElementListBuilder rbac(AuthenticationDescriptor auth, Role[] roles, 
            RbacAction... actions) {
            return RbacRoles.rbac(this, auth, roles, actions);
        }
        
        /**
         * Builds with inherited RBAC rules if available.
         * 
         * @param auth the authentication descriptor, may be <b>null</b> for none
         * @return the result of {@link #build()}
         */
        public default SubmodelElementList build(AuthenticationDescriptor auth) {
            rbac(auth);
            return build();
        }
        
        @Override
        public default void justBuild() {
            build();
        }
        
    }

    /**
     * Returns all elements.
     * 
     * @return all elements
     */
    public Iterable<SubmodelElement> elements();
    
    /**
     * Returns the number of elements.
     * 
     * @return the number of elements
     */
    public int getElementsCount();

    /**
     * Returns the first submodel element with the given name.
     * 
     * @param idShort the short id of the property
     * @return the submodel element, <b>null</b> for none
     */
    public SubmodelElement getElement(String idShort);
    
    /**
     * Returns a submodel element with the given index.
     * 
     * @param index the 0-based index of the property, in [{@code 0};{@link #getElementsCount()}[
     * @return the submodel element, <b>null</b> for none
     * @throws IndexOutOfBoundsException if index is not within the given range
     */
    public SubmodelElement getElement(int index);

    /**
     * Deletes the submodel element with the given index.
     * 
     * @param index the 0-based index of the property, in [{@code 0};{@link #getElementsCount()}[
     * @throws IndexOutOfBoundsException if index is not within the given range
     */
    public void deleteElement(int index);
    
    /**
     * Creates a reference to this collection.
     * 
     * @return the reference
     */
    public Reference createReference();

}
