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

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Defines the interface of a sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SubmodelElementCollection extends SubmodelElement, ElementsAccess {
    
    /**
     * The submodel element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SubmodelElementCollectionBuilder extends SubmodelElementContainerBuilder, 
        DeferredBuilder<SubmodelElementCollection>, RbacReceiver<SubmodelElementCollectionBuilder> {
        
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
        public SubmodelElementCollectionBuilder setSemanticId(String refValue);
        
        @Override
        public default SubmodelElementCollectionBuilder rbac(AuthenticationDescriptor auth, Role[] roles, 
            RbacAction... actions) {
            return RbacRoles.rbac(this, auth, roles, actions);
        }
        
        /**
         * Builds with inherited RBAC rules if available.
         * 
         * @param auth the authentication descriptor, may be <b>null</b> for none
         * @return the result of {@link #build()}
         */
        public default SubmodelElementCollection build(AuthenticationDescriptor auth) {
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
     * Returns a submodel element with the given name.
     * 
     * @param idShort the short id of the property
     * @return the submodel element, <b>null</b> for none
     */
    public SubmodelElement getElement(String idShort);
    
    /**
     * Creates a reference to this collection.
     * 
     * @return the reference
     */
    public Reference createReference();

}
