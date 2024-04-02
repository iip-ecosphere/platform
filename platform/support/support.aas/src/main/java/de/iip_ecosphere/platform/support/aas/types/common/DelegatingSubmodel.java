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

import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * A delegating submodel elements collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DelegatingSubmodel implements Submodel {

    private Submodel delegate;

    /**
     * Creates an instance based on the given delegate.
     * 
     * @param delegate the delegate
     */
    protected DelegatingSubmodel(Submodel delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Returns the delegate.
     * 
     * @return the delegate
     */
    protected Submodel getDelegate() {
        return delegate;
    }
    
    /**
     * Returns all sub-model elements in the element container.
     * 
     * @return all sub-model elements
     */
    public Iterable<SubmodelElement> elements() {
        return submodelElements();
    }

    @Override
    public String getIdShort() {
        return delegate.getIdShort();
    }

    @Override
    public void accept(AasVisitor visitor) {
        delegate.accept(visitor);
    }

    @Override
    public void update() {
        delegate.update();
    }

    @Override
    public String getSemanticId(boolean stripPrefix) {
        return delegate.getSemanticId();
    }

    @Override
    public void setSemanticId(String semanticId) {
        delegate.setSemanticId(semanticId);
    }

    @Override
    public String getIdentification() {
        return delegate.getIdentification();
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return delegate.submodelElements();
    }

    @Override
    public int getSubmodelElementsCount() {
        return delegate.getSubmodelElementsCount();
    }

    @Override
    public Iterable<DataElement> dataElements() {
        return delegate.dataElements();
    }

    @Override
    public int getDataElementsCount() {
        return delegate.getDataElementsCount();
    }

    @Override
    public Iterable<Property> properties() {
        return delegate.properties();
    }

    @Override
    public int getPropertiesCount() {
        return delegate.getPropertiesCount();
    }

    @Override
    public Iterable<Operation> operations() {
        return delegate.operations();
    }

    @Override
    public int getOperationsCount() {
        return delegate.getOperationsCount();
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return delegate.getSubmodelElementCollection(idShort);
    }

    @Override
    public Entity getEntity(String idShort) {
        return delegate.getEntity(idShort);
    }
    
    @Override
    public DataElement getDataElement(String idShort) {
        return delegate.getDataElement(idShort);
    }

    @Override
    public Property getProperty(String idShort) {
        return delegate.getProperty(idShort);
    }

    @Override
    public Operation getOperation(String idShort) {
        return delegate.getOperation(idShort);
    }

    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        return delegate.getReferenceElement(idShort);
    }

    @Override
    public RelationshipElement getRelationshipElement(String idShort) {
        return delegate.getRelationshipElement(idShort);
    }
    
    @Override
    public void deleteElement(String idShort) {
        delegate.deleteElement(idShort);
    }

    @Override
    public void buildDeferred() {
        delegate.buildDeferred();
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return delegate.getSubmodelElement(idShort);
    }

    @Override
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
        boolean allowDuplicates) {
        return delegate.createSubmodelElementCollectionBuilder(idShort, ordered, allowDuplicates);
    }

    @Override
    public Reference createReference() {
        return delegate.createReference();
    }

    @Override
    public boolean create(Consumer<SubmodelElementContainerBuilder> func, boolean propagate, String... path) {
        return delegate.create(func, propagate, path);
    }

    @Override
    public <T extends SubmodelElement> boolean iterate(IteratorFunction<T> func, Class<T> cls, String... path) {
        return delegate.iterate(func, cls, path);
    }

}
