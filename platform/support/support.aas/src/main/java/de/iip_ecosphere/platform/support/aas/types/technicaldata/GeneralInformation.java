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

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.support.aas.FileDataElement;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollection;

/**
 * Defines the interface to the general technical information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GeneralInformation extends DelegatingSubmodelElementCollection {

    public static final String ID_SHORT = "GeneralInformation";
    public static final String SEMANTIC_ID_PRODUCT_IMAGE 
        = iri("https://admin-shell.io/ZVEI/TechnicalData/ProductImage/1/1");
    public static final String MANUFACTURER_LOGOID = "ManufacturerLogo";
    public static final String PRODUCTIMAGE_PREFIX = "ProductImage";
    
    /**
     * Creates an instance.
     * 
     * @param delegate the delegate submodel element collection
     */
    protected GeneralInformation(SubmodelElementCollection delegate) {
        super(delegate);
    }
    
    /**
     * Returns the manufacturer name.
     * 
     * @return the name (may but shall not be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public String getManufacturerName() throws ExecutionException {
        return getStringValue(this, "ManufacturerName");
    }

    /**
     * Returns the manufacturer product designation.
     * 
     * @return the product designation
     * @throws ExecutionException if accessing the property fails
     */
    public MultiLanguageProperty getManufacturerProductDesignation() throws ExecutionException {
        try {
            return (MultiLanguageProperty) getElement("ManufacturerProductDesignation");
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns the manufacturer part number.
     * 
     * @return the part number (may but shall not be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public String getManufacturerArticleNumber() throws ExecutionException {
        return getStringValue(this, "ManufacturerArticleNumber");
    }

    /**
     * Returns the manufacturer order code.
     * 
     * @return the order code (may but shall not be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public String getManufacturerOrderCode() throws ExecutionException {
        return getStringValue(this, "ManufacturerOrderCode");
    }

    /**
     * Gets the logo of the manufacturer provided in common format (.png, .jpg).
     * 
     * @return the logo (may be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public FileDataElement getManufacturerLogo() throws ExecutionException {
        try {
            return (FileDataElement) getDataElement("ManufacturerLogo");
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }

    /**
     * Returns optional product images in common format (.png, .jpg).
     * 
     * @return the images
     */
    public Iterable<FileDataElement> getProductImages() {
        return stream(elements(), FileDataElement.class, e -> SEMANTIC_ID_PRODUCT_IMAGE.equals(e.getSemanticId()))
            .collect(Collectors.toList());
    }
    
}
