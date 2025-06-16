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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.basyx2.BaSyxElementTranslator.SubmodelElementsRegistrar;
import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;

/**
 * Implements the entity wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxEntity extends BaSyxSubmodelElement implements Entity, SubmodelElementsRegistrar {
    
    private org.eclipse.digitaltwin.aas4j.v3.model.Entity entity;
    private Map<String, Builder<?>> deferred;
    private List<SubmodelElement> statementsList; // not nice, shall be two implementations
    
    /**
     * Implements the entity builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxEntityBuilder extends BaSyxSubmodelElementContainerBuilder<
        org.eclipse.digitaltwin.aas4j.v3.model.Submodel> implements EntityBuilder {
        
        private BaSyxSubmodelElementContainerBuilder<?> parentBuilder;
        private BaSyxEntity instance;
        private org.eclipse.digitaltwin.aas4j.v3.model.Entity entity;
        private List<org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement> statements = new ArrayList<>();
        private boolean isNew = true;
        
        /**
         * Creates a builder instance.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short id of the reference element
         * @param type the entity type
         * @param asset the asset of the entity, may be <b>null</b> for none
         */
        BaSyxEntityBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, String idShort, EntityType type, 
            Reference asset) {
            if (null == idShort || 0 == idShort.length()) {
                throw new IllegalArgumentException("idShort must be given");
            }
            this.parentBuilder = parentBuilder;
            instance = new BaSyxEntity();
            entity = new org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEntity();
            entity.setIdShort(idShort);
            entity.setEntityType(Tools.translate(type));
            /*if (asset instanceof BaSyxReference) { // TODO not supported anymore
                entity.setGlobalAssetId(((BaSyxReference) asset).getAsset());
            }*/
        }

        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        protected BaSyxEntityBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder, BaSyxEntity instance) {
            this.parentBuilder = parentBuilder;
            this.instance = instance;
            this.isNew = false;
            this.entity = instance.entity;
            this.instance.initialize();
        }

        @Override
        public BaSyxSubmodelElementContainerBuilder<?> getParentBuilder() {
            return parentBuilder;
        }

        @Override
        public Entity build() {
            instance.entity = entity;
            instance.entity.setStatements(statements);
            return updateInBuild(isNew, parentBuilder.register(instance));
        }

        @Override
        public EntityBuilder setSemanticId(String refValue) {
            return Tools.setSemanticId(this, refValue, entity);
        }

        @Override
        public EntityBuilder setDescription(LangString... description) {
            entity.setDescription(Tools.translate(description));
            return this;
        }

        @Override
        public Reference createReference() {
            return BaSyxReference.createModelReference(entity);
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
        public AasBuilder getAasBuilder() {
            return parentBuilder.getAasBuilder();
        }

        @Override
        public boolean isNew() {
            return isNew;
        }

        @Override
        public boolean hasElement(String idShort) {
            return instance.getElement(idShort) != null;
        }

        @Override
        protected AbstractSubmodel<org.eclipse.digitaltwin.aas4j.v3.model.Submodel> getInstance() {
            return null;
        }
        
        @Override
        protected BaSyxFile register(BaSyxFile file) {
            statements.add(file.getSubmodelElement());
            return instance.add(file);
        }

        @Override
        protected BaSyxBlob register(BaSyxBlob blob) {
            statements.add(blob.getSubmodelElement());
            return instance.add(blob);
        }

        @Override
        protected BaSyxOperation register(BaSyxOperation operation) {
            statements.add(operation.getSubmodelElement());
            return instance.add(operation);
        }
        
        @Override
        protected BaSyxProperty register(BaSyxProperty property) {
            statements.add(property.getSubmodelElement());
            return instance.add(property);
        }

        @Override
        protected BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
            statements.add(property.getSubmodelElement());
            return instance.add(property);
        }

        @Override
        protected BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
            statements.add(relationship.getSubmodelElement());
            return instance.add(relationship);
        }

        @Override
        protected BaSyxEntity register(BaSyxEntity entity) {
            statements.add(entity.getSubmodelElement());
            return instance.add(entity);
        }
        
        @Override
        protected BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            statements.add(reference.getSubmodelElement());
            return instance.add(reference);
        }

        @Override
        public EntityBuilder setEntityType(EntityType type) {
            entity.setEntityType(Tools.translate(type));
            return this;
        }

        @Override
        public EntityBuilder setAsset(Reference asset) {
            /*if (asset instanceof BaSyxReference) { // TODO not supported anymore
                entity.setAsset(((BaSyxReference) asset).getReference());
            }*/
            return this;
        }
        
    }
    
    /**
     * Creates an instance. Prevents external access.
     */
    private BaSyxEntity() {
    }
    
    /**
     * Creates an instance and directly sets the entity.
     * 
     * @param entity the entity
     */
    BaSyxEntity(org.eclipse.digitaltwin.aas4j.v3.model.Entity entity) {
        this.entity = entity;
    }
    
    /**
     * Creates the actual data structure to use. {@link #statementsList} shall be set before.
     */
    private void createElementsStructure() {
        statementsList = new ArrayList<SubmodelElement>();
    }
    
    /**
     * Dynamically initializes the elements structure.
     */
    private void initialize() {
        if (null == statementsList) {
            createElementsStructure();
            BaSyxElementTranslator.registerSubmodelElements(entity.getStatements(), this);        
        }
    }
    
    /**
     * Returns the BaSyx entity.
     * 
     * @return the BaSyx entity
     */
    org.eclipse.digitaltwin.aas4j.v3.model.Entity getEntity() {
        return entity;
    }

    @Override
    org.eclipse.digitaltwin.aas4j.v3.model.Entity getSubmodelElement() {
        return entity;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitEntity(this);
        for (SubmodelElement se : visitor.sortSubmodelElements(statementsList)) {
            se.accept(visitor);
        }
        visitor.endVisitEntity(this);
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return elements();
    }

    @Override
    public Iterable<SubmodelElement> elements() {
        initialize();
        return statementsList;
    }

    @Override
    public int getElementsCount() {
        initialize();
        return statementsList.size();
    }

    /**
     * Returns the elements/statements as stream.
     * 
     * @return the stream
     */
    private Stream<SubmodelElement> elementsStream() {
        initialize();
        return statementsList.stream();
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return getElement(idShort);
    }

    @Override
    public SubmodelElement getElement(String idShort) {
        initialize();
        SubmodelElement result = null;
        Optional<SubmodelElement> tmp = elementsStream()
            .filter(s -> s.getIdShort().equals(idShort))
            .findFirst();
        result = tmp.isPresent() ? tmp.get() : null; 
        return result;
    }

    @Override
    public Reference createReference() {
        return BaSyxReference.createModelReference(entity);
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
    public void deleteElement(String idShort) {
        initialize();
        statementsList.removeIf(s -> s.getIdShort().equals(idShort));
        entity.getStatements().removeIf(s -> s.getIdShort().equals(idShort));
    }
    
    @Override
    public EntityType getType() {
        return Tools.translate(entity.getEntityType());
    }
    
    /**
     * Adds an element.
     * 
     * @param <T> the actual type of the element
     * @param elt the element
     * @return {@code elt}
     */
    private <T extends SubmodelElement> T add(T elt) {
        if (null == statementsList) {
            statementsList = new ArrayList<>();
        }
        statementsList.add(elt);
        return elt;
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
    public BaSyxBlob register(BaSyxBlob blob) {
        return add(blob);
    }

    @Override
    public BaSyxFile register(BaSyxFile file) {
        return add(file);
    }

    @Override
    public BaSyxMultiLanguageProperty register(BaSyxMultiLanguageProperty property) {
        return add(property);
    }

    @Override
    public BaSyxRelationshipElement register(BaSyxRelationshipElement relationship) {
        return add(relationship);
    }

    @Override
    public BaSyxEntity register(BaSyxEntity entity) {
        return add(entity);
    }

    @Override
    public BaSyxOperation register(BaSyxOperation operation) {
        return add(operation);
    }

    @Override
    public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
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

}
