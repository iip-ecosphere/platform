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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * Implements a fake sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeSubmodelElementCollection extends FakeElement implements SubmodelElementCollection {

    private Map<String, SubmodelElement> elements = new HashMap<>();
    private Map<String, Builder<?>> deferred;
    
    /**
     * The builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class FakeSubmodelElementCollectionBuilder extends FakeSubmodelElementContainerBuilder 
        implements SubmodelElementCollectionBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeSubmodelElementCollection instance;
        private boolean isNew = true;
        
        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param ordered whether the collection shall be ordered
         * @param allowDuplicates whether duplicates shall be allowed
         */
        protected FakeSubmodelElementCollectionBuilder(FakeSubmodelElementContainerBuilder parent, String idShort, 
            boolean ordered, boolean allowDuplicates) { // fake, forget ordered, allowDuplicates
            this.parent = parent;
            this.instance = createInstance(idShort);
        }

        /**
         * Creates a builder instance for an existing instance.
         * 
         * @param parent the parent builder
         * @param instance the instance
         */
        protected FakeSubmodelElementCollectionBuilder(FakeSubmodelElementContainerBuilder parent, 
            FakeSubmodelElementCollection instance) {
            this.parent = parent;
            this.instance = instance;
            this.isNew = false;
        }
        
        /**
         * Creates the instance.
         * 
         * @param idShort the short id
         * @return the instance
         */
        protected FakeSubmodelElementCollection createInstance(String idShort) {
            return new FakeSubmodelElementCollection(idShort);
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
            boolean allowDuplicates) {
            SubmodelElementCollectionBuilder result = DeferredBuilder.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class, instance.deferred);
            if (null == result) {
                result = new FakeSubmodelElementCollectionBuilder(this, idShort, ordered, allowDuplicates); 
            }
            return result; 
        }

        @Override
        public Reference createReference() {
            return new FakeReference();
        }
        
        @Override
        <T extends SubmodelElement> T registerElement(T elt) {
            instance.elements.put(elt.getIdShort(), elt);
            return elt;
        }

        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return parent;
        }

        @Override
        public AasBuilder getAasBuilder() {
            return parent.getAasBuilder();
        }

        @Override
        public boolean isNew() {
            return isNew;
        }

        @Override
        void defer(String shortId, Builder<?> builder) {
            instance.deferred = DeferredBuilder.defer(shortId, builder, instance.deferred);
        }

        @Override
        void buildMyDeferred() {
            DeferredBuilder.buildDeferred(instance.deferred);            
        }

        @Override
        public void buildDeferred() {
            parent.buildMyDeferred();
        }

        @Override
        public void defer() {
            parent.defer(instance.getIdShort(), this);
        }

        @Override
        public SubmodelElementCollection build() {
            buildMyDeferred();
            parent.register(instance);
            return instance;
        }

        @Override
        public boolean hasElement(String idShort) {
            return instance.elements.containsKey(idShort);
        }

        @Override
        public SubmodelElementCollectionBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }

    }
    
    /**
     * Creates an instance.
     * 
     * @param idShort the id
     */
    protected FakeSubmodelElementCollection(String idShort) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitSubmodelElementCollection(this);
        for (SubmodelElement sm : visitor.sortSubmodelElements(elements.values())) {
            sm.accept(visitor);
        }
        visitor.endSubmodelElementCollection(this);
    }

    /**
     * Returns the elements.
     *
     * @return the elements
     */
    protected Map<String, SubmodelElement> elts() {
        return elements;
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return elements.values();
    }
    
    @Override
    public Iterable<SubmodelElement> elements() {
        return elements.values();
    }
    
    /**
     * Filters {@code #elements()} for the given short id and type.
     * 
     * @param <T> the element type to return
     * @param idShort the short id
     * @param type the element type
     * @return the found element or <b>null</b>
     */
    private <T extends SubmodelElement> T filter(String idShort, Class<T> type) {
        T result = null;
        for (SubmodelElement sm : elements()) {
            if (sm.getIdShort().equals(idShort) && type.isInstance(sm)) {
                result = type.cast(sm);
            }
        }
        return result;
    }
    
    /**
     * Returns a type-filtered stream.
     * 
     * @param <T> the actual type of submodel element
     * @param type the type to filter for
     * @return the stream of filtered elements
     */
    protected <T extends SubmodelElement> Stream<T> stream(Class<T> type) {
        Predicate<SubmodelElement> filter = null == type ? e -> true : e -> type.isInstance(e);
        return elements.values().stream().filter(filter).map(e -> type.cast(e));
    }

    @Override
    public DataElement getDataElement(String idShort) {
        return filter(idShort, DataElement.class);
    }

    @Override
    public Property getProperty(String idShort) {
        return filter(idShort, Property.class);
    }

    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        return filter(idShort, ReferenceElement.class);
    }
    
    @Override
    public RelationshipElement getRelationshipElement(String idShort) {
        return filter(idShort, RelationshipElement.class);
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return elements.get(idShort);
    }
    
    @Override
    public SubmodelElement getElement(String idShort) {
        return elements.get(idShort);
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return filter(idShort, SubmodelElementCollection.class);
    }
    
    @Override
    public Entity getEntity(String idShort) {
        return filter(idShort, Entity.class);
    }

    @Override
    public int getElementsCount() {
        return elements.size();
    }

    @Override
    public Reference createReference() {
        return new FakeReference();
    }

    @Override
    public void deleteElement(String idShort) {
        elements.remove(idShort);
    }

    @Override
    public Operation getOperation(String idShort) {
        return filter(idShort, Operation.class);
    }

}
