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

package de.iip_ecosphere.platform.support.aas.basyx;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;

import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;

import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * Basic implementation for a container-based model element. Subclasses must call {@link #buildMyDeferred()} in an 
 * appropriate method (usually {@link Builder#build()}) and use 
 * {@link DeferredBuilder#getDeferred(String, Class, java.util.Map)} when potentially creating a builder that could 
 * be deferred.
 * 
 * @param <S> the BaSyx type implementing the sub-model
 * @author Holger Eichelberger, SSE
 */
abstract class BaSyxSubmodelElementContainerBuilder<S extends ISubmodel> implements SubmodelElementContainerBuilder {

    @Override
    public PropertyBuilder createPropertyBuilder(String idShort) {
        return new BaSyxProperty.BaSyxPropertyBuilder(this, idShort);
    }

    @Override
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
        return new BaSyxReferenceElement.BaSyxReferenceElementBuilder(this, idShort);
    }
    
    @Override
    public OperationBuilder createOperationBuilder(String idShort) {
        return new BaSyxOperation.BaSxyOperationBuilder(this, idShort);
    }
    
    /**
     * Creates a reference to the sub-model under creation.
     * 
     * @return the reference
     */
    public Reference createReference() {
        return getInstance().createReference();
    }
    
    /**
     * Returns the underlying instance.
     * 
     * @return the instance
     */
    protected abstract AbstractSubmodel<S> getInstance();
    
    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    BaSyxOperation register(BaSyxOperation operation) {
        getInstance().getSubmodel().addSubmodelElement(operation.getSubmodelElement());
        return getInstance().register(operation);
    }
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    BaSyxProperty register(BaSyxProperty property) {
        getInstance().getSubmodel().addSubmodelElement(property.getSubmodelElement());
        return getInstance().register(property);
    }

    /**
     * Registers a reference element.
     * 
     * @param reference the reference
     * @return {@code reference}
     */
    BaSyxReferenceElement register(BaSyxReferenceElement reference) {
        getInstance().getSubmodel().addSubmodelElement(reference.getSubmodelElement());
        return getInstance().register(reference);
    }

    /**
     * Registers a sub-model element collection.
     * 
     * @param collection the collection
     * @return {@code collection}
     */
    BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        if (null == getInstance().getSubmodelElementCollection(collection.getIdShort())) {
            getInstance().getSubmodel().addSubmodelElement(collection.getSubmodelElement());
            getInstance().register(collection);
        }
        return collection;
    }

    /**
     * Registers a sub-build as deferred.
     * 
     * @param shortId the shortId of the element
     * @param builder the sub-builder to be registered
     * @see #buildMyDeferred()
     */
    void defer(String shortId, Builder<?> builder) {
        getInstance().defer(shortId, builder);
    }

    /**
     * Calls {@link Builder#build()} on all deferred builders.
     * 
     * @see #defer(String, Builder)
     */
    void buildMyDeferred() {
        getInstance().buildDeferred();
    }

}
