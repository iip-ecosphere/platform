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

package de.iip_ecosphere.platform.support.aas;

/**
 * Common identifiers to be considered by implementations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface IdentifierType {
    
    /**
     * URN with valid syntax following the prefix. The prefix shall remain with the identifier.
     */
    public static final String URN_PREFIX = "urn:";

    /**
     * URN with raw text following the prefix. The prefix shall be removed from the following raw text.
     */
    public static final String URN_TEXT_PREFIX = "urnText:";

    /**
     * IRDI with valid syntax following the prefix. The prefix will be removed from the identifier.
     */
    public static final String IRDI_PREFIX = "irdi:";
    
    /**
     * IRI with valid syntax following the prefix. The prefix will be removed from the identifier.
     */
    public static final String IRI_PREFIX = "iri:";
    
    /**
     * Creates an {@link #URN_PREFIX urn-prefixed} string.
     * 
     * @param text the trailing text
     * @return the urn-prefixed string
     */
    public static String urn(String text) {
        return compose(URN_PREFIX, text);
    }

    /**
     * Creates an {@link #urn-text-prefixed} string.
     * 
     * @param text the trailing text
     * @return the urn-text-prefixed string
     */
    public static String urnText(String text) {
        return compose(URN_TEXT_PREFIX, text);
    }

    /**
     * Creates an {@link #IRDI_PREFIX irdi-prefixed} string.
     * 
     * @param text the trailing text
     * @return the irdi-prefixed string
     */
    public static String irdi(String text) {
        return compose(IRDI_PREFIX, text);
    }

    /**
     * Creates an {@link #IRI_PREFIX iri-prefixed} string.
     * 
     * @param text the trailing text
     * @return the iri-prefixed string
     */
    public static String iri(String text) {
        return compose(IRI_PREFIX, text);
    }
    
    /**
     * Composes a prefix defined in this class with the text to be prefixed.
     * 
     * @param prefix the prefix
     * @param text the text to be prefixed
     * @return the composed text representing an identifier
     */
    public static String compose(String prefix, String text) {
        return prefix + text;
    }

}
