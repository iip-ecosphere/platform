/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.types.common;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;

/**
 * Utility methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Utils {

    /**
     * Returns a 2 digits counting id short as usual in IDTA templates.
     * 
     * @param prefix the prefix name
     * @param nr the counting number
     * @return the composed idShort
     */
    public static String getCountingIdShort(String prefix, int nr) {
        return prefix + String.format("%02d", nr);
    }

    /**
     * Assert that {@code valid} else emits an {@link IllegalArgumentException} with text 
     * {@code exception}.
     * 
     * @param valid the validity criteria
     * @param exception the exception text
     * @throws IllegalArgumentException if not {@code valid}
     */
    public static void assertThat(boolean valid, String exception) {
        if (!valid) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Creates a multi-language property.
     * 
     * @param builder the parent builder
     * @param enable whether the creation of multi-language properties is enabled (incompatibility with AASX Explorer)
     * @param idShort the idShort
     * @param semanticId the semanticId of the property
     * @param texts the values of the property
     */
    public static void createMultiLanguageProperty(SubmodelElementContainerBuilder builder, boolean enable, 
        String idShort, String semanticId, LangString... texts) {
        if (enable) {
            MultiLanguagePropertyBuilder mlpb = builder
                .createMultiLanguagePropertyBuilder(idShort)
                .setSemanticId(semanticId);
            for (LangString t: texts) {
                mlpb.addText(t);
            }
            mlpb.build();
        }
    }

    /**
     * Helper to create a Gregorian calendar.
     * 
     * @param date may be <b>null</b> for now or in format YYY-MM-DDThh:mm:ss.SSS+hh:mm
     * @return the Gregorian calendar, <b>null</b> if the creation fails
     */
    public static XMLGregorianCalendar parse(String date) {
        XMLGregorianCalendar result = null;
        if (null == date) {
            try {
                GregorianCalendar now = new GregorianCalendar();
                result = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            } catch (DatatypeConfigurationException e) {
            }
        } else {
            try {
                result = DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
            } catch (DatatypeConfigurationException e) {
            }
        }
        return result;
    }

}
