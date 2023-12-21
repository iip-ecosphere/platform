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
import java.util.List;

import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation;

/**
 * A fake general information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeGeneralInformation extends FakeSubmodelElementCollection implements GeneralInformation {

    private String manufacturerName;
    private List<LangString> manufacturerProductDesignation = new ArrayList<>();
    private String manufacturerPartNumber;
    private String manufacturerOrderCode;

    /**
     * A fake builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FakeGeneralInformationBuilder extends FakeSubmodelElementCollectionBuilder 
        implements GeneralInformationBuilder {

        private FakeGeneralInformation instance;
        
        /**
         * Creates an instance.
         * 
         * @param parent the parent builder
         * @param manufacturerName the manufacturer name
         * @param manufacturerProductDesignation the manufacturer product designation
         * @param manufacturerPartNumber the manufacturer part number
         * @param manufacturerOrderCode the manufacturer order code
         * @throws IllegalArgumentException may be thrown if {@code idShort} is not given
         */
        protected FakeGeneralInformationBuilder(FakeSubmodelElementContainerBuilder parent, String manufacturerName,
            LangString manufacturerProductDesignation, String manufacturerPartNumber,
            String manufacturerOrderCode) {
            super(parent, "GeneralInformation", false, false);
            instance.manufacturerName = manufacturerName;
            instance.manufacturerProductDesignation.add(manufacturerProductDesignation); 
            instance.manufacturerPartNumber = manufacturerPartNumber;
            instance.manufacturerOrderCode = manufacturerOrderCode;
        }
        
        @Override
        protected FakeSubmodelElementCollection createInstance(String idShort) {
            instance = new FakeGeneralInformation(idShort);
            return instance;
        }

        @Override
        public GeneralInformationBuilder addProductImageFile(String name, String file, String mimeType) {
            return this; // ignored for now
        }

        @Override
        public GeneralInformationBuilder setManufacturerLogo(String file, String mimeType) {
            return this; // ignored for now
        }
        
    }

    /**
     * Creates an instance.
     * 
     * @param idShort the idShort
     */
    protected FakeGeneralInformation(String idShort) {
        super(idShort);
    }


    @Override
    public String getManufacturerName() {
        return manufacturerName; // ignored for now
    }

    @Override
    public List<LangString> getManufacturerProductDesignation() {
        return manufacturerProductDesignation; // ignored for now
    }

    @Override
    public String getManufacturerPartNumber() {
        return manufacturerPartNumber; // ignored for now
    }

    @Override
    public String getManufacturerOrderCode() {
        return manufacturerOrderCode; // ignored for now
    }

    @Override
    public FileDataElement getManufacturerLogo() {
        return null; // ignored for now
    }

    @Override
    public Iterable<FileDataElement> getProductImages() {
        return null; // ignored for now
    }

}
