/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.fakeAas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.Element;
import de.iip_ecosphere.platform.support.aas.LangString;

/**
 * Implements a basis fake element.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class FakeElement implements Element {

    private String idShort;
    private String semanticId;
    private Map<String, LangString> description;
    
    /**
     * Creates the instance.
     * 
     * @param idShort the short id.
     */
    protected FakeElement(String idShort) {
        this.idShort = idShort;
    }
    
    @Override
    public String getIdShort() {
        return idShort;
    }
    
    @Override
    public void update() {
        // not needed here
    }

    /**
     * Returns the semantic id of the element.
     * 
     * @param stripPrefix if the plain semantic id or the prefix shall also be emitted
     * @return the semantic id in textual format, e.g., with/out identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     */
    public String getSemanticId(boolean stripPrefix) {
        return semanticId;
    }

    /**
     * Changes the semantic id of the element.
     * 
     * @param semanticId the semantic id in textual format with identifier prefix, or <b>null</b> if there is no 
     *     semantic id or no translation to string
     */
    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

    /**
     * Sets the description in terms of language strings.
     * 
     * @param description the description
     * @return <b>this</b>
     */
    public void setDescription(LangString... description) {
        if (description.length > 0) {
            this.description = new HashMap<>();
            for (LangString d : description) {
                this.description.put(d.getLanguage(), d);
            }
        } else {
            this.description = null;
        }
    }

    /**
     * Adds a single description.
     * 
     * @param text the description
     */
    public void addDescription(LangString text) {
        if (null == description) {
            this.description = new HashMap<>();
        }
        description.put(text.getLanguage(), text);
    }
    
    /**
     * Returns the description of the element.
     * 
     * @return the description
     */
    public Map<String, LangString> getDescription() {
        return null == description ? null : Collections.unmodifiableMap(description);
    }

}
