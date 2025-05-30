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

import de.iip_ecosphere.platform.support.aas.Reference;

/**
 * Implements a BaSyx reference value wrapper.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxReference implements Reference {

    private org.eclipse.digitaltwin.aas4j.v3.model.Reference reference;

    /**
     * Creates an instance.
     * 
     * @param reference the BaSyx reference
     */
    public BaSyxReference(org.eclipse.digitaltwin.aas4j.v3.model.Reference reference) {
        this.reference = reference;
    }

    /**
     * Create an model reference pointing to {@code target}.
     * 
     * @param target the target
     * @return the model reference
     */
    static BaSyxReference createModelReference(org.eclipse.digitaltwin.aas4j.v3.model.Referable target) {
        return new BaSyxReference(Tools.createModelReference(target));
    }
    

    /**
     * Returns the BaSyx reference.
     * 
     * @return the BaSyx reference
     */
    org.eclipse.digitaltwin.aas4j.v3.model.Reference getReference() {
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
