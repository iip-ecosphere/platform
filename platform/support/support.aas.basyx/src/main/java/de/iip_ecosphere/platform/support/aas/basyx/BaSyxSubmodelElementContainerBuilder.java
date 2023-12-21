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
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.FileDataElement.FileDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement.RelationshipElementBuilder;
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
public abstract class BaSyxSubmodelElementContainerBuilder<S extends ISubmodel> 
    implements SubmodelElementContainerBuilder {

    @Override
    public PropertyBuilder createPropertyBuilder(String idShort) {
        return new BaSyxProperty.BaSyxPropertyBuilder(this, idShort);
    }

    @Override
    public MultiLanguagePropertyBuilder createMultiLanguagePropertyBuilder(String idShort) {
        return new BaSyxMultiLanguageProperty.BaSyxMultiLanguagePropertyBuilder(this, idShort);
    }
    
    @Override
    public RelationshipElementBuilder createRelationshipElementBuilder(String idShort, 
        Reference first, Reference second) {
        return new BaSyxRelationshipElement.BaSyxRelationshipElementBuilder(this, idShort, first, second);
    }

    @Override
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
        return new BaSyxReferenceElement.BaSyxReferenceElementBuilder(this, idShort);
    }
    
    @Override
    public EntityBuilder createEntityBuilder(String idShort, EntityType type, Reference asset) {
        return new BaSyxEntity.BaSyxEntityBuilder(this, idShort, type, asset);
    }
    
    @Override
    public OperationBuilder createOperationBuilder(String idShort) {
        return new BaSyxOperation.BaSxyOperationBuilder(this, idShort);
    }
    
    @Override
    public FileDataElementBuilder createFileDataElementBuilder(String idShort, String contents, String mimeType) {
        return new BaSyxFile.BaSyxFileDataElementBuilder(this, idShort, contents, mimeType);
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
     * Registers a relationship element.
     * 
     * @param relationship the relationship element
     * @return {@code relationship}
     */
    BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
        getInstance().getSubmodel().addSubmodelElement(relationship.getSubmodelElement());
        return getInstance().register(relationship);
    }

    /**
     * Registers an element. Default for all remaining registration functions.
     * 
     * @param <T> the actual type of the element
     * @param elt the element
     * @return {@code elt}
     */
    <T extends SubmodelElement> T registerElement(T elt) {
        if (elt instanceof BaSyxSubmodelElement) {
            BaSyxSubmodelElement bse = (BaSyxSubmodelElement) elt;
            getInstance().getSubmodel().addSubmodelElement(bse.getSubmodelElement());
        }
        return getInstance().registerElement(elt); // fallback stays on fallback
    }
    
    // TODO cleanup

    /**
     * Registers a entity element.
     * 
     * @param entity the entity
     * @return {@code reference}
     */
    BaSyxEntity register(BaSyxEntity entity) {
        getInstance().getSubmodel().addSubmodelElement(entity.getSubmodelElement());
        return getInstance().register(entity);
    }

    /**
     * Registers a file data element.
     * 
     * @param file the file data element
     * @return {@code file}
     */
    BaSyxFile register(BaSyxFile file) {
        getInstance().getSubmodel().addSubmodelElement(file.getSubmodelElement());
        return getInstance().register(file);
    }
    
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
     * Registers a multi-language property.
     * 
     * @param property the property
     * @return {@code property}
     */
    BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
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
     * @param propagate enable propagation into the interface instance (usually {@code true}) or take a (performance)
     * shortcut and only update the BaSyx submodel ({@code false})
     * @return {@code collection}
     */
    protected BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection, boolean propagate) {
        if (null == getInstance().getSubmodelElementCollection(collection.getIdShort())) {
            getInstance().getSubmodel().addSubmodelElement(collection.getSubmodelElement());
            if (propagate) {
                getInstance().register(collection);
            }
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
