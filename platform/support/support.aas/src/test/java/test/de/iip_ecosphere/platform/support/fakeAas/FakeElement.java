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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.aas.Element;

/**
 * Implements a basis fake element.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class FakeElement implements Element {

    private String idShort;
    private String semanticId;
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    protected FakeElement(String idShort) {
        this.idShort = idShort;
    }
    
    @Override
    public String getIdShort() {
        return idShort;
    }
    
    @Override
    public void update() {
        // not needed here
    }

    /**
     * Returns the semantic id of the element.
     * 
     * @param stripPrefix if the plain semantic id or the prefix shall also be emitted
     * @return the semantic id in textual format, e.g., with/out identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     */
    public String getSemanticId(boolean stripPrefix) {
        return semanticId;
    }

    /**
     * Changes the semantic id of the element.
     * 
     * @param semanticId the semantic id in textual format with identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     */
    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

}
