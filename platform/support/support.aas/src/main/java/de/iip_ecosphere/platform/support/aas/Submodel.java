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

import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;

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
    public interface SubmodelBuilder extends SubmodelElementContainerBuilder, DeferredBuilder<Submodel>, 
        RbacReceiver<SubmodelBuilder> {
        
        /**
         * Creates a reference on the sub-model under construction.
         * 
         * @return the reference
         */
        public Reference createReference();

        /**
         * Sets the semantic ID of the property in terms of a reference.
         * 
         * @param refValue the reference value (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public SubmodelBuilder setSemanticId(String refValue);

        @Override
        public default SubmodelBuilder rbac(AuthenticationDescriptor auth, Role[] roles, RbacAction... actions) {
            return RbacRoles.rbac(this, auth, roles, actions);
        }

        @Override
        public default void justBuild() {
            build();
        }
        
    }

    /**
     * Creates a builder for a contained sub-model element collection (not ordered, no duplicates). Calling this method 
     * again with the same name shall lead to a builder that allows for modifying the sub-model.
     * 
     * @param idShort the short name of the collection
     * @return the builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty; or if modification is not possible
     */
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort);
    
    /**
     * Returns a sub-model element list builder either by providing access to an existing list or through 
     * a builder to add a new sub-model elements list (ultimately only if {@link Builder#build()} was called).
     * 
     * @param idShort the short name of the list
     * @return the sub-model list builder
     */
    public SubmodelElementListBuilder createSubmodelElementListBuilder(String idShort);

    /**
     * Returns a sub-model element container builder either by providing access to an existing collection or list or 
     * through a builder to add a new sub-model elements collection or list (default collection, ultimately only if 
     * {@link Builder#build()} was called).
     * 
     * @param idShort the short name of the list
     * @return the sub-model collection builder
     */
    public SubmodelElementContainerBuilder createSubmodelElementContainerBuilder(String idShort);

    /**
     * Returns the reference to the AAS.
     * 
     * @return the reference
     */
    public Reference createReference();

    /**
     * Interprets {@code path} to end with a contained submodel elements collection and applies
     * {@code func} to it without loading/caching the submodel into its interface representation.
     * 
     * @param func the function to apply for creation
     * @param propagate the change into the interface instance; if applied frequently, may imply a performance issue
     * @param path the path to the submodel
     * @return {@code true} if something has been applied, {@code false} else
     */
    public boolean create(Consumer<SubmodelElementContainerBuilder> func, boolean propagate, String... path);

    @FunctionalInterface
    public interface IteratorFunction<T extends SubmodelElement> {

        /**
         * Applies the function.
         * 
         * @param element the element to apply the function to
         * @return {@code true} for continuing the iteration, {@code false} for stopping
         */
        public boolean apply(T element);
        
    }
    
    /**
     * Interprets {@code path} to end with a submodel element or a submode elements collection and applies
     * {@code func} to the element or to all elements in the collection without loading/caching the submodel 
     * elements into its interface representation.
     * 
     * @param <T> type of elements
     * @param func the function to apply
     * @param cls class of elements to filter during the iteration
     * @param path the path to the submodel element/Collection
     * @return {@code true} if something has been applied, {@code false} else
     */
    public <T extends SubmodelElement> boolean iterate(IteratorFunction<T> func, Class<T> cls, String... path);    

    /**
     * Requests a refresh. No need for implementation, if underlying AAS implementation does this implicitly.
     */
    public default void refreshOnUse() {
    }
    
}
