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

package de.iip_ecosphere.platform.support.http;

import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to HTTP. Requires an implementing plugin of type {@link Http} or an active 
 * {@link HttpProviderDescriptor}. Simplified interface akin to the Apache HTTP client.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Http {
    
    private static Http instance; 

    static {
        instance = PluginManager.getPluginInstance(Http.class, HttpProviderDescriptor.class);
    }

    /**
     * Returns the Http instance.
     * 
     * @return the instance
     */
    public static Http getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param http the Http instance
     */
    public static void setInstance(Http http) {
        if (null != http) {
            instance = http;
        }
    }
    
    /**
     * Creates a POST request.
     * 
     * @param uri the URI to address
     * @return the post request
     */
    public abstract HttpPost createPost(String uri);
    
    /**
     * Creates a default client instance.
     * 
     * @return the client instance
     */
    public abstract HttpClient createClient();

    /**
     * Creates a pooled client instance.
     * 
     * @return the client instance
     */
    public abstract HttpClient createPooledClient();

}
