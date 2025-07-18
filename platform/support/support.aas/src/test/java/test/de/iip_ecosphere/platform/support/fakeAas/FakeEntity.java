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

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import test.de.iip_ecosphere.platform.support.fakeAas.FakeSubmodelElementCollection
    .FakeSubmodelElementCollectionBuilder;
import test.de.iip_ecosphere.platform.support.fakeAas.FakeSubmodelElementList.FakeSubmodelElementListBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;

/**
 * Implements a fake entity.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeEntity extends FakeElement implements Entity {
    
    private EntityType type;
    private Map<String, SubmodelElement> elements = new HashMap<>();
    private Map<String, Builder<?>> deferred;
   
    static class FakeEntityBuilder extends FakeSubmodelElementContainerBuilder implements EntityBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeEntity instance;
        private boolean isNew = true;

        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param type the entity type
         * @param asset the asset, may be <b>null</b>
         */
        FakeEntityBuilder(FakeSubmodelElementContainerBuilder parent, String idShort, EntityType type, 
            Reference asset) {
            this.parent = parent;
            this.instance = new FakeEntity(idShort);
            this.instance.type = type;
        }
        
        /**
         * Creates a builder instance for an existing instance.
         * 
         * @param parent the parent builder
         * @param instance the instance
         */
        protected FakeEntityBuilder(FakeSubmodelElementContainerBuilder parent, 
            FakeEntity instance) {
            this.parent = parent;
            this.instance = instance;
            this.isNew = false;
        }
        
        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return parent;
        }

        @Override
        public EntityBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }
        
        @Override
        public EntityBuilder setDescription(LangString... description) {
            // ignoring for now
            return this;
        }

        @Override
        public Entity build() {
            parent.register(instance);
            return instance;
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
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort) {
            SubmodelElementCollectionBuilder result = DeferredBuilder.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class, instance.deferred);
            if (null == result) {
                result = new FakeSubmodelElementCollectionBuilder(this, idShort, false, false); 
            }
            return result; 
        }

        @Override
        public SubmodelElementListBuilder createSubmodelElementListBuilder(String idShort) {
            SubmodelElementListBuilder result = DeferredBuilder.getDeferred(idShort, 
                SubmodelElementListBuilder.class, instance.deferred);
            if (null == result) {
                result = new FakeSubmodelElementListBuilder(this, idShort); 
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
        public boolean hasElement(String idShort) {
            return instance.elements.containsKey(idShort);
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
        void defer(String shortId, Builder<?> builder) {
            instance.deferred = DeferredBuilder.defer(shortId, builder, instance.deferred);
        }

        @Override
        void buildMyDeferred() {
            parent.buildMyDeferred();
        }

        @Override
        public EntityBuilder setEntityType(EntityType type) {
            instance.type = type;
            return this;
        }

        @Override
        public EntityBuilder setAsset(Reference asset) {
            // ignored for now
            return this;
        }

        @Override
        public EntityBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            // ignore parent paths here
            return AuthenticationDescriptor.elementRbac(this, auth, role, instance.getIdShort(), actions);
        }

        @Override
        public EntityBuilder rbac(AuthenticationDescriptor auth) {
            return this; // usually not needed
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the short id
     */
    protected FakeEntity(String idShort) {
        super(idShort);
    }
    
    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitEntity(this);
        for (SubmodelElement se : visitor.sortSubmodelElements(elements.values())) {
            se.accept(visitor);
        }
        visitor.endVisitEntity(this);
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return elements.values();
    }
    
    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return elements.get(idShort);
    }
    
    @Override
    public Iterable<SubmodelElement> elements() {
        return elements.values();
    }

    @Override
    public int getElementsCount() {
        return elements.size();
    }

    @Override
    public SubmodelElement getElement(String idShort) {
        return elements.get(idShort);
    }

    @Override
    public Reference createReference() {
        return new FakeReference();
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return filter(idShort, SubmodelElementCollection.class);
    }

    @Override
    public SubmodelElementList getSubmodelElementList(String idShort) {
        return filter(idShort, SubmodelElementList.class);
    }

    @Override
    public Entity getEntity(String idShort) {
        return filter(idShort, Entity.class);
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

    @Override
    public DataElement getDataElement(String idShort) {
        return filter(idShort, DataElement.class);
    }

    @Override
    public Property getProperty(String idShort) {
        return filter(idShort, Property.class);
    }

    @Override
    public Operation getOperation(String idShort) {
        return filter(idShort, Operation.class);
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
    public void deleteElement(String idShort) {
        elements.remove(idShort);
    }

    @Override
    public EntityType getType() {
        return type;
    }

}
