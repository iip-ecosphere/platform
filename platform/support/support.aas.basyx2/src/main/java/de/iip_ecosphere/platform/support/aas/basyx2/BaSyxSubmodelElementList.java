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

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;
import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxElementTranslator.SubmodelElementsRegistrar;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;

/**
 * Wrapper for the BaSyx sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodelElementList extends BaSyxSubmodelElement implements SubmodelElementList, 
    SubmodelElementsRegistrar, BaSyxSubmodelElementParent {
    
    private org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList list;
    private List<SubmodelElement> elementsList;
    private Map<String, Builder<?>> deferred;
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxSubmodelElementListBuilder extends BaSyxSubmodelElementContainerBuilder<
        org.eclipse.digitaltwin.aas4j.v3.model.Submodel> implements SubmodelElementListBuilder {
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxSubmodelElementList instance;
        private org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList list;
        private boolean isNew = true;
        private boolean propagate = true;
        
        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the sub-model element
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        protected BaSyxSubmodelElementListBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            String idShort) {
            this(parentBuilder, idShort, () -> new BaSyxSubmodelElementList(), () -> {
                org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList list 
                    = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList(); 
                list.setIdShort(Tools.checkId(idShort));
                return list;
            });
        }

        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the sub-model element
         * @param wCreator creates a wrapper instance, subclass of the containing class
         * @param bCreator creates a BaSyx instance
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        protected BaSyxSubmodelElementListBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            String idShort, Supplier<BaSyxSubmodelElementList> wCreator, 
            Supplier<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList> bCreator) {
            this.parentBuilder = parentBuilder;
            this.instance = wCreator.get();
            this.list = bCreator.get();
            this.instance.createElementsStructure();
        }

        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        protected BaSyxSubmodelElementListBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            BaSyxSubmodelElementList instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.isNew = false;
            if (instance.list instanceof org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) {
                this.list = (org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) 
                    instance.list;
            } else {
                throw new IllegalArgumentException("Cannot create a " + getClass().getSimpleName() + " on a " 
                    + instance.list.getClass().getSimpleName());
            }
            this.instance.initialize();
        }
        
        /**
         * Returns the BaSyx collection created by this instance.
         * 
         * @return the collection
         */
        protected org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList getCollection() {
            return list;
        }
        
        /**
         * Returns the collection instance being created.
         * 
         * @return the collection instance
         */
        protected BaSyxSubmodelElementList getCollectionInstance() {
            return instance;
        }
        
        @Override
        public SubmodelElementListBuilder setSemanticId(String refValue) {
            return Tools.setSemanticId(this, refValue, list);                
        }

        @Override
        public PropertyBuilder createPropertyBuilder(String idShort) {
            return BaSyxProperty.BaSyxPropertyBuilder.create(this, idShort, instance.getProperty(idShort)); 
        }

        @Override
        public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
            return new BaSyxReferenceElement.BaSyxReferenceElementBuilder(this, idShort);
        }
        
        @Override
        public OperationBuilder createOperationBuilder(String idShort) {
            return new BaSyxOperation.BaSxyOperationBuilder(this, idShort);
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            SubmodelElementCollectionBuilder result = DeferredBuilder.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class, instance.deferred);
            if (null == result) {
                SubmodelElementCollection sub = instance.getSubmodelElementCollection(idShort);
                if (null == sub) {
                    result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, idShort, 
                        ordered, allowDuplicates);
                } else {
                    result = new BaSyxSubmodelElementCollection.BaSyxSubmodelElementCollectionBuilder(this, 
                       (BaSyxSubmodelElementCollection) sub);
                }
            }
            return result;
        }

        @Override
        public SubmodelElementListBuilder createSubmodelElementListBuilder(String idShort) {
            SubmodelElementListBuilder result = DeferredBuilder.getDeferred(idShort, 
                SubmodelElementListBuilder.class, instance.deferred);
            if (null == result) {
                SubmodelElementList sub = instance.getSubmodelElementList(idShort);
                if (null == sub) {
                    result = new BaSyxSubmodelElementList.BaSyxSubmodelElementListBuilder(this, idShort);
                } else {
                    result = new BaSyxSubmodelElementList.BaSyxSubmodelElementListBuilder(this, 
                       (BaSyxSubmodelElementList) sub);
                }
            }
            return result;
        }
        
        @Override
        public SubmodelElementContainerBuilder createSubmodelElementContainerBuilder(String idShort) {
            SubmodelElementContainerBuilder result;
            SubmodelElement sub = instance.getSubmodelElement(idShort);
            if (sub instanceof SubmodelElementList) {
                result = createSubmodelElementListBuilder(idShort);
            } else {
                result = createSubmodelElementCollectionBuilder(idShort);
            }
            return result;
        }

        @Override
        public Reference createReference() {
            return BaSyxReference.createModelReference(list);
        }

        @Override
        protected BaSyxFile register(BaSyxFile file) {
            addSubmodelElement(this.list, file.getSubmodelElement());
            return instance.register(file);
        }

        @Override
        protected BaSyxRange register(BaSyxRange range) {
            addSubmodelElement(this.list, range.getSubmodelElement());
            return instance.register(range);
        }

        @Override
        protected BaSyxBlob register(BaSyxBlob blob) {
            addSubmodelElement(this.list, blob.getSubmodelElement());
            return instance.register(blob);
        }

        @Override
        protected BaSyxOperation register(BaSyxOperation operation) {
            addSubmodelElement(this.list, operation.getSubmodelElement());
            return instance.register(operation);
        }
        
        @Override
        protected BaSyxProperty register(BaSyxProperty property) {
            addSubmodelElement(this.list, property.getSubmodelElement());
            return instance.register(property);
        }

        @Override
        protected BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
            addSubmodelElement(this.list, property.getSubmodelElement());
            return instance.register(property);
        }

        @Override
        protected BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
            addSubmodelElement(this.list, relationship.getSubmodelElement());
            return instance.register(relationship);
        }

        @Override
        protected BaSyxEntity register(BaSyxEntity entity) {
            addSubmodelElement(this.list, entity.getSubmodelElement());
            return instance.register(entity);
        }
        
        @Override
        protected BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            addSubmodelElement(this.list, reference.getSubmodelElement());
            return instance.register(reference);
        }

        @Override
        protected BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection, 
            boolean propagate) {
            addSubmodelElement(this.list, collection.getSubmodelElement());
            if (propagate) {
                instance.register(collection);
            }
            return collection;
        }

        @Override
        protected BaSyxSubmodelElementList register(BaSyxSubmodelElementList collection, 
            boolean propagate) {
            addSubmodelElement(this.list, collection.getSubmodelElement());
            if (propagate) {
                instance.register(collection);
            }
            return collection;
        }

        @Override
        public void defer() {
            parentBuilder.defer(list.getIdShort(), this);
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
            parentBuilder.buildMyDeferred();
        }

        @Override
        public BaSyxSubmodelElementList build() {
            buildMyDeferred();
            instance.list = list;
            if (isNew) {
                parentBuilder.register(instance, propagate);
            } 
            return updateInBuild(isNew, instance);
        }

        @Override
        public AasBuilder getAasBuilder() {
            return parentBuilder.getAasBuilder();
        }

        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return parentBuilder;
        }

        @Override
        protected AbstractSubmodel<org.eclipse.digitaltwin.aas4j.v3.model.Submodel> getInstance() {
            return null;
        }

        @Override
        public boolean isNew() {
            return isNew;
        }
        
        @Override
        public boolean hasElement(String idShort) {
            return instance.getElement(idShort) != null;
        }
        
        /**
         * Enables or disables registration propagation into the interface instance.
         * 
         * @param propagate enable or disable propagation
         */
        void setPropagation(boolean propagate) {
            this.propagate = propagate;            
        }
        
    }
    
    /**
     * Creates an instance. Prevents external creation.
     */
    protected BaSyxSubmodelElementList() {
    }
 
    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param list the list instance
     */
    protected BaSyxSubmodelElementList(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList list) {
        this.list = list;
    }
    
    /**
     * Adds a submodel element.
     * 
     * @param list the list to add the element to
     * @param element the element to add
     */
    static void addSubmodelElement(org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList list,
        org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement element) {
        list.setValue(Tools.addElement(list.getValue(), element));
    }
    
    /**
     * Creates the actual data structure to use. {@link #elementsList} shall be set before.
     */
    private void createElementsStructure() {
        elementsList = new ArrayList<SubmodelElement>();
    }

    /**
     * Dynamically initializes the elements structure.
     */
    private void initialize() {
        if (null == elementsList) {
            createElementsStructure();
            BaSyxElementTranslator.registerSubmodelElements(list.getValue(), this);        
        }
    }

    @Override
    public int getElementsCount() {
        initialize();
        return elementsList.size();
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return elements();
    }

    @Override
    public Iterable<SubmodelElement> elements() {
        return elementsCollection();
    }

    /**
     * Returns the elements as collection.
     * 
     * @return the collection
     */
    private Collection<SubmodelElement> elementsCollection() {
        initialize();
        return elementsList;
    }
    
    /**
     * Returns the elements as stream.
     * 
     * @return the stream
     * @see #elementsCollection()
     */
    private Stream<SubmodelElement> elementsStream() {
        return elementsCollection().stream();
    }
    
    /**
     * Adds an element.
     * 
     * @param <T> the actual type of the element
     * @param elt the element
     * @return {@code elt}
     */
    private <T extends SubmodelElement> T add(T elt) {
        elementsList.add(elt);
        if (elt instanceof BaSyxSubmodelElement) {
            ((BaSyxSubmodelElement) elt).setParent(this);
        }
        return elt;
    }
    
    /**
     * Returns an iterable of submodel elements complying to a given predicate.
     * 
     * @param filter the filter
     * @return the iterable
     */
    protected Iterable<SubmodelElement> getElements(Predicate<SubmodelElement> filter) {
        return new Iterable<SubmodelElement>() {
            
            @Override
            public Iterator<SubmodelElement> iterator() {
                return elementsStream()
                    .filter(filter)
                    .iterator();
            }
        };
    }
    
    /**
     * Returns an iterable of submodel elements complying to a given predicate and a given target cast type.
     * 
     * @param <E> the type of submodel element
     * @param filter the filter
     * @param castType the type to filter for and cast to
     * @return the iterable
     */
    protected <E extends SubmodelElement> Iterable<E> getElements(Predicate<SubmodelElement> filter, 
        Class<E> castType) {
        return new Iterable<E>() {
            
            @Override
            public Iterator<E> iterator() {
                return elementsStream()
                    .filter(e -> castType.isInstance(e))
                    .filter(filter)
                    .map(e -> castType.cast(e))
                    .iterator();
            }
        };
    }

    /**
     * Returns an iterable of submodel element collections complying to a given predicate.
     * 
     * @param filter the filter
     * @return the iterable
     */
    protected Iterable<SubmodelElementCollection> getSubmodelElementCollections(
        Predicate<SubmodelElementCollection> filter) {
        return new Iterable<SubmodelElementCollection>() {
            
            @Override
            public Iterator<SubmodelElementCollection> iterator() {
                return elementsStream()
                    .filter(e -> e instanceof SubmodelElementCollection)
                    .map(SubmodelElementCollection.class::cast)
                    .filter(filter)
                    .iterator();
            }
        };
    }

    @Override
    public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList getSubmodelElement() {
        return list;
    }

    @Override
    public DataElement getDataElement(String idShort) {
        return getElement(idShort, DataElement.class);
    }

    @Override
    public Property getProperty(String idShort) {
        return getElement(idShort, Property.class);
    }

    @Override
    public Operation getOperation(String idShort) {
        return getElement(idShort, Operation.class);
    }

    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        return getElement(idShort, ReferenceElement.class);
    }

    @Override
    public RelationshipElement getRelationshipElement(String idShort) {
        return getElement(idShort, RelationshipElement.class);
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return getElement(idShort);
    }

    @Override
    public SubmodelElement getElement(String idShort) {
        initialize();
        Optional<SubmodelElement> tmp = elementsStream()
            .filter(s -> s.getIdShort().equals(idShort))
            .findFirst();
        return tmp.isPresent() ? tmp.get() : null; 
    }
    

    @Override
    public SubmodelElement getElement(int index) {
        initialize();
        return elementsList.get(index);
    }

    @Override
    public void deleteElement(int index) {
        initialize();
        elementsList.remove(index);
    }

    /**
     * {@link #getElement(String)} combined with a type filter.
     * 
     * @param <T> the type
     * @param idShort the short id to search for
     * @param type the class representing the type
     * @return the element with given type or <b>null</b> for none
     */
    private <T extends SubmodelElement> T getElement(String idShort, Class<T> type) {
        T result = null;
        SubmodelElement tmp = getElement(idShort);
        if (type.isInstance(tmp)) {
            result = type.cast(tmp);
        }
        return result;
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return getElement(idShort, SubmodelElementCollection.class);
    }

    @Override
    public SubmodelElementList getSubmodelElementList(String idShort) {
        return getElement(idShort, SubmodelElementList.class);
    }

    @Override
    public Entity getEntity(String idShort) {
        return getElement(idShort, Entity.class);
    }

    @Override
    public <T extends SubmodelElement> T registerElement(T elt) {
        return add(elt); // TODO move add here?
    }
    
    @Override
    public BaSyxProperty register(BaSyxProperty property) {
        return add(property);
    }

    @Override
    public BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
        return add(property);
    }

    @Override
    public BaSyxBlob register(BaSyxBlob blob) {
        return add(blob);
    }

    @Override
    public BaSyxFile register(BaSyxFile file) {
        return add(file);
    }
    
    @Override
    public BaSyxRange register(BaSyxRange range) {
        return add(range);
    }    

    @Override
    public BaSyxOperation register(BaSyxOperation operation) {
        return add(operation);
    }

    @Override
    public BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
        return add(relationship);
    }

    @Override
    public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
        return add(reference);
    }

    @Override
    public BaSyxEntity register(BaSyxEntity reference) {
        return add(reference);
    }

    @Override
    public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        return add(collection);
    }

    @Override
    public BaSyxSubmodelElementList register(BaSyxSubmodelElementList list) {
        return add(list);
    }

    @Override
    public <D extends org.eclipse.digitaltwin.aas4j.v3.model.DataElement> 
        BaSyxDataElement<D> register(BaSyxDataElement<D> dataElement) {
        return add(dataElement);
    }

    @Override
    public void accept(AasVisitor visitor) {
        initialize();
        visitor.visitSubmodelElementList(this);
        for (SubmodelElement se : elementsCollection()) {
            se.accept(visitor);
        }
        visitor.endSubmodelElementList(this);
    }

    @Override
    public Reference createReference() {
        return BaSyxReference.createModelReference(list);
    }
    
    /**
     * Removes a submodel element through its idShort.
     * 
     * @param idShort the idshort to remove 
     */
    void deleteSubmodelElement(String idShort) {
        list.setValue(Tools.removeElements(list.getValue(), e -> e.getIdShort().equals(idShort)));
        updateConnectedSubmodelElement();
    }

    @Override
    public void deleteElement(String idShort) {
        initialize();
        for (int i = 0; i < elementsList.size(); i++) {
            SubmodelElement elt = elementsList.get(i);
            if (elt.getIdShort().equalsIgnoreCase(idShort)) {
                elementsList.remove(i);
                deleteSubmodelElement(idShort);
                break;
            }
        }
    }

    @Override
    public void update() {
        elementsList = null;
        
        if (null != getParent()) {
            org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement elt = getParent().processOnPath(
                CollectionUtils.toList(list), true, (sId, p, r) -> r.getSubmodelElement(sId, p));
            if (elt instanceof org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) {
                list = (org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) elt;
            }
        }
    }

    @Override
    public org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement getPathElement() {
        return list;
    }

}
