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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.basyx.submodel.metamodel.api.ISubModel;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
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
public abstract class AbstractSubmodel<S extends ISubModel> implements Submodel, SubmodelElementsRegistrar {

    private S submodel;
    private List<Operation> operations = new ArrayList<>();
    private List<DataElement> dataElements = new ArrayList<>();
    private List<SubmodelElement> submodelElements = new ArrayList<>(); // just redundant?

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param submodel the sub model instance
     */
    protected AbstractSubmodel(S submodel) {
        this.submodel = submodel;
    }
    
    /**
     * Returns the submodel instance.
     * 
     * @return the submodel instance
     */
    S getSubModel() {
        return submodel;
    }
    
    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    public BaSyxOperation register(BaSyxOperation operation) {
        operations.add(operation);
        submodelElements.add(operation);
        return operation;
    }
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    public BaSyxProperty register(BaSyxProperty property) {
        dataElements.add(property);
        submodelElements.add(property);
        return property;
    }
    
    /**
     * Registers a reference element.
     * 
     * @param reference the reference element
     * @return {@code reference}
     */
    public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
        submodelElements.add(reference);
        return reference;
    }

    /**
     * Registers a sub-model element collection.
     * 
     * @param collection the element collection
     * @return {@code collection}
     */
    public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        submodelElements.add(collection);
        return collection;
    }

    @Override
    public String getIdShort() {
        return submodel.getIdShort();
    }

    @Override
    public Iterable<SubmodelElement> submodelElements() {
        return submodelElements;
    }

    @Override
    public Iterable<DataElement> dataElements() {
        return dataElements;
    }

    @Override
    public Iterable<Operation> operations() {
        return operations;
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
        // looping may not be efficient, let's see
        Property found = null;
        for (DataElement elt : dataElements) {
            if (elt.getIdShort().equals(idShort)) {
                found = (Property) elt;
                break;
            }
        }
        return found;
    }

    @Override
    public Property getProperty(String idShort) {
        Property result = null;
        DataElement tmp = getDataElement(idShort);
        if (tmp instanceof Property) {
            result = (Property) tmp;
        }
        return result;
    }
    
    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        // looping may not be efficient, let's see
        ReferenceElement found = null;
        for (SubmodelElement elt : submodelElements) {
            if (elt instanceof ReferenceElement && elt.getIdShort().equals(idShort)) {
                found = (ReferenceElement) elt;
                break;
            }
        }
        return found;
    }
    
    @Override
    public Operation getOperation(String idShort, int numArgs) {
        Operation found = null;
        for (Operation op : operations) {
            if (op.getIdShort().equals(idShort) && numArgs == op.getArgsCount()) {
                found = op;
                break;
            }
        }
        return found;
    }

    @Override
    public Operation getOperation(String idShort, int inArgs, int outArgs, int inOutArgs) {
        Operation found = null;
        for (Operation op : operations) {
            if (op.getIdShort().equals(idShort) && inArgs == op.getInArgsCount() && outArgs == op.getOutArgsCount() 
                && inOutArgs == op.getInOutArgsCount()) {
                found = op;
                break;
            }
        }
        return found;
    }

    @Override
    public SubmodelElement getSubmodelElement(String idShort) {
        // looping may not be efficient, let's see
        SubmodelElement found = null;
        for (SubmodelElement se : submodelElements) {
            if (se.getIdShort().equals(idShort)) {
                found = se;
                break;
            }
        }
        return found;
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
        for (DataElement de : dataElements) {
            de.accept(visitor);
        }
        for (Operation op : operations) {
            op.accept(visitor);
        }
        for (SubmodelElement se : submodelElements) {
            // remaining elements
            if (!(se instanceof DataElement && se instanceof Operation)) {
                se.accept(visitor);
            }
        }
        visitor.endSubmodel(this);
    }

}
