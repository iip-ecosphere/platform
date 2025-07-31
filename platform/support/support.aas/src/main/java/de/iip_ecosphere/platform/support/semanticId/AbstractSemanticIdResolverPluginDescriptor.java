/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.semanticId;

import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * Basic semanticid plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractSemanticIdResolverPluginDescriptor extends SingletonPluginDescriptor<SemanticIdResolver> 
    implements SemanticIdResolverDescriptor {

    /**
     * Creates an instance.
     */
    public AbstractSemanticIdResolverPluginDescriptor() {
        super("semanticId", null, SemanticIdResolver.class, null);
    }

    @Override
    protected PluginSupplier<SemanticIdResolver> initPluginSupplier(
        PluginSupplier<SemanticIdResolver> pluginSupplier) { 
        return p -> createResolver();
    }
    
    @Override
    protected abstract String initId(String id);

}
