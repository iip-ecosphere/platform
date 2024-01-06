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
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollection;

/**
 * Defines the interface to the further information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FurtherInformation extends DelegatingSubmodelElementCollection {

    public static final String ID_SHORT = "FurtherInformation";
    public static final String IRI_TEXT_STATEMENT = iri("https://admin-shell.io/ZVEI/TechnicalData/TextStatement/1");
    public static final String TEXT_STATEMENT_PREFIX = "TextStatement";
    
    /**
     * Creates an instance.
     * 
     * @param delegate the SMC delegate
     */
    FurtherInformation(SubmodelElementCollection delegate) {
        super(delegate);
    }
    
    /**
     * Returns the valid date.
     * 
     * @return denotes a date on which the data specified in the submodel was valid from for the 
     *     associated asset
     * @throws ExecutionException if accessing the valid date fails
     */
    public XMLGregorianCalendar getValidDate() throws ExecutionException {
        try {
            return (XMLGregorianCalendar) getProperty("ValidDate").getValue();
        } catch (ClassCastException e) { // preliminary
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
    /**
     * Sets the valid date.
     * 
     * @param validDate denotes a date on which the data specified in the submodel was valid from for the 
     *     associated asset
     * @throws ExecutionException if setting the data fails
     */
    public void setValidDate(XMLGregorianCalendar validDate) throws ExecutionException {
        getProperty("ValidDate").setValue(validDate);
    }
    
    /**
     * Returns the text statements with potentially prefixed ids.
     * 
     * @return statements the statements, first level maps short_ids to (language, text), may be <b>null</b> if 
     * the original value was <b>null</b> 
     */
    public Iterable<MultiLanguageProperty> getStatements() {
        return stream(elements(), MultiLanguageProperty.class, 
            e -> IRI_TEXT_STATEMENT.equals(e.getSemanticId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Returns a specific statement.
     * 
     * @param nr the nr of the statement
     * @return the statement or <b>null</b> for none
     * @throws ExecutionException if the statement cannot be accessed
     */
    public MultiLanguageProperty getStatement(int nr) throws ExecutionException {
        try {
            return (MultiLanguageProperty) getElement(getCountingIdShort(TEXT_STATEMENT_PREFIX, nr));
        } catch (ClassCastException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
    }
    
}
