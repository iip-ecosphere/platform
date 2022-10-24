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

import java.util.Map;

import de.iip_ecosphere.platform.connectors.ConnectorParameter.CacheMode;

/**
 * A default caching strategy.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultCachingStrategy implements CachingStrategy {

    private Object singleCache;
    private Map<String, Object> cache;
    private CacheMode mode = CacheMode.NONE;

    @Override
    public void setCacheMode(CacheMode mode) {
        if (null != mode) {
            this.mode = mode;
        }
    }
    
    @Override
    public boolean checkCache(Object data) {
        boolean send = true;
        if (null != data) {
            switch (mode) {
            case HASH:
                if (null == singleCache || singleCache.hashCode() != data.hashCode()) {
                    send = true;
                    singleCache = data;
                }
                break;
            case EQUALS:
                if (null == singleCache || !singleCache.equals(data)) {
                    send = true;
                    singleCache = data;
                }
                break;
            default:
                break;
            }
        }
        return send;
    }
    
    @Override
    public boolean checkCache(String key, Object data) {
        boolean send = true;
        switch(mode) {    
        case HASH:
            send = checkCache(key, data, (o1, o2) -> o1.hashCode() == o2.hashCode());
            break;
        case EQUALS:
            send = checkCache(key, data, (o1, o2) -> o1.equals(o2));
            break;
        default:
            break;
        }
        return send;
    }
    
    /**
     * A functor indicating that two objects are considered the same.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected interface ConsideredSame {

        /**
         * Checks the given two objects.
         * 
         * @param o1 the first object
         * @param o2 the second object
         * @return {@code true} for equals, {@code false} else
         */
        public boolean isSame(Object o1, Object o2);
    }
    
    /**
     * Checks the cache.
     * 
     * @param key the AAS id to consider caches for different AAS
     * @param data the data to send
     * @param same a functor checking the data
     * @return {@code true} for sending {@code data}, {@code false} for not sending {@code data}
     */
    private boolean checkCache(String key, Object data, ConsideredSame same) {
        boolean send = true;
        Object o = cache.get(key);
        if (null != o) {
            if (!same.isSame(data, o)) {
                cache.put(key, data);
            } else {
                send = false;
            }
        } else {
            cache.put(key, data);
        }
        return send;
    }

    @Override
    public void clearCache() {
        singleCache = null;
        cache.clear();
    }

}
