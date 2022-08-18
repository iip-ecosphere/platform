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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import org.slf4j.LoggerFactory;

/**
 * Defines the interface for resolution of semantic ids, IRDIs, IRIs ...
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class SemanticIdResolver {

    public static final String GERMAN = Locale.GERMAN.getLanguage();
    public static final String ENGLISH = Locale.ENGLISH.getLanguage();
    
    private static List<SemanticIdResolver> resolvers;
    
    /**
     * Returns the name of the resolver.
     * 
     * @return the name of the resolver, by default the class name
     */
    public String getName() {
        return getClass().getName();
    }
    
    /**
     * Tries to resolve a given semantic id.
     *  
     * @param semanticId the semantic id to resolve
     * @return the resolution result or <b>null</b> for no resolution
     */
    public abstract SemanticIdResolutionResult resolveSemanticId(String semanticId);
    
    /**
     * Returns whether this resolver believes that it is able to resolve the given {@code semanticId}.
     * 
     * @param semanticId the semantic id
     * @return {@code true} if the resolve is responsible and it makes sense to call {@link #resolve(String)} or
     *   {@code false} if the resolver declares itselt as not responsible
     */
    public abstract boolean isResponsible(String semanticId);
    
    /**
     * Tries to resolve a given semantic id by querying all known resolver instances.
     *  
     * @param semanticId the semantic id to resolve
     * @return the resolution result or <b>null</b> for no resolution
     */
    public static SemanticIdResolutionResult resolve(String semanticId) {
        SemanticIdResolutionResult result = null;
        initialize();
        for (SemanticIdResolver resolver : resolvers) {
            if (resolver.isResponsible(semanticId)) {
                result = resolver.resolveSemanticId(semanticId);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Lazy initialization of the resolvers.
     */
    private static void initialize() {
        if (null == resolvers) {
            resolvers = new ArrayList<>();
            ServiceLoader<SemanticIdResolverDescriptor> loader = ServiceLoader.load(SemanticIdResolverDescriptor.class);
            loader.forEach(l -> {
                SemanticIdResolver resolver = l.createResolver();
                resolvers.add(resolver);
                LoggerFactory.getLogger(SemanticIdResolver.class).info("Registered semanticId resolver {}", 
                    resolver.getName());
            });
        }
    }
    
    /**
     * Returns whether the given resolver class is known.
     * 
     * @param cls the class to look for
     * @return {@code true} if {@code cls} is known, {@code false} else
     */
    public static boolean hasResolver(Class<? extends SemanticIdResolver> cls) {
        boolean result = false;
        initialize();
        for (SemanticIdResolver resolver : resolvers) {
            if (cls.isInstance(resolver)) {
                result = true;
                break;
            }
        }
        return result;
    }
    
}
