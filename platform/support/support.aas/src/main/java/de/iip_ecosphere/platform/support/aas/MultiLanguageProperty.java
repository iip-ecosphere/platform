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

package de.iip_ecosphere.platform.support.aas;

import java.util.Map;

import de.iip_ecosphere.platform.support.Builder;

/**
 * Represents an AAS Multi-language Property.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface MultiLanguageProperty extends Element, DataElement {

    /**
     * A builder for {@link MultiLanguageProperty}.
     */
    public interface MultiLanguagePropertyBuilder extends Builder<MultiLanguageProperty> {
        
        /**
         * Adds a language string as text of this data element.
         * 
         * @param text the text
         * @return <b>this</b>
         */
        public MultiLanguagePropertyBuilder addText(LangString text);
        
        /**
         * Sets the semantic ID of the property in terms of a reference.
         * 
         * @param refValue the reference value (supported: irdi:<i>irdiValue</i>)
         * @return <b>this</b>
         */
        public MultiLanguagePropertyBuilder setSemanticId(String refValue);

        /**
         * Sets the description in terms of language strings.
         * 
         * @param description the description
         * @return <b>this</b>
         */
        public MultiLanguagePropertyBuilder setDescription(LangString... description);

    }

    /**
     * Returns the description of this property, potentially in different languages.
     * 
     * @return the description, may be empty or <b>null</b>
     */
    public Map<String, LangString> getDescription();

}
