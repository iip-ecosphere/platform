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
 * Represents an AAS string in a certain language.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LangString {

    private String language;
    private String description;

    /**
     * Creates the string.
     * 
     * @param language the language
     * @param description the text/description/string
     */
    public LangString(String language, String description) {
        this.language = language;
        this.description = description;
    }
    
    /**
     * Returns the language.
     * 
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Returns the description.
     * 
     * @return the description/text/string
     */
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return "[lang='" + language + "', desc='" + description + "']"; 
    }
    
    @Override
    public int hashCode() {
        int result = null == getLanguage() ? 0 : getLanguage().hashCode();
        result += null == getDescription() ? 0 : getDescription().hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (obj instanceof LangString) {
            LangString ls = (LangString) obj;
            result = equalsSafe(getLanguage(), ls.getLanguage());
            result &= equalsSafe(getDescription(), ls.getDescription());
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * Returns whether {@code s1} and {@code s2} are equal, even considering <b>null</b>.
     *  
     * @param s1 the first string
     * @param s2 the second string
     * @return whether both strings are either <b>null</b> or not null and equal
     */
    private static boolean equalsSafe(String s1, String s2) {
        return null == s1 ? null == s2 : s1.equals(s2);
    }
    
}
