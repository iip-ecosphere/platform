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

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties.TechnicalPropertiesBuilder;
import test.de.iip_ecosphere.platform.support.fakeAas.FakeAas.FakeAasBuilder;

/**
 * Fake technical submodel.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeTechnicalDataSubmodel extends FakeSubmodel implements TechnicalDataSubmodel {

    private FakeTechnicalProperties technicalProperties;
    private ProductClassifications productClassifications;
    private GeneralInformation generalInformation;
    private FurtherInformation furtherInformation;
    
    /**
     * Fake builder for the technical data submodel.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class FakeTechnicalDataSubmodelBuilder extends FakeSubmodelBuilder implements TechnicalDataSubmodelBuilder {
        
        private FakeTechnicalDataSubmodel instance;
        
        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         * @param identifier the submodel identifier
         */
        protected FakeTechnicalDataSubmodelBuilder(FakeAasBuilder parent, String identifier) {
            super(parent, "TechnicalData", identifier);
        }

        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         * @param instance the existing instance
         */
        FakeTechnicalDataSubmodelBuilder(FakeAasBuilder parent, FakeSubmodel instance) {
            super(parent, instance);
        }

        @Override
        protected FakeSubmodel createInstance(String idShort, String identifier) {
            instance = new FakeTechnicalDataSubmodel(idShort, identifier);
            return instance;
        }

        @Override
        FakeSubmodelElementCollection register(FakeSubmodelElementCollection collection)  {
            if (collection instanceof FakeTechnicalProperties) {
                instance.technicalProperties = (FakeTechnicalProperties) collection;
            } else if (collection instanceof FakeProductClassifications) {
                instance.productClassifications = (FakeProductClassifications) collection;
            } else if (collection instanceof FakeGeneralInformation) {
                instance.generalInformation = (FakeGeneralInformation) collection;
            } else if (collection instanceof FakeFurtherInformation) {
                instance.furtherInformation = (FakeFurtherInformation) collection;
            }
            return super.register(collection);
        }

        @Override
        public TechnicalPropertiesBuilder createTechnicalPropertiesBuilder() {
            return new FakeTechnicalProperties.FakeTechnicalPropertiesBuilder(this);
        }

        @Override
        public ProductClassificationsBuilder createProductClassificationsBuilder() {
            return new FakeProductClassifications.FakeProductClassificationsBuilder(this);
        }

        @Override
        public GeneralInformationBuilder createGeneralInformationBuilder(String manufacturerName,
                LangString manufacturerProductDesignation, String manufacturerPartNumber,
                String manufacturerOrderCode) {
            return new FakeGeneralInformation.FakeGeneralInformationBuilder(this, manufacturerName,
                manufacturerProductDesignation, manufacturerPartNumber,
                manufacturerOrderCode);
        }

        @Override
        public FurtherInformationBuilder createFurtherInformationBuilder(XMLGregorianCalendar validDate) {
            return new FakeFurtherInformation.FakeFurtherInformationBuilder(this);
        }
        
    }
    
    /**
     * Creates an instance.
     * 
     * @param idShort the idshort
     * @param identifier the identifier
     */
    protected FakeTechnicalDataSubmodel(String idShort, String identifier) {
        super(idShort, identifier);
    }

    @Override
    public TechnicalProperties getTechnicalProperties() {
        return technicalProperties;
    }

    @Override
    public ProductClassifications getProductClassifications() {
        return productClassifications;
    }

    @Override
    public GeneralInformation getGeneralInformation() {
        return generalInformation;
    }

    @Override
    public FurtherInformation getFurtherInformation() {
        return furtherInformation;
    }

}
