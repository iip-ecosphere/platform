/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.types.common;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.BlobDataElement.BlobDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.FileDataElement.FileDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Range.RangeBuilder;
import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.RelationshipElement.RelationshipElementBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Implements a (basic) entity , which delegates all work to an internally created builder. This class shall be 
 * used, if a template submodel shall allow for extensible addition
 * of further submodel elements other than those defined in a template. If this is not intended,
 * directly use {@link Builder} of {@link Entity}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DelegatingEntityBuilder implements EntityBuilder {

    private EntityBuilder delegate;
    
    /**
     * Creates a delegating Entity builder.
     * 
     * @param delegate the builder to delegate to
     */
    protected DelegatingEntityBuilder(EntityBuilder delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Returns the builder this builder is delegating to.
     * 
     * @return the builder
     */
    protected EntityBuilder getDelegate() {
        return delegate;
    }

    @Override
    public PropertyBuilder createPropertyBuilder(String idShort) {
        return delegate.createPropertyBuilder(idShort);
    }

    @Override
    public MultiLanguagePropertyBuilder createMultiLanguagePropertyBuilder(String idShort) {
        return delegate.createMultiLanguagePropertyBuilder(idShort);
    }

    @Override
    public RelationshipElementBuilder createRelationshipElementBuilder(String idShort, Reference first,
            Reference second) {
        return delegate.createRelationshipElementBuilder(idShort, first, second);
    }

    @Override
    public EntityBuilder createEntityBuilder(String idShort, EntityType type, Reference asset) {
        return delegate.createEntityBuilder(idShort, type, asset);
    }

    @Override
    public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
        return delegate.createReferenceElementBuilder(idShort);
    }

    @Override
    public OperationBuilder createOperationBuilder(String idShort) {
        return delegate.createOperationBuilder(idShort);
    }

    @Override
    public FileDataElementBuilder createFileDataElementBuilder(String idShort, String contents, String mimeType) {
        return delegate.createFileDataElementBuilder(idShort, contents, mimeType);
    }

    @Override
    public RangeBuilder createRangeBuilder(String idShort, Type type, Object min, Object max) {
        return delegate.createRangeBuilder(idShort, type, min, max);
    }

    @Override
    public BlobDataElementBuilder createBlobDataElementBuilder(String idShort, String contents, String mimeType) {
        return delegate.createBlobDataElementBuilder(idShort, contents, mimeType);
    }

    @Override
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
        boolean allowDuplicates) {
        return delegate.createSubmodelElementCollectionBuilder(idShort, ordered, allowDuplicates);
    }

    @Override
    public SubmodelElementContainerBuilder getParentBuilder() {
        return delegate.getParentBuilder();
    }

    @Override
    public AasBuilder getAasBuilder() {
        return delegate.getAasBuilder();
    }

    @Override
    public boolean isNew() {
        return delegate.isNew();
    }

    @Override
    public boolean hasElement(String idShort) {
        return delegate.hasElement(idShort);
    }

    @Override
    public Reference createReference() {
        return delegate.createReference();
    }

    @Override
    public Entity build() {
        return delegate.build();
    }

    @Override
    public EntityBuilder setDescription(LangString... description) {
        return delegate.setDescription(description);
    }

    @Override
    public EntityBuilder setSemanticId(String refValue) {
        return delegate.setSemanticId(refValue);
    }

}