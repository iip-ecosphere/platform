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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Defines the interface of a data element.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DataElement extends SubmodelElement {

    /**
     * Builds a data element.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface DataElementBuilder<T extends DataElement> extends Builder<T> {
        
        // incomplete
        
        /**
         * Sets the semantic ID of the data element in terms of a reference.
         * 
         * @param semanticId the semantic id (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public DataElementBuilder<T> setSemanticId(String semanticId);

    }

}
