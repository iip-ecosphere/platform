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

package de.iip_ecosphere.platform.support.aas.basyx.types.technicaldata;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.technicalproperties.TechnicalProperties;

import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxSubmodelElementContainerBuilder;

/**
 * Wrapper for the BaSyx technical properties class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTechnicalProperties extends BaSyxSubmodelElementCollection implements 
    de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties {

    public static final String ID_SHORT = TechnicalProperties.IDSHORT;
    public static final String MAIN_SECTION_PREFIX = TechnicalProperties.MAINSECTIONPREFIX;
    public static final String SUB_SECTION_PREFIX = TechnicalProperties.SUBSECTIONPREFIX;
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxTechnicalPropertiesBuilder extends BaSyxSubmodelElementCollectionBuilder 
        implements TechnicalPropertiesBuilder {

        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        BaSyxTechnicalPropertiesBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder) {
            super(parentBuilder, ID_SHORT, 
                () -> new BaSyxTechnicalProperties(), 
                () -> new TechnicalProperties()); 
        }
        
        /**
         * Creates an instance from an existing BaSyx instance.
         * 
         * @param parentBuilder the parent builder
         * @param instance the BaSyx instance
         */
        BaSyxTechnicalPropertiesBuilder(BaSyxSubmodelElementContainerBuilder<?> parentBuilder,
            BaSyxTechnicalProperties instance) {
            super(parentBuilder, instance);
        }
        
        @Override
        public SubmodelElementCollectionBuilder createMainSectionBuilder(String name, boolean ordered, 
            boolean allowDuplicates) {
            return super.createSubmodelElementCollectionBuilder(MAIN_SECTION_PREFIX + name, 
                ordered, allowDuplicates);
        }
        
        @Override
        public SubmodelElementCollectionBuilder createSubSectionBuilder(String name, boolean ordered, 
            boolean allowDuplicates) {
            return super.createSubmodelElementCollectionBuilder(SUB_SECTION_PREFIX + name, 
                ordered, allowDuplicates);
        }
        
        // no register, build needed as mapped to non-specific types/underlying operations

    }

    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxTechnicalProperties() {
        super();
    }

    /**
     * Creates an instance and sets the BaSyx instance directly.
     * 
     * @param collection the collection instance
     */
    BaSyxTechnicalProperties(org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections
        .generalinformation.GeneralInformation collection) {
        super(collection);
    }

    @Override
    public TechnicalProperties getSubmodelElement() {
        return (TechnicalProperties) super.getSubmodelElement();
    }
    
    @Override
    public Iterable<SubmodelElementCollection> mainSections() {
        return getSubmodelElementCollections(c -> c.getIdShort().startsWith(MAIN_SECTION_PREFIX));
    }

    @Override
    public Iterable<SubmodelElementCollection> subSections() {
        return getSubmodelElementCollections(c -> c.getIdShort().startsWith(SUB_SECTION_PREFIX));
    }

    @Override
    public Iterable<SubmodelElement> sMENotDescribedBySemanticId() {
        // performance-wise not ideal, but consistent with underlying implementation
        Set<String> ids = getSubmodelElement().getSMENotDescribedBySemanticId()
            .stream()
            .map(e -> e.getIdShort())
            .collect(Collectors.toSet());
        
        return getElements(e -> ids.contains(e.getIdShort()));
    }

    @Override
    public Iterable<SubmodelElement> arbitrary() {
        // performance-wise not ideal, but consistent with underlying implementation
        Set<String> ids = getSubmodelElement().getArbitrary()
            .stream()
            .map(e -> e.getIdShort())
            .collect(Collectors.toSet());
        
        return getElements(e -> ids.contains(e.getIdShort()));
    }

}
