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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.FileDataElement.FileDataElementBuilder;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement;
import de.iip_ecosphere.platform.support.aas.RelationshipElement.RelationshipElementBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;
import test.de.iip_ecosphere.platform.support.fakeAas.FakeAas.FakeAasBuilder;
import test.de.iip_ecosphere.platform.support.fakeAas.FakeSubmodelElementList.FakeSubmodelElementListBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList;

/**
 * A fake (inefficient) submodel.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeSubmodel extends FakeElement implements Submodel {

    private FakeAas parent;
    private Map<String, SubmodelElement> elements = new HashMap<>();
    private Map<String, Builder<?>> deferred;
    private String identifier;
    
    /**
     * A fake sub-model builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class FakeSubmodelBuilder extends FakeSubmodelElementContainerBuilder implements SubmodelBuilder {

        private FakeAasBuilder parent;
        private FakeSubmodel instance;
        private boolean isNew = true;

        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        protected FakeSubmodelBuilder(FakeAasBuilder parent, String idShort) {
            this(parent, idShort, null);
        }

        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         */
        FakeSubmodelBuilder(FakeAasBuilder parent, String idShort, String identifier) {
            this.parent = parent;
            this.instance = createInstance(idShort, identifier);
            this.instance.parent = null != parent ? parent.getInstance() : null;
        }

        /**
         * Creates a builder for an existing instance.
         * 
         * @param parent the parent
         * @param instance the instance
         */
        public FakeSubmodelBuilder(FakeAasBuilder parent, FakeSubmodel instance) {
            this.parent = parent;
            this.instance = instance;
            this.isNew = false;
        }
        
        /**
         * Creates the instance.
         * 
         * @param idShort the short id
         * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         * @return the instance
         */
        protected FakeSubmodel createInstance(String idShort, String identifier) {
            return new FakeSubmodel(idShort, identifier);
        }
        
        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort) {
            SubmodelElementCollectionBuilder result = instance.getDeferred(idShort, 
                SubmodelElementCollectionBuilder.class);
            if (null == result) {
                result = new FakeSubmodelElementCollection.FakeSubmodelElementCollectionBuilder(this, idShort, false, 
                    false);
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
            return instance.createSubmodelElementContainerBuilder(idShort);
        }

        @Override
        public Reference createReference() {
            return new FakeReference();
        }

        @Override
        public void defer() {
            parent.defer(instance.getIdShort(), this);
        }

        @Override
        public void buildDeferred() {
            parent.buildMyDeferred();
        }

        @Override
        public Submodel build() {
            return parent.register(instance);
        }
        
        @Override
        <T extends SubmodelElement> T registerElement(T elt) {
            instance.elements.put(elt.getIdShort(), elt);
            return elt;
        }
        
        @Override
        public SubmodelElementContainerBuilder getParentBuilder() {
            return null;
        }

        @Override
        public AasBuilder getAasBuilder() {
            return parent;
        }

        @Override
        public boolean isNew() {
            return isNew;
        }
        
        @Override
        public boolean hasElement(String idShort) {
            return instance.elements.containsKey(idShort);
        }

        @Override
        void defer(String shortId, Builder<?> builder) {
            instance.defer(shortId, builder);
        }

        @Override
        void buildMyDeferred() {
            instance.buildDeferred();
        }
        
        /**
         * Returns the instance.
         * 
         * @return the instance
         */
        protected FakeSubmodel getInstance() {
            return instance;
        }

        @Override
        public FileDataElementBuilder createFileDataElementBuilder(String idShort, String contents, String mimeType) {
            return new FakeFileDataElement.FakeFileDataElementBuilder(this, idShort, contents, mimeType);
        }

        @Override
        public SubmodelBuilder setSemanticId(String semanticId) {
            instance.setSemanticId(semanticId);
            return this;
        }

        @Override
        public RelationshipElementBuilder createRelationshipElementBuilder(String idShort, Reference first,
                Reference second) {
            return new FakeRelationshipElement.FakeRelationshipElementBuilder(this, idShort, first, second);
        }
        
        @Override
        public SubmodelBuilder rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions) {
            return AuthenticationDescriptor.submodelRbac(this, auth, role, getInstance().getIdShort(), actions);
        }

    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     * @param identifier the identifier of the sub-model (may be <b>null</b> or empty for an identification based on
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
     */
    protected FakeSubmodel(String idShort, String identifier) {
        super(idShort);
        this.identifier = identifier;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitSubmodel(this);
        for (SubmodelElement elt : visitor.sortSubmodelElements(filter(Property.class))) {
            elt.accept(visitor);
        }
        for (SubmodelElement elt : visitor.sortSubmodelElements(filter(DataElement.class))) {
            if (!(elt instanceof Property)) {
                elt.accept(visitor);
            }
        }
        for (SubmodelElement elt : visitor.sortSubmodelElements(filter(Operation.class))) {
            elt.accept(visitor);
        }
        for (SubmodelElement se : visitor.sortSubmodelElements(elements.values())) {
            // remaining elements, don't iterate over them again
            if (!(se instanceof Property || se instanceof DataElement || se instanceof Operation)) {
                se.accept(visitor);
            }
        }        
        visitor.endSubmodel(this);
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return elements.values();
    }

    @Override
    public int getSubmodelElementsCount() {
        return elements.size();
    }
    
    /**
     * Filters {@code #elements()} for the given type.
     * 
     * @param <T> the element type to return
     * @param type the element type
     * @return the found element or <b>null</b>
     */
    private <T extends SubmodelElement> List<T> filter(Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (SubmodelElement sm : submodelElements()) {
            if (type.isInstance(sm)) {
                result.add(type.cast(sm));
            }
        }
        return result;
    }
    
    /**
     * Filters {@code #submodelElements()} for the given short id and type.
     * 
     * @param <T> the element type to return
     * @param idShort the short id
     * @param type the element type
     * @return the found element or <b>null</b>
     */
    private <T extends SubmodelElement> T filter(String idShort, Class<T> type) {
        T result = null;
        for (SubmodelElement sm : submodelElements()) {
            if (sm.getIdShort().equals(idShort) && type.isInstance(sm)) {
                result = type.cast(sm);
            }
        }
        return result;
    }

    @Override
    public Iterable<DataElement> dataElements() {
        return filter(DataElement.class);
    }

    @Override
    public Iterable<Property> properties() {
        return filter(Property.class);
    }

    @Override
    public int getDataElementsCount() {
        return filter(DataElement.class).size();
    }

    @Override
    public Iterable<Operation> operations() {
        return filter(Operation.class);
    }

    @Override
    public int getOperationsCount() {
        return filter(Operation.class).size();
    }


    @Override
    public int getPropertiesCount() {
        return filter(Property.class).size();
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
    public Operation getOperation(String idShort) {
        return filter(idShort, Operation.class);
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return filter(idShort, SubmodelElement.class);
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
    
    @Override
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort) {
        SubmodelElementCollectionBuilder result = getDeferred(idShort, SubmodelElementCollectionBuilder.class);
        if (null == result) {
            FakeSubmodelElementContainerBuilder secb = new FakeSubmodel.FakeSubmodelBuilder(
                new FakeAasBuilder(parent), this);
            result = new FakeSubmodelElementCollection.FakeSubmodelElementCollectionBuilder(
                secb, idShort, false, false);
        }
        return result;
    }

    @Override
    public SubmodelElementListBuilder createSubmodelElementListBuilder(String idShort) {
        SubmodelElementListBuilder result = getDeferred(idShort, SubmodelElementListBuilder.class);
        if (null == result) {
            FakeSubmodelElementContainerBuilder secb = new FakeSubmodel.FakeSubmodelBuilder(
                new FakeAasBuilder(parent), this);
            result = new FakeSubmodelElementList.FakeSubmodelElementListBuilder(secb, idShort);
        }
        return result;
    }
    
    @Override
    public SubmodelElementContainerBuilder createSubmodelElementContainerBuilder(String idShort) {
        SubmodelElementContainerBuilder result;
        SubmodelElement sub = getSubmodelElement(idShort);
        if (sub instanceof SubmodelElementList) {
            result = createSubmodelElementListBuilder(idShort);
        } else {
            result = createSubmodelElementCollectionBuilder(idShort);
        }
        return result;
    }

    @Override
    public Reference createReference() {
        return new FakeReference();
    }

    @Override
    public void deleteElement(String idShort) {
        elements.remove(idShort);
    }

    /**
     * Registers a sub-build as deferred.
     * 
     * @param shortId the shortId of the element
     * @param builder the sub-builder to be registered
     * @see #buildDeferred()
     */
    void defer(String shortId, Builder<?> builder) {
        deferred = DeferredBuilder.defer(shortId, builder, deferred);
    }

    @Override
    public void buildDeferred() {
        DeferredBuilder.buildDeferred(deferred);
    }

    /**
     * Returns a deferred builder.
     * 
     * @param <B> the builder type
     * @param shortId the short id
     * @param cls the builder type
     * @return the builder or <b>null</b> if no builder for {@code shortId} with the respective type is registered
     */
    <B extends Builder<?>> B getDeferred(String shortId, Class<B> cls) {
        return DeferredBuilder.getDeferred(shortId, cls, deferred);
    }

    @Override
    public String getIdentification() {
        return identifier;
    }
    
    @Override
    public boolean create(Consumer<SubmodelElementContainerBuilder> func, boolean propagate, String... path) {
        return false;
    }

    @Override
    public <T extends SubmodelElement> boolean iterate(IteratorFunction<T> func, Class<T> cls, String... path) {
        return false;
    }

}
