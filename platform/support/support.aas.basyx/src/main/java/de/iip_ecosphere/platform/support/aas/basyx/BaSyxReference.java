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

import org.eclipse.basyx.submodel.metamodel.api.reference.IReference;

import de.iip_ecosphere.platform.support.aas.Reference;

/**
 * Implements a BaSyx reference value wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxReference implements Reference {

    private IReference reference;
    
    /**
     * Creates an instance.
     * 
     * @param reference the BaSyx reference
     */
    public BaSyxReference(IReference reference) {
        this.reference = reference;
    }
    
    /**
     * Returns the BaSyx reference.
     * 
     * @return the BaSyx reference
     */
    IReference getReference() {
        return reference;
    }
    
    @Override
    public String toString() {
        return null != reference ? reference.toString() : "<ref: null>";
    }

    @Override
    public boolean hasReference() {
        return reference != null;
    }
    
    // does not return a value, may fail in tests otherwise
    
    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object instanceof BaSyxReference) {
            BaSyxReference ref = (BaSyxReference) object;
            if (null == reference) {
                result = ref.reference == null;
            } else {
                result = reference.equals(ref.reference);
            }
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        return null == reference ? 0 : reference.hashCode(); 
    }
    
}
