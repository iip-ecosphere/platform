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

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Invokable;

/**
 * Implements an abstract invocables creator for the VAB following the naming conventions of 
 * {@link AasOperationsProvider}. Function objects as well as class itself must be serializable for remote deployment.
 * 
 * Although serializable lambda functions appear feasible, we experienced deserialization problems and rely now on
 * explicit functor instances. Failing operation executions throw the occuring (runtime) exception and require catching 
 * them.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAasRestInvocablesCreator implements InvocablesCreator, Serializable {

    private static final long serialVersionUID = -4388430468665656598L;
    
    /**
     * Returns an identifier for the underlying connection, e.g., host + port.
     * 
     * @return the identifier
     */
    protected abstract String getId();
    
    /**
     * Composes a server URL from the given URL {@code suffix}.
     * 
     * @param suffix the URL suffix
     * @return the composed URL
     */
    protected abstract String composeUrl(String suffix);

    /**
     * Defines a generic, serializable operation.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class Operation implements Invokable {

        private String name;
        
        /**
         * Creates an operation instance.
         * 
         * @param name the name of the operation
         */
        protected Operation(String name) {
            this.name = name;
        }

        @Override
        public String getUrl() {
            // https://wiki.basyx.org/en/latest/content/user_documentation/basyx_components/v1/aas-server/
            // features/operation-delegation.html
            // https://wiki.basyx.org/en/latest/content/user_documentation/basyx_components/v2/submodel_repository/
            // features/operation-delegation.html
            return AbstractAasRestInvocablesCreator.this.composeUrl(AasOperationsProvider.PREFIX_SERVICE + name);
        }
        
        @Override
        public String getSubmodelRepositoryUrl() {
            return AbstractAasRestInvocablesCreator.this.getSubmodelRepositoryUrl();
        }
        
        @Override
        public void execute(OperationInvocation invocation) throws IOException {
            if (invocation instanceof BaSyxOperationInvocation) {
                BaSyxOperationInvocation bi = (BaSyxOperationInvocation) invocation;
                try {
                    bi.execute(getSubmodelRepository());
                } catch (ElementDoesNotExistException e) {
                    throw new IOException(e);
                }
            }
        }

    }
    
    /**
     * Returns the submodel repository to delegate calls to.
     * 
     * @return the submodel repository
     */
    protected abstract ConnectedSubmodelRepository getSubmodelRepository();

    /**
     * Returns the submodel repository to delegate calls to.
     * 
     * @return the submodel repository url
     */
    protected abstract String getSubmodelRepositoryUrl();

    @Override
    public Invokable createGetter(String name) {
        return null; // not available in v3
    }

    @Override
    public Invokable createSetter(String name) {
        return null; // not available in v3
    }

    @Override
    public Invokable createInvocable(String name) {
        return new Operation(name);
    }

}
