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

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;

import de.iip_ecosphere.platform.support.aas.Invokable.OperationInvocation;

/**
 * Represents an implementation-specific submodel operation invocation.
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxOperationInvocation implements OperationInvocation {
    
    private String submodelId;
    private String idShort;
    private OperationVariable[] args;
    private OperationVariable[] result;
    
    /**
     * Creates an invocation object.
     * 
     * @param submodelId the submodel id
     * @param idShort the id of the operation
     * @param args the arguments
     */
    BaSyxOperationInvocation(String submodelId, String idShort, OperationVariable[] args) {
        this.submodelId = submodelId;
        this.idShort = idShort;
        this.args = args;
    }

    /**
     * Executes the operation on {@code repo}.
     * 
     * @param repo the repository to execute the operation on
     * @throws ElementDoesNotExistException if the operation/submodel cannot be found
     */
    void execute(ConnectedSubmodelRepository repo) throws ElementDoesNotExistException {
        result = repo.invokeOperation(submodelId, idShort, args);
    }

    /**
     * Returns the result of the execution if no exception was thrown.
     * 
     * @return the result
     */
    public OperationVariable[] getResult() {
        return result;
    }

}
