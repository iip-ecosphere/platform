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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;

/**
 * A simple Yaml-based semantic catalog for testing/shipping with the platform.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlSemanticCatalog {

    private List<DefaultSemanticIdResolutionResult> definitions = new ArrayList<>();
    
    /**
     * Returns the definitions.
     * 
     * @return the definitions
     */
    public List<DefaultSemanticIdResolutionResult> getDefinitions() {
        return definitions;
    }

    /**
     * Defines the definitions. [snakeyaml]
     * 
     * @param definitions the definitions
     */
    public void setDefinitions(List<DefaultSemanticIdResolutionResult> definitions) {
        this.definitions = definitions;
    }

    /**
     * Reads a Yaml semantic catalog from an input stream.
     * 
     * @param input the input stream
     * @return the catalog
     * @throws IOException if the catalog cannot be read
     */
    public static YamlSemanticCatalog read(InputStream input) throws IOException {
        return AbstractSetup.readFromYaml(YamlSemanticCatalog.class, input);
    }
    
    /**
     * Reads a Yaml semantic catalog from an input stream logging IO exceptions.
     * 
     * @param input the input stream
     * @return the catalog
     */
    public static YamlSemanticCatalog readSafe(InputStream input) {
        try {
            return read(input);
        } catch (IOException e) {
            LoggerFactory.getLogger(YamlSemanticCatalog.class).warn(
                "Cannot read catalog from input stream: {}. Falling back to empty catalog.", e.getMessage());
            return new YamlSemanticCatalog();
        }
    }
    
    /**
     * Turns a semantic catalog into a resolution map.
     * 
     * @param catalog the catalog
     * @param adjuster optional function to set, e.g., default values for a catalog (may be <b>null</b>)
     * @return the resolution map
     */
    public static Map<String, SemanticIdResolutionResult> toMap(YamlSemanticCatalog catalog, 
        Function<DefaultSemanticIdResolutionResult, DefaultSemanticIdResolutionResult> adjuster) {
        Map<String, SemanticIdResolutionResult> result = new HashMap<>();
        if (null != catalog) {
            for (DefaultSemanticIdResolutionResult r : catalog.getDefinitions()) {
                if (null != adjuster) {
                    r = adjuster.apply(r);
                }
                result.put(r.getSemanticId(), r);
            }
        }
        return result;
    }

    /**
     * A default Yaml-based resolver.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class YamlBasedResolver extends SemanticIdResolver {

        private String name;
        private Map<String, SemanticIdResolutionResult> catalog;
        private Predicate<String> responsible;

        /**
         * Creates an instance based on a resource name {@link ResourceLoader}.
         * 
         * @param name the print name of the resolver
         * @param resource the resource containing the catalog
         * @param responsible a predicate indicating responsibility
         * @param adjuster optional function to set, e.g., default values for a catalog (may be <b>null</b>)
         */
        public YamlBasedResolver(String name, String resource, Predicate<String> responsible, 
            Function<DefaultSemanticIdResolutionResult, DefaultSemanticIdResolutionResult> adjuster) {
            this(name, readSafe(ResourceLoader.getResourceAsStream(resource)), responsible, adjuster);
        }
        
        /**
         * Creates an instance.
         * 
         * @param name the print name of the resolver
         * @param catalog the catalog to use
         * @param responsible a predicate indicating responsibility
         * @param adjuster optional function to set, e.g., default values for a catalog (may be <b>null</b>)
         */
        public YamlBasedResolver(String name, YamlSemanticCatalog catalog, Predicate<String> responsible, 
            Function<DefaultSemanticIdResolutionResult, DefaultSemanticIdResolutionResult> adjuster) {
            this.catalog = toMap(catalog, adjuster);
            this.responsible = responsible;
            this.name = null == name || name.length() == 0 ? getClass().getName() : name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public SemanticIdResolutionResult resolveSemanticId(String semanticId) {
            return catalog.get(semanticId);
        }

        @Override
        public boolean isResponsible(String semanticId) {
            return responsible.test(semanticId);
        }
        
    }
    
}
