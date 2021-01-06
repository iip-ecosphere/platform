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

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import test.de.iip_ecosphere.platform.support.fakeAas.FakeAas.FakeAasBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * A fake (inefficient) submodel.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeSubmodel extends FakeElement implements Submodel {

    private FakeAas parent;
    private Map<String, SubmodelElement> elements = new HashMap<>();
    
    /**
     * A fake sub-model builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeSubmodelBuilder extends FakeSubmodelElementContainerBuilder implements SubmodelBuilder {

        private FakeAasBuilder parent;
        private FakeSubmodel instance;
        private boolean isNew = true;

        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        FakeSubmodelBuilder(FakeAasBuilder parent, String idShort) {
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
            this.instance = new FakeSubmodel(idShort);
            this.instance.parent = null != parent ? parent.getInstance() : null;
        }

        /**
         * Creates a builder for an existing instance.
         * 
         * @param parent the parent
         * @param instance the instance
         */
        FakeSubmodelBuilder(FakeAasBuilder parent, FakeSubmodel instance) {
            this.parent = parent;
            this.instance = instance;
            this.isNew = false;
        }
        
        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
            boolean allowDuplicates) {
            return new FakeSubmodelElementCollection.FakeSubmodelElementCollectionBuilder(this, idShort, ordered, 
                allowDuplicates);
        }

        @Override
        public Reference createReference() {
            return new FakeReference();
        }

        @Override
        public Submodel build() {
            return parent.register(instance);
        }

        @Override
        FakeOperation register(FakeOperation operation) {
            instance.elements.put(operation.getIdShort(), operation);
            return operation;
        }

        @Override
        FakeProperty register(FakeProperty property) {
            instance.elements.put(property.getIdShort(), property);
            return property;
        }

        @Override
        FakeReferenceElement register(FakeReferenceElement reference) {
            instance.elements.put(reference.getIdShort(), reference);
            return reference;
        }

        @Override
        FakeSubmodelElementCollection register(FakeSubmodelElementCollection collection) {
            instance.elements.put(collection.getIdShort(), collection);
            return collection;
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
        
    }
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    protected FakeSubmodel(String idShort) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitSubmodel(this);
        for (SubmodelElement elt : elements.values()) {
            elt.accept(visitor);
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
        return null;
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
    public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
        boolean allowDuplicates) {
        FakeSubmodelElementContainerBuilder secb = new FakeSubmodel.FakeSubmodelBuilder(
            new FakeAasBuilder(parent), this);
        return new FakeSubmodelElementCollection.FakeSubmodelElementCollectionBuilder(
            secb, idShort, ordered, allowDuplicates);
    }

    @Override
    public Reference createReference() {
        return new FakeReference();
    }

    @Override
    public void delete(SubmodelElement elt) {
        elements.remove(elt.getIdShort());
    }

}
