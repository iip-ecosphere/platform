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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElementCollection;

import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxElementTranslator.SubmodelElementsRegistrar;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;

/**
 * Wrapper for the BaSyx sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodelElementCollection extends BaSyxSubmodelElement implements SubmodelElementCollection, 
    SubmodelElementsRegistrar {
    
    private ISubmodelElementCollection collection;
    private Map<String, SubmodelElement> elements;
    private Map<String, Builder<?>> deferred;
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxSubmodelElementCollectionBuilder extends BaSyxSubmodelElementContainerBuilder<ISubmodel> 
        implements SubmodelElementCollectionBuilder {
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxSubmodelElementCollection instance;
        private ISubmodelElementCollection collection;
        private boolean isNew = true;
        
        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the sub-model element
         * @param ordered whether the collection is ordered
         * @param allowDuplicates whether the collection allows duplicates
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        protected BaSyxSubmodelElementCollectionBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            String idShort, boolean ordered, boolean allowDuplicates) {
            this(parentBuilder, idShort, () -> new BaSyxSubmodelElementCollection(), () -> {
                org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection coll 
                    = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection(); 
                coll.setIdShort(Tools.checkId(idShort));
                coll.setOrdered(ordered);
                coll.setAllowDuplicates(allowDuplicates);
                return coll;
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
        protected BaSyxSubmodelElementCollectionBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            String idShort, Supplier<BaSyxSubmodelElementCollection> wCreator, 
            Supplier<org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection> bCreator) {
            this.parentBuilder = parentBuilder;
            this.instance = wCreator.get();
            this.instance.elements = new HashMap<String, SubmodelElement>();
            this.collection = bCreator.get();
        }

        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        protected BaSyxSubmodelElementCollectionBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, 
            BaSyxSubmodelElementCollection instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.isNew = false;
            if (instance.collection instanceof ISubmodelElementCollection) {
                this.collection = (ISubmodelElementCollection) instance.collection;
            } else {
                throw new IllegalArgumentException("Cannot create a " + getClass().getSimpleName() + " on a " 
                    + instance.collection.getClass().getSimpleName());
            }
            this.instance.initialize();
        }
        
        /**
         * Returns the BaSyx collection created by this instance.
         * 
         * @return the collection
         */
        protected ISubmodelElementCollection getCollection() {
            return collection;
        }
        
        /**
         * Returns the collection instance being created.
         * 
         * @return the collection instance
         */
        protected BaSyxSubmodelElementCollection getCollectionInstance() {
            return instance;
        }

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
        public Reference createReference() {
            return new BaSyxReference(collection.getReference());
        }

        @Override
        protected BaSyxOperation register(BaSyxOperation operation) {
            this.collection.addSubmodelElement(operation.getSubmodelElement());
            return instance.register(operation);
        }
        
        @Override
        protected BaSyxProperty register(BaSyxProperty property) {
            this.collection.addSubmodelElement(property.getSubmodelElement());
            BaSyxProperty p = instance.register(property);
            return p;
        }

        @Override
        protected BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            this.collection.addSubmodelElement(reference.getSubmodelElement());
            return instance.register(reference);
        }

        @Override
        protected BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
            this.collection.addSubmodelElement(collection.getSubmodelElement());
            return instance.register(collection);
        }
        
        @Override
        public void defer() {
            parentBuilder.defer(collection.getIdShort(), this);
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
        public BaSyxSubmodelElementCollection build() {
            buildMyDeferred();
            instance.collection = collection;
            if (isNew) {
                parentBuilder.register(instance);
            }
            return instance;
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
        protected AbstractSubmodel<ISubmodel> getInstance() {
            return null;
        }

        @Override
        public boolean isNew() {
            return isNew;
        }
        
    }
    
    /**
     * Creates an instance. Prevents external creation.
     */
    protected BaSyxSubmodelElementCollection() {
    }
 
    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param collection the collection instance
     */
    protected BaSyxSubmodelElementCollection(ISubmodelElementCollection collection) {
        this.collection = collection;
    }

    /**
     * Dynamically initializes the elements structure.
     */
    private void initialize() {
        if (null == elements) {
            elements = new HashMap<String, SubmodelElement>();
            BaSyxElementTranslator.registerSubmodelElements(collection.getSubmodelElements(), this);        
        }
    }

    @Override
    public int getElementsCount() {
        initialize();
        return elements.size();
    }
    
    @Override
    public Iterable<SubmodelElement> elements() {
        initialize();
        return elements.values();
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
                return elements
                    .values()
                    .stream()
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
                return elements
                    .values()
                    .stream()
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
                return elements
                    .values()
                    .stream()
                    .filter(e -> e instanceof SubmodelElementCollection)
                    .map(SubmodelElementCollection.class::cast)
                    .filter(filter)
                    .iterator();
            }
        };
    }

    @Override
    public String getIdShort() {
        return collection.getIdShort();
    }

    @Override
    public ISubmodelElementCollection getSubmodelElement() {
        return collection;
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
    public SubmodelElement getElement(String idShort) {
        initialize();
        return elements.get(idShort);
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
    public BaSyxProperty register(BaSyxProperty property) {
        elements.put(property.getIdShort(), property);
        return property;
    }

    @Override
    public BaSyxOperation register(BaSyxOperation operation) {
        elements.put(operation.getIdShort(), operation);
        return operation;
    }

    @Override
    public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
        elements.put(reference.getIdShort(), reference);
        return reference;
    }

    @Override
    public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        elements.put(collection.getIdShort(), collection);
        return collection;
    }
    
    @Override
    public <D extends org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.DataElement> 
        BaSyxDataElement<D> register(BaSyxDataElement<D> dataElement) {
        elements.put(dataElement.getIdShort(),  dataElement);
        return dataElement;
    }

    @Override
    public void accept(AasVisitor visitor) {
        initialize();
        visitor.visitSubmodelElementCollection(this);
        for (SubmodelElement se : elements.values()) {
            se.accept(visitor);
        }
        visitor.endSubmodelElementCollection(this);
    }

    @Override
    public Reference createReference() {
        return new BaSyxReference(collection.getReference());
    }

    @Override
    public void deleteElement(String idShort) {
        initialize();
        if (elements.containsKey(idShort)) {
            elements.remove(idShort);
            collection.deleteSubmodelElement(idShort);
        }
    }

    @Override
    public void update() {
        elements = null;
    }

}
