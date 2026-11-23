/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.tools.lib;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Generic XML utility class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class XmlUtilities {

    /**
     * Creates a default document builder factory.
     * 
     * @return the document builder factory
     */
    public static DocumentBuilderFactory newInstance() {
        return DocumentBuilderFactory.newInstance();
    }

    /**
     * Creates a secure document builder factory. Resorts to default if not possible.
     * 
     * @return the document builder factory
     */
    public static DocumentBuilderFactory newSecureInstance() {
        DocumentBuilderFactory dbf = newInstance();
        try {
            configureSecure(dbf);
        } catch (ParserConfigurationException e) {
            System.out.println("Cannot configure secure document builder factory. Resorting to default. " 
                + e.getMessage());
        }
        return dbf;
    }
    
    /**
     * Configures {@code dbf} for security.
     * 
     * @param dbf the document builder factory
     * @return the configured document builder factory
     * @throws ParserConfigurationException if configuring fails
     */
    public static DocumentBuilderFactory configureSecure(DocumentBuilderFactory dbf) 
        throws ParserConfigurationException {
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return dbf;
    }
    
}
