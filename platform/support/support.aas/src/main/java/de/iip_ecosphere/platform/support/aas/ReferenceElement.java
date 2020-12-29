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
 * Defines the interface for a reference element.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ReferenceElement extends SubmodelElement {

    /**
     * Defines the interface for a reference element builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ReferenceElementBuilder extends Builder<ReferenceElement> {

        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public SubmodelElementContainerBuilder getParentBuilder();
        
        /**
         * Defines the value.
         * 
         * @param value the value
         * @return <b>this</b>
         */
        public ReferenceElementBuilder setValue(Reference value);
        
    }

    /**
     * Returns the reference.
     * 
     * @return the reference
     */
    public Reference getValue();

}
