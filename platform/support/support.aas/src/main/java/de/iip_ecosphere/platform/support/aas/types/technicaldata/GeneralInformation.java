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

import java.util.List;

import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Defines the interface to the general technical information.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface GeneralInformation extends SubmodelElementCollection {

    /**
     * The general information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface GeneralInformationBuilder extends SubmodelElementCollectionBuilder {

        /**
         * Adds an optional product image in common format (.png, .jpg).
         * 
         * @param name the name to be used as id, may be prefixed by the underlying implementation
         * @param file the relative or absolute file name with extension
         * @param mimeType the mime type of the file
         * @return <b>this</b>
         */
        public GeneralInformationBuilder addProductImageFile(String name, String file, String mimeType);

        /**
         * Sets the logo of the manufacturer provided in common format (.png, .jpg).
         * 
         * @param file the relative or absolute file name with extension
         * @param mimeType the mime type of the file
         * @return <b>this</b>
         */
        public GeneralInformationBuilder setManufacturerLogo(String file, String mimeType);

    }
    
    /**
     * Returns the manufacturer name.
     * 
     * @return the name
     */
    public String getManufacturerName();

    /**
     * Returns the manufacturer product designation.
     * 
     * @return the product designation
     */
    public List<de.iip_ecosphere.platform.support.aas.LangString> getManufacturerProductDesignation();

    /**
     * Returns the manufacturer part number.
     * 
     * @return the part number
     */
    public String getManufacturerPartNumber();

    /**
     * Returns the manufacturer order code.
     * 
     * @return the order code
     */
    public String getManufacturerOrderCode();

    /**
     * Gets the logo of the manufacturer provided in common format (.png, .jpg).
     * 
     * @return the logo
     */
    public FileDataElement getManufacturerLogo();

    /**
     * Returns optional product images in common format (.png, .jpg).
     * 
     * @return the images
     */
    public Iterable<FileDataElement> getProductImages();
    
}
