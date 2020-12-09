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

import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.SubModel;
import de.iip_ecosphere.platform.support.aas.SubModelElement;

/**
 * Basic sub-model implementation.
 * 
 * @param <S> the BaSyx type implementing the sub-model
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSubModel<S extends ISubModel> implements SubModel {

    private S subModel;
    private List<Operation> operations = new ArrayList<>();
    private List<DataElement> dataElements = new ArrayList<>();
    private List<SubModelElement> subModelElements = new ArrayList<>(); // just redundant?

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param subModel the sub model instance
     */
    protected AbstractSubModel(S subModel) {
        this.subModel = subModel;
    }
    
    /**
     * Returns the submodel instance.
     * 
     * @return the submodel instance
     */
    S getSubModel() {
        return subModel;
    }
    
    /**
     * Registers an operation.
     * 
     * @param operation the operation
     * @return {@code operation}
     */
    BaSyxOperation register(BaSyxOperation operation) {
        operations.add(operation);
        subModelElements.add(operation);
        return operation;
    }
    
    /**
     * Registers a property.
     * 
     * @param property the property
     * @return {@code property}
     */
    BaSyxProperty register(BaSyxProperty property) {
        dataElements.add(property);
        subModelElements.add(property);
        return property;
    }

    @Override
    public String getIdShort() {
        return subModel.getIdShort();
    }

    @Override
    public Iterable<SubModelElement> submodelElements() {
        return subModelElements;
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
        return subModelElements.size();
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
    public Property getProperty(String idShort) {
        // looping may not be efficient, let's see
        Property found = null;
        for (DataElement elt : dataElements) {
            if (elt instanceof Property && elt.getIdShort().equals(idShort)) {
                found = (Property) elt;
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

}
