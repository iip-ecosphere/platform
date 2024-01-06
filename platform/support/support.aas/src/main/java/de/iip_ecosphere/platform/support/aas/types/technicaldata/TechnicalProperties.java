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

import static de.iip_ecosphere.platform.support.aas.IdentifierType.iri;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import java.util.stream.Collectors;

import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollection;

/**
 * Defines the interface to the technical properties.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TechnicalProperties extends DelegatingSubmodelElementCollection {
    
    public static final String ID_SHORT = "TechnicalProperties";
    public static final String IRI_SEMANTIC_NOT_AVAILABLE = iri("https://admin-shell.io/SemanticIdNotAvailable/1/1");
    public static final String IRI_MAIN_SECTION = iri("https://admin-shell.io/ZVEI/TechnicalData/MainSection/1/1");
    public static final String IRI_SUB_SECTION = iri("https://admin-shell.io/ZVEI/TechnicalData/SubSection/1/1");
    
    /**
     * Creates an instance.
     * 
     * @param sme the delegate submodel element collection
     */
    TechnicalProperties(SubmodelElementCollection sme) {
        super(sme);
    }
    
    /**
     * Returns the main sections as iterable.
     * 
     * @return the main sections
     */
    public Iterable<SubmodelElementCollection> mainSections() {
        return stream(elements(), SubmodelElementCollection.class, 
            e -> IRI_MAIN_SECTION.equals(e.getSemanticId()))
            .collect(Collectors.toList());
    }

    /**
     * Returns the sub sections as iterable.
     * 
     * @return the sub sections
     */
    public Iterable<SubmodelElementCollection> subSections() {
        return stream(elements(), SubmodelElementCollection.class, 
            e -> IRI_SUB_SECTION.equals(e.getSemanticId()))
            .collect(Collectors.toList());
    }

    /**
     * Returns the submodel elements that are not described by a semantic id of a common classification system.
     * 
     * @return the submodel elements
     */
    public Iterable<SubmodelElement> sMENotDescribedBySemanticId() {
        return stream(elements(), SubmodelElementCollection.class, 
            e -> e.getSemanticId() == null)
            .collect(Collectors.toList());
    }

    /**
     * Returns the submodel elements that have arbitrary semanticId but are not defined in a classification system.
     * 
     * @return the submodel elements
     */
    public Iterable<SubmodelElement> arbitrary() {
        return stream(elements(), SubmodelElementCollection.class, 
            e -> !IRI_MAIN_SECTION.equals(e.getSemanticId()) && !IRI_SUB_SECTION.equals(e.getSemanticId()))
            .collect(Collectors.toList());
    }
    
}
