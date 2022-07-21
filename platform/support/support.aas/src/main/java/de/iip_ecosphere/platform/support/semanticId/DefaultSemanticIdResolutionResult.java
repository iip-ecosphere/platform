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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of resolution result, to be read from YAML.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultSemanticIdResolutionResult implements SemanticIdResolutionResult {

    private String semanticId = "";
    private String kind = "";
    private String publisher = "";
    private String version = "";
    private String revision = "";
    private Map<String, DefaultNaming> naming = new HashMap<>();
    
    /**
     * Default naming implementation.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DefaultNaming implements Naming {

        private String name = "";
        private String structuredName = "";
        private String description = "";
        
        @Override
        public String getName() {
            return name;
        }

        /**
         * Changes the name. [snakeyaml]
         * 
         * @param name the new name
         */
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getStructuredName() {
            return structuredName;
        }

        /**
         * Changes the structured name. [snakeyaml]
         * 
         * @param structuredName the new structured name
         */
        public void setStructuredName(String structuredName) {
            this.structuredName = structuredName;
        }

        @Override
        public String getDescription() {
            return description;
        }

        /**
         * Changes the description. [snakeyaml]
         * 
         * @param description the new description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
    }
    
    @Override
    public String getSemanticId() {
        return semanticId;
    }
    
    /**
     * Changes the semantic id. [snakeyaml]
     * 
     * @param semanticId the new semantic id
     */
    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

    @Override
    public String getKind() {
        return kind;
    }

    /**
     * Changes the kind. [snakeyaml]
     * 
     * @param kind the new kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    @Override
    public String getPublisher() {
        return publisher;
    }

    /**
     * Changes the publisher. [snakeyaml]
     * 
     * @param publisher the new publisher
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Changes the publisher. [snakeyaml]
     * 
     * @param version the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    /**
     * Changes the publisher. [snakeyaml]
     * 
     * @param revision the new revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public Map<String, ? extends Naming> getNaming() {
        return naming;
    }

    /**
     * Changes the naming. [snakeyaml]
     * 
     * @param naming the new naming
     */
    public void setNaming(Map<String, Object> naming) {
        this.naming.clear();
        for (Map.Entry<String, Object> e: naming.entrySet()) {
            if (e.getValue() instanceof DefaultNaming) {
                this.naming.put(e.getKey(), (DefaultNaming) e.getValue());
            } else if (e.getValue() instanceof Map<?, ?>) {
                Map<?, ?> val = (Map<?, ?>) e.getValue();
                DefaultNaming n = new DefaultNaming();
                n.setDescription(getStringSafe(val, "description"));
                n.setName(getStringSafe(val, "name"));
                n.setStructuredName(getStringSafe(val, "structuredName"));
                this.naming.put(e.getKey(), n);
            }
        }
        this.naming = Collections.unmodifiableMap(this.naming);
    }

    /**
     * Turns a value from {@code values} to a string considering <b>null</b>.
     * 
     * @param values the values to read from 
     * @param key the key to read
     * @return the value
     */
    private static String getStringSafe(Map<?, ?> values, String key) {
        Object o = values.get(key);
        return null == o ? "" : o.toString();
    }

}
