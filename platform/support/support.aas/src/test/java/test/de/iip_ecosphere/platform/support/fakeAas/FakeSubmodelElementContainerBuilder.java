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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;

/**
 * Basic implementation for a container-based model element.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class FakeSubmodelElementContainerBuilder implements SubmodelElementContainerBuilder {
    
    @Override
    public PropertyBuilder createPropertyBuilder(String idShort) {
        return new FakeProperty.FakePropertyBuilder(this, idShort);
    }

    @Override
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
        return new FakeReferenceElement.FakeReferenceElementBuilder(this, idShort);
    }
    
    @Override
    public OperationBuilder createOperationBuilder(String idShort) {
        return new FakeOperation.FakeOperationBuilder(this, idShort);
    }

    /**
     * Registers an element.
     * 
     * @param element the element
     * @return {@code element}
     */
    abstract FakeFileDataElement register(FakeFileDataElement element);
    
    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    abstract FakeOperation register(FakeOperation operation);
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    abstract FakeProperty register(FakeProperty property);

    /**
     * Registers a reference element.
     * 
     * @param reference the reference
     * @return {@code reference}
     */
    abstract FakeReferenceElement register(FakeReferenceElement reference);

    /**
     * Registers a sub-model element collection.
     * 
     * @param collection the collection
     * @return {@code collection}
     */
    abstract FakeSubmodelElementCollection register(FakeSubmodelElementCollection collection);

    /**
     * Registers a sub-build as deferred.
     * 
     * @param shortId the shortId of the element
     * @param builder the sub-builder to be registered
     * @see #buildMyDeferred()
     */
    abstract void defer(String shortId, Builder<?> builder);

    /**
     * Calls {@link Builder#build()} on all deferred builders.
     * 
     * @see #defer(String, Builder)
     */
    abstract void buildMyDeferred();

}
