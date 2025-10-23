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

package de.iip_ecosphere.platform.configuration;

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.OperationCompletedListener;

/**
 * Records AAS changes to be applied, usually based on configuration changes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasChanges {
    
    private transient List<AasChange> aasChanges = new ArrayList<>();

    /**
     * Called during AAS setup of the configuration component. Must be called before 
     * {@link #bindOperations(ProtocolServerBuilder)}
     * 
     * @param smBuilder the submodel builder where to map the configuration into
     * @param iCreator the invocables creator
     * @param completedListener to be called when AAS operations are completed
     */
    public void setup(SubmodelBuilder smBuilder, InvocablesCreator iCreator, 
        OperationCompletedListener completedListener) {
    }
    
    /**
     * Binds the AAS operations. Must be called after 
     * {@link #setup(SubmodelBuilder, InvocablesCreator, OperationCompletedListener)}.
     * 
     * @param sBuilder the server builder
     */
    public void bindOperations(ProtocolServerBuilder sBuilder) {
    }
    
    /**
     * Clears all remaining AAS changes.
     */
    public void clearAasChanges() {
        aasChanges.clear();
    }

    /**
     * Clears all remaining AAS changes.
     * 
     * @return the cleared AAS changes
     */
    public Iterable<AasChange> getAndClearAasChanges() {
        List<AasChange> result = new ArrayList<>(aasChanges);
        aasChanges.clear();
        return result;
    }

    /**
     * Adds an AAS change.
     * 
     * @param change the change, ignored if <b>null</b>
     */
    public void addAasChange(AasChange change) {
        if (null != change) {
            aasChanges.add(change);
        }
    }

}
