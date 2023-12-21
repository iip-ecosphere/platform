/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.fakeAas;

import java.util.ArrayList;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties;

/**
 * Fake technical properties.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeTechnicalProperties extends FakeSubmodelElementCollection implements TechnicalProperties {

    /**
     * Fake builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FakeTechnicalPropertiesBuilder extends FakeSubmodelElementCollectionBuilder 
        implements TechnicalPropertiesBuilder {

        /**
         * Creates an instance.
         * 
         * @param parent the parent instance
         */
        FakeTechnicalPropertiesBuilder(FakeSubmodelElementContainerBuilder parent) {
            super(parent, "TechnicalProperties", false, false);
        }

        @Override
        public SubmodelElementCollectionBuilder createMainSectionBuilder(String name, boolean ordered,
            boolean allowDuplicates) {
            return new FakeSubmodelElementCollectionBuilder(this, "MainSection_" + name, ordered, allowDuplicates);
        }

        @Override
        public SubmodelElementCollectionBuilder createSubSectionBuilder(String name, boolean ordered,
            boolean allowDuplicates) {
            return new FakeSubmodelElementCollectionBuilder(this, "SubSection_" + name, ordered, allowDuplicates);
        }
        
        @Override
        protected FakeSubmodelElementCollection createInstance(String idShort) {
            return new FakeTechnicalProperties(idShort);
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the idShort
     */
    protected FakeTechnicalProperties(String idShort) {
        super(idShort);
    }

    
    @Override
    public Iterable<SubmodelElementCollection> mainSections() {
        return stream(SubmodelElementCollection.class)
            .filter(e -> e.getIdShort().startsWith("MainSection_"))
            .collect(Collectors.toList());
    }

    @Override
    public Iterable<SubmodelElementCollection> subSections() {
        return stream(SubmodelElementCollection.class)
            .filter(e -> e.getIdShort().startsWith("SubSection_"))
            .collect(Collectors.toList());
    }

    @Override
    public Iterable<SubmodelElement> sMENotDescribedBySemanticId() {
        return new ArrayList<SubmodelElement>(); // for now
    }

    @Override
    public Iterable<SubmodelElement> arbitrary() {
        return new ArrayList<SubmodelElement>(); // for now
    }

}
