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

import de.iip_ecosphere.platform.support.aas.SubModel.SubModelBuilder;

/**
 * Represents an AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Aas extends Element, Identifiable, HasDataSpecification {
    
    /**
     * Used to build an AAS.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface AasBuilder {

        /**
         * Creates a builder for a contained sub-model.
         * 
         * @param idShort the short id of the sub-model
         * @return the builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public SubModelBuilder createSubModelBuilder(String idShort);

        /**
         * Builds the instance.
         * 
         * @return the Aas instance
         */
        public Aas build();

    }
    
    /**
     * Returns the sub-models.
     * 
     * @return the sub-models
     */
    public Iterable<? extends SubModel> submodels();
    
    /**
     * Returns the number of sub-models.
     * 
     * @return the number of sub-models
     */
    public int getSubmodelCount();
    
    /**
     * Returns the sub-model with the specified name.
     * 
     * @param idShort the short name to search for
     * @return the sub-model or <b>null</b> if there was none
     */
    public SubModel getSubModel(String idShort);
    
}
