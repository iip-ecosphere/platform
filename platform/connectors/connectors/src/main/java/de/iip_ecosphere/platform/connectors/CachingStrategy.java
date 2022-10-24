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

package de.iip_ecosphere.platform.connectors;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.ConnectorParameter.CacheMode;

/**
 * Defines a pluggable caching strategy. Caching strategies must have a public no-arg constructor. [testing, mocking]
 * 
 * @author Holger Eichelberger, SSE
 */
public interface CachingStrategy {
    
    /**
     * Defines the actual cache mode.
     * 
     * @param mode the actual cache mode
     */
    public void setCacheMode(CacheMode mode);

    /**
     * Checks the cache if configured. 
     * 
     * @param data the data to send
     * @return {@code true} for sending {@code data}, {@code false} for not sending {@code data}
     */
    public boolean checkCache(Object data);
    
    /**
     * Checks a multi-cache if configured.
     * 
     * @param key a key into the cache
     * @param data the data to send
     * @return {@code true} for sending {@code data}, {@code false} for not sending {@code data}
     */
    public boolean checkCache(String key, Object data);
    
    /**
     * Clears the cache.
     */
    public void clearCache();

    /**
     * Creates a default caching strategy instance based on a public non-arg constructor.
     * 
     * @param cls the class to create the strategy for, may be <b>null</b>
     * @return the strategy instance or an instance of {@link DefaultCachingStrategy}
     */
    public static CachingStrategy createInstance(Class<? extends CachingStrategy> cls) {
        CachingStrategy result = null;
        if (cls != null) {
            try {
                result = cls.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LoggerFactory.getLogger(CachingStrategy.class).warn("Creating caching strategy for {}: {}", 
                    cls.getName(), e.getMessage());
            }
        }
        if (null == result) {
            result = new DefaultCachingStrategy();
        }
        return result;
    }
    
}
