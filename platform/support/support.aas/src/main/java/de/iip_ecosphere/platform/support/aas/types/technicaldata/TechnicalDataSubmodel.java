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

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodel;
import de.iip_ecosphere.platform.support.aas.types.common.Utils;

/**
 * Support for <a href="https://industrialdigitaltwin.org/wp-content/uploads/2022/10/
 * IDTA-02003-1-2_Submodel_TechnicalData.pdf">IDTA 02003-1-2 Generic Frame for Technical Data for
 * Industrial Equipment in Manufacturing</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TechnicalDataSubmodel extends DelegatingSubmodel {
    
    public static final String ID_SHORT = "TechnicalData";

    /**
     * Creates an instance.
     * 
     * @param aas the parent AAS
     */
    public TechnicalDataSubmodel(Aas aas) {
        super(aas.getSubmodel(ID_SHORT));
    }

    /**
     * Creates an instance.
     * 
     * @param delegate the submodel delegate
     */
    public TechnicalDataSubmodel(Submodel delegate) {
        super(delegate);
    }

    /**
     * Returns the technical properties classifications.
     * 
     * @return the technical properties, may be <b>null</b> if not created by a builder before
     */
    public TechnicalProperties getTechnicalProperties() {
        return Utils.wrapSubmodelElementCollection(this, TechnicalProperties.ID_SHORT, 
            s -> new TechnicalProperties(s));
    }

    /**
     * Returns the product classifications.
     * 
     * @return the product classifications, may be <b>null</b> if not created by a builder before
     */
    public ProductClassifications getProductClassifications() {
        return Utils.wrapSubmodelElementCollection(this, ProductClassifications.ID_SHORT, 
            s -> new ProductClassifications(s));
    }
    
    /**
     * Returns the general information.
     * 
     * @return the general information, may be <b>null</b> if not created by a builder before
     */
    public GeneralInformation getGeneralInformation()  {
        return Utils.wrapSubmodelElementCollection(this, GeneralInformation.ID_SHORT, 
            s -> new GeneralInformation(s));
    }

    /**
     * Returns the further information.
     * 
     * @return the further information, may be <b>null</b> if not created by a builder before
     */
    public FurtherInformation getFurtherInformation() {
        return Utils.wrapSubmodelElementCollection(this, FurtherInformation.ID_SHORT, 
            s -> new FurtherInformation(s));
    }
    
}
