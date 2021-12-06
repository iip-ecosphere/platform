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

import java.util.Collection;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;

/**
 * Defines the interface to the further information.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface FurtherInformation {

    /**
     * The further information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface FurtherInformationBuilder extends SubmodelElementCollectionBuilder {

        /**
         * Sets the text statements.
         * 
         * @param name the name to be used as short identifier, may be prefixed by the underlying implementation
         * @param statement the language-annotated statement
         * @return <b>this</b>
         */
        public FurtherInformationBuilder addStatement(String name, LangString statement);

    }
    
    /**
     * Returns the valid date.
     * 
     * @return denotes a date on which the data specified in the submodel was valid from for the 
     *     associated asset
     */
    public XMLGregorianCalendar getValidDate();
    
    /**
     * Sets the valid date.
     * 
     * @param validDate denotes a date on which the data specified in the submodel was valid from for the 
     *     associated asset
     */
    public void setValidDate(XMLGregorianCalendar validDate);

    /**
     * Returns the text statements with potentially prefixed ids.
     * 
     * @return statements the statements, first level maps short_ids to (language, text), may be <b>null</b> if 
     * the original value was <b>null</b> 
     */
    public Map<String, Collection<LangString>> getStatements();
    
    /**
     * Adds the given the text statements.
     * 
     * @param statements the statements, first level maps short_ids to (language, text) 
     */
    public void setStatements(Map<String, Collection<LangString>> statements);

}
