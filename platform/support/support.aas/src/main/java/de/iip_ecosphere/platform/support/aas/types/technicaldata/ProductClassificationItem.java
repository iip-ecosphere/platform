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

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollection;

/**
 * Defines the interface of a product classification item.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProductClassificationItem extends DelegatingSubmodelElementCollection {

    public static final String SEMANTIC_ID = iri(
        "https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationItem/1/1");

    /**
     * Creates an instance.
     * 
     * @param delegate the underlying delegate
     */
    ProductClassificationItem(SubmodelElementCollection delegate) {
        super(delegate);
    }

    /**
     * Returns the class of the associated product or industrial equipment in the classification system according to 
     * the notation of the system.
     * 
     * @return the product class id, which is ideally used to reference the IRI/ IRDI of the product class (may but 
     *   shall not be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public String getProductClassId() throws ExecutionException {
        return getStringValue(this, "ProductClassId");
    }

    /**
     * Returns the common name of the classification system. Examples for common names for classification systems are 
     * "ECLASS" or "IEC CDD".
     * 
     * @return the name of the classification system (may but shall not be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public String getProductClassificationSystem() throws ExecutionException {
        return getStringValue(this, "ProductClassificationSystem");
    }
    
    /**
     * Returns the version of the classification system.
     * 
     * @return the version (may be <b>null</b>)
     * @throws ExecutionException if accessing the value fails
     */
    public String getClassificationSystemVersion() throws ExecutionException {
        return getStringValue(this, "ClassificationSystemVersion");
    }
    
}