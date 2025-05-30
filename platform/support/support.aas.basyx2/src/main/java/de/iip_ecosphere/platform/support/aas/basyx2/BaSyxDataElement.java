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

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;

/**
 * Wraps a BaSyx data element. Shall be created by respective builder methods.
 * 
 * @param <D> the BaSyx data element type 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDataElement<D extends 
    org.eclipse.digitaltwin.aas4j.v3.model.DataElement> extends BaSyxSubmodelElement 
    implements DataElement {
    
    private D dataElement;
    
    /**
     * Creates a data element from a given BaSyx instance.
     * 
     * @param dataElement the data element
     */
    protected BaSyxDataElement(D dataElement) {
        this.dataElement = dataElement;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitDataElement(this);
    }

    @Override
    SubmodelElement getSubmodelElement() {
        return dataElement;
    }
    
    /**
     * Returns the BaSyx instance.
     * 
     * @return the instance
     */
    public D getDataElement() {
        return dataElement;
    }

}
