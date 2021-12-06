/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.types.technicaldata;

import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Defines the interface to the technical properties.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TechnicalProperties extends SubmodelElementCollection {

    /**
     * The general information builder. There are no specific sub-builders for SmePropertyNotDescribedBySemanticId
     * and arbitrary elements as these are just projections of the elements.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TechnicalPropertiesBuilder extends SubmodelElementCollectionBuilder {

        /**
         * Creates a main section builder.
         * 
         * @param name the name of the section that may be prefixed by the underlying implementation
         * @param ordered whether the collection is ordered
         * @param allowDuplicates whether the collection allows duplicates
         * @return the main section builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public SubmodelElementCollectionBuilder createMainSectionBuilder(String name, boolean ordered, 
            boolean allowDuplicates);

        /**
         * Creates a sub section builder.
         * 
         * @param name the name of the section that may be prefixed by the underlying implementation
         * @param ordered whether the collection is ordered
         * @param allowDuplicates whether the collection allows duplicates
         * @return the sub section builder
         * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty
         */
        public SubmodelElementCollectionBuilder createSubSectionBuilder(String name, boolean ordered, 
            boolean allowDuplicates);
        
    }
    
    /**
     * Returns the main sections as iterable.
     * 
     * @return the main sections
     */
    public Iterable<SubmodelElementCollection> mainSections();

    /**
     * Returns the sub sections as iterable.
     * 
     * @return the sub sections
     */
    public Iterable<SubmodelElementCollection> subSections();

    /**
     * Returns the submodel elements that are not described by a semantic id of a common classification system.
     * 
     * @return the submodel elements
     */
    public Iterable<SubmodelElement> sMENotDescribedBySemanticId();

    /**
     * Returns the submodel elements that have arbitrary semanticId but are not defined in a classification system.
     * 
     * @return the submodel elements
     */
    public Iterable<SubmodelElement> arbitrary();
    
}
