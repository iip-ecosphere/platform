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

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.ProductClassifications.ProductClassificationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalProperties.TechnicalPropertiesBuilder;

/**
 * Defines the interface to the technical data submodel that can be used as-is.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TechnicalDataSubmodel extends Submodel {
    
    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TechnicalDataSubmodelBuilder extends SubmodelBuilder {
        
        /**
         * Creates a technical properties builder.
         * 
         * @return the builder
         */
        public TechnicalPropertiesBuilder createTechnicalPropertiesBuilder();

        /**
         * Creates a product classifications builder.
         * 
         * @return the builder
         */
        public ProductClassificationsBuilder createProductClassificationsBuilder();
        
        /**
         * Creates a general information builder.
         * 
         * @param manufacturerName the manufacturer name
         * @param manufacturerProductDesignation the manufacturer product designation
         * @param manufacturerPartNumber the manufacturer part number
         * @param manufacturerOrderCode the manufacturer order code
         * @return the builder
         */
        public GeneralInformationBuilder createGeneralInformationBuilder(String manufacturerName, 
            de.iip_ecosphere.platform.support.aas.LangString manufacturerProductDesignation, 
            String manufacturerPartNumber, String manufacturerOrderCode);

        /**
         * Creates a further information builder.
         * 
         * @param validDate denotes a date on which the data specified in the submodel was valid from for the 
         *     associated asset
         * @return the builder
         */
        public FurtherInformationBuilder createFurtherInformationBuilder(XMLGregorianCalendar validDate);

    }
    
    /**
     * Returns the technical properties classifications.
     * 
     * @return the technical properties, may be <b>null</b> if not created by a builder before
     */
    public TechnicalProperties getTechnicalProperties();

    /**
     * Returns the product classifications.
     * 
     * @return the product classifications, may be <b>null</b> if not created by a builder before
     */
    public ProductClassifications getProductClassifications();
    
    /**
     * Returns the general information.
     * 
     * @return the general information, may be <b>null</b> if not created by a builder before
     */
    public GeneralInformation getGeneralInformation();

    /**
     * Returns the further information.
     * 
     * @return the further information, may be <b>null</b> if not created by a builder before
     */
    public FurtherInformation getFurtherInformation();
    
}
