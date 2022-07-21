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
    private Map<String, Naming> naming = new HashMap<>();
    
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
    public Map<String, Naming> getNaming() {
        return naming;
    }

    /**
     * Changes the naming. [snakeyaml]
     * 
     * @param naming the new naming
     */
    public void setNaming(Map<String, Naming> naming) {
        this.naming = naming;
    }

}
