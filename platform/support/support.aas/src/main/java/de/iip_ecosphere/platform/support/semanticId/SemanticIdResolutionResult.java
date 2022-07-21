/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.semanticId;

import java.util.Map;

/**
 * Represents the result of a semantic id resolution. Largely inspired by <a href="https://eclass.eu/">ECLASS</a>.
 * We intentionally left out the translation factors for now.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface SemanticIdResolutionResult {

    public interface Naming {

        /**
         * Returns the name.
         *
         * @return the name
         */
        public String getName();

        /**
         * Returns the structured name.
         *
         * @return the structured name
         */
        public String getStructuredName();
        
        /**
         * Returns the description.
         *
         * @return the description
         */
        public String getDescription();
        
    }
    
    /**
     * Returns the resolved semantic id.
     * 
     * @return the resolved semantic id
     */
    public String getSemanticId();
    
    /**
     * Returns the kind of semantic id.
     * 
     * @return the kind
     */
    public String getKind();

    /**
     * Returns the publisher of the semantic id definition.
     * 
     * @return the publisher
     */
    public String getPublisher();
    
    /**
     * Returns the version of the semantic id definition.
     * 
     * @return the version
     */
    public String getVersion();
    
    /**
     * Returns the revision of the semantic id definition.
     * 
     * @return the revision
     */
    public String getRevision();
    
    /**
     * Returns the naming according to languages.
     * 
     * @return the language (code) - naming mapping
     */
    public Map<String, Naming> getNaming();

}
