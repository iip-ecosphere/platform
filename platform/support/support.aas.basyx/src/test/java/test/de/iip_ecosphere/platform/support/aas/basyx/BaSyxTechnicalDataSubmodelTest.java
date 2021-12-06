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

package test.de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.furtherinformation.FurtherInformation;
import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.generalinformation.GeneralInformation;
import org.eclipse.basyx.submodel.types.technicaldata.submodelelementcollections.technicalproperties.TechnicalProperties;

import test.de.iip_ecosphere.platform.support.aas.TechnicalDataSubmodelTest;

/**
 * A test for the wrapped technical data submodel.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTechnicalDataSubmodelTest extends TechnicalDataSubmodelTest {

    @Override
    protected String toFurtherInformationStatementId(String id) {
        return FurtherInformation.TEXTSTATEMENTPREFIX + id;
    }

    @Override
    protected String getGeneralInformationManufacturerLogoId() {
        return GeneralInformation.MANUFACTURERLOGOID;
    }

    @Override
    protected String toGeneralInformationProductImageFileId(String id) {
        return GeneralInformation.PRODUCTIMAGEPREFIX + id;
    }

    @Override
    protected String toTechnicalPropertiesMainSectionId(String id) {
        return TechnicalProperties.MAINSECTIONPREFIX + id;
    }

    @Override
    protected String toTechnicalPropertiesSubSectionId(String id) {
        return TechnicalProperties.SUBSECTIONPREFIX + id;
    }
    
}
