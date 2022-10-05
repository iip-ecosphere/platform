/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.ivml;

import java.util.concurrent.ExecutionException;

import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Provides access to decision variables.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DecisionVariableProvider {
    
    /**
     * Returns a decision variable for a given variable name.
     * 
     * @param varName the variable name
     * @return the associated decision variable, may be <b>null</b> for node
     * @throws ExecutionException if retrieving the node fails
     */
    public IDecisionVariable getVariable(String varName) throws ExecutionException;
    
}