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
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.vab.exception.provider.ResourceNotFoundException;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.DeferredBuilder;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxElementTranslator.SubmodelElementsRegistrar;

/**
 * Basic sub-model implementation.
 * 
 * @param <S> the BaSyx type implementing the sub-model
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSubmodel<S extends ISubmodel> implements Submodel, SubmodelElementsRegistrar {

    private S submodel;
    private Map<String, Operation> operations = new HashMap<>();
    private Map<String, DataElement> dataElements = new HashMap<>(); // TODO now partly values in BaSyx
    private Map<String, Property> properties = new HashMap<>();
    private Map<String, SubmodelElement> submodelElements = new HashMap<>();
    private Map<String, Builder<?>> deferred;

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param submodel the sub model instance
     */
    protected AbstractSubmodel(S submodel) {
        this.submodel = submodel;
    }
    
    /**
     * Returns the sub-model instance.
     * 
     * @return the sub-model instance
     */
    S getSubmodel() {
        return submodel;
    }
    
    /**
     * Emits a warning.
     * 
     * @param msg the message to be emitted
     */
    private void warn(String msg) {
        LoggerFactory.getLogger(getClass()).warn(msg);
    }
    
    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    public BaSyxOperation register(BaSyxOperation operation) {
        String id = operation.getIdShort();
        if (operations.containsKey(id) || submodelElements.containsKey(id)) {
            warn("There is already an operation/element with short id '" + id + "'. "
                + "The property/element may be redefined.");
        }
        operations.put(id, operation);
        submodelElements.put(id, operation);
        return operation;
    }
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    public BaSyxProperty register(BaSyxProperty property) {
        String id = property.getIdShort();
        if (properties.containsKey(id) || submodelElements.containsKey(id)) {
            warn("There is already a property/element with short id '" + id + "'. "
                + "The property/element may be redefined.");
        }
        properties.put(id, property);
        submodelElements.put(id, property);
        return property;
    }
    
    /**
     * Registers a reference element.
     * 
     * @param reference the reference element
     * @return {@code reference}
     */
    public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
        String id = reference.getIdShort();
        if (submodelElements.containsKey(id)) {
            warn("There is already a reference/element with short id '" + id + "'. "
                + "The reference/element may be redefined.");
        }
        submodelElements.put(id, reference);
        return reference;
    }

    /**
     * Registers a sub-model element collection.
     * 
     * @param collection the element collection
     * @return {@code collection}
     */
    public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        String id = collection.getIdShort();
        if (submodelElements.containsKey(id)) {
            warn("There is already a collection/element with short id '" + id + "'. "
                + "The collection/element may be redefined.");
        }
        submodelElements.put(id, collection);
        return collection;
    }

    @Override
    public String getIdShort() {
        return submodel.getIdShort();
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return submodelElements.values();
    }

    @Override
    public Iterable<Property> properties() {
        return properties.values();
    }

    @Override
    public Iterable<DataElement> dataElements() {
        return dataElements.values();
    }

    @Override
    public Iterable<Operation> operations() {
        return operations.values();
    }

    @Override
    public int getSubmodelElementsCount() {
        return submodelElements.size();
    }

    @Override
    public int getDataElementsCount() {
        return dataElements.size();
    }

    @Override
    public int getOperationsCount() {
        return operations.size();
    }
    
    @Override
    public DataElement getDataElement(String idShort) {
        return dataElements.get(idShort);
    }

    @Override
    public Property getProperty(String idShort) {
        return properties.get(idShort);
    }

    @Override
    public int getPropertiesCount() {
        return properties.size();
    }
    
    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        // looping may not be efficient, let's see
        ReferenceElement found = null;
        try {
            SubmodelElement elt = submodelElements.get(idShort);
            if (elt instanceof ReferenceElement) {
                found = (ReferenceElement) elt;
            }
        } catch (ResourceNotFoundException e) {
        }
        return found;
    }
    
    @Override
    public Operation getOperation(String idShort) {
        return operations.get(idShort);
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        return submodelElements.get(idShort);
    }
    
    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        SubmodelElementCollection result = null;
        SubmodelElement tmp = getSubmodelElement(idShort);
        if (tmp instanceof SubmodelElementCollection) {
            result = (SubmodelElementCollection) tmp;
        }
        return result;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitSubmodel(this);
        for (DataElement de : dataElements.values()) {
            de.accept(visitor);
        }
        for (Operation op : operations.values()) {
            op.accept(visitor);
        }
        for (SubmodelElement se : submodelElements.values()) {
            // remaining elements, don't iterate over them again
            if (!(se instanceof DataElement || se instanceof Operation)) {
                se.accept(visitor);
            }
        }
        visitor.endSubmodel(this);
    }

    @Override
    public Reference createReference() {
        return new BaSyxReference(getSubmodel().getReference());
    }

    @Override
    public void delete(SubmodelElement elt) {
        try {
            if (elt instanceof Property) {
                properties.remove(elt.getIdShort());
            } else if (elt instanceof DataElement) {
                dataElements.remove(elt.getIdShort());
            } else if (elt instanceof Operation) {
                operations.remove(elt.getIdShort());
            }
            submodelElements.remove(elt.getIdShort());
            submodel.deleteSubmodelElement(elt.getIdShort());
        } catch (ResourceNotFoundException e) {
        }
    }

    /**
     * Returns an AAS sub-model URI according to the BaSyx naming schema. [public for testing, debugging]
     * 
     * @param server the server address
     * @param aas the AAS
     * @param submodel the sub-model
     * @return the endpoint URI
     */
    public static String getSubmodelEndpoint(ServerAddress server, Aas aas, Submodel submodel) {
        return AbstractAas.getAasEndpoint(server, aas) 
            + "/submodels/" + Tools.idToUrlPath(submodel.getIdShort()) + "/submodel";
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

    /**
     * Calls {@link Builder#build()} on all deferred builders.
     * 
     * @see #defer(String, Builder)
     */
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

}
