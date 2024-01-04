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
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.BlobDataElement.BlobDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.FileDataElement.FileDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.RelationshipElement.RelationshipElementBuilder;

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
    public MultiLanguagePropertyBuilder createMultiLanguagePropertyBuilder(String idShort) {
        return new FakeMultiLanguageProperty.FakeMultiLanguagePropertyBuilder(this, idShort);
    }

    @Override
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
        return new FakeReferenceElement.FakeReferenceElementBuilder(this, idShort);
    }

    @Override
    public EntityBuilder createEntityBuilder(String idShort, EntityType type, Reference asset) {
        return new FakeEntity.FakeEntityBuilder(this, idShort, type, asset);
    }

    @Override
    public OperationBuilder createOperationBuilder(String idShort) {
        return new FakeOperation.FakeOperationBuilder(this, idShort);
    }

    @Override
    public FileDataElementBuilder createFileDataElementBuilder(String idShort, String contents, String mimeType) {
        return new FakeFileDataElement.FakeFileDataElementBuilder(this, idShort, contents, mimeType);
    }

    @Override
    public BlobDataElementBuilder createBlobDataElementBuilder(String idShort, String value, String mimeType) {
        return new FakeBlobDataElement.FakeBlobDataElementBuilder(this, idShort, value, mimeType);
    }

    @Override
    public RelationshipElementBuilder createRelationshipElementBuilder(String idShort, Reference first,
        Reference second) {
        return new FakeRelationshipElement.FakeRelationshipElementBuilder(this, idShort, first, second);
    }
    
    /**
     * Registers an element. Default for all remaining registration functions in this interface.
     * 
     * @param <T> the actual type of the element
     * @param elt the element
     * @return {@code elt}
     */
    abstract <T extends SubmodelElement> T registerElement(T elt);

    /**
     * Registers an element.
     * 
     * @param element the element
     * @return {@code element}
     */
    FakeBlobDataElement register(FakeBlobDataElement element) {
        return registerElement(element);
    }

    /**
     * Registers an element.
     * 
     * @param element the element
     * @return {@code element}
     */
    FakeFileDataElement register(FakeFileDataElement element) {
        return registerElement(element);
    }

    /**
     * Registers a relationship element.
     * 
     * @param relationship the relationship element
     * @return {@code relationship}
     */
    FakeRelationshipElement register(FakeRelationshipElement relationship) {
        return registerElement(relationship);
    }

    /**
     * Registers an entity.
     * 
     * @param entity the entity
     * @return {@code entity}
     */
    FakeEntity register(FakeEntity entity) {
        return registerElement(entity);
    }

    /**
     * Registers a reference element.
     * 
     * @param reference the reference
     * @return {@code reference}
     */
    FakeReferenceElement register(FakeReferenceElement reference) {
        return registerElement(reference);
    }

    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    FakeOperation register(FakeOperation operation) {
        return registerElement(operation);
    }
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    FakeProperty register(FakeProperty property) {
        return registerElement(property);
    }

    /**
     * Registers a multi-language property.
     * 
     * @param property the property
     * @return {@code property}
     */
    FakeMultiLanguageProperty register(FakeMultiLanguageProperty property)  {
        return registerElement(property);
    }

    /**
     * Registers a sub-model element collection.
     * 
     * @param collection the collection
     * @return {@code collection}
     */
    FakeSubmodelElementCollection register(FakeSubmodelElementCollection collection)  {
        return registerElement(collection);
    }

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
