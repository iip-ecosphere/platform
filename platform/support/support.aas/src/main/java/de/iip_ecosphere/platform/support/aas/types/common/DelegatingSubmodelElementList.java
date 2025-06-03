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

package de.iip_ecosphere.platform.support.aas.types.common;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList;

/**
 * A delegating submodel elements list.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DelegatingSubmodelElementList implements SubmodelElementList {
    
    private SubmodelElementList delegate;

    /**
     * Creates an instance based on the given delegate.
     * 
     * @param delegate the delegate
     */
    protected DelegatingSubmodelElementList(SubmodelElementList delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Returns the delegate.
     * 
     * @return the delegate
     */
    protected SubmodelElementList getDelegate() {
        return delegate;
    }
    
    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return delegate.submodelElements();
    }    

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return delegate.getSubmodelElement(idShort);
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
        return delegate.getSemanticId(stripPrefix);
    }

    @Override
    public void setSemanticId(String semanticId) {
        delegate.setSemanticId(semanticId);
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return delegate.getSubmodelElementCollection(idShort);
    }

    @Override
    public SubmodelElementList getSubmodelElementList(String idShort) {
        return delegate.getSubmodelElementList(idShort);
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
    public Iterable<SubmodelElement> elements() {
        return delegate.elements();
    }

    @Override
    public int getElementsCount() {
        return delegate.getElementsCount();
    }

    @Override
    public SubmodelElement getElement(String idShort) {
        return delegate.getElement(idShort);
    }

    @Override
    public Reference createReference() {
        return delegate.createReference();
    }

    @Override
    public SubmodelElement getElement(int index) {
        return delegate.getElement(index);
    }

    @Override
    public void deleteElement(int index) {
        delegate.deleteElement(index);
    }

}
