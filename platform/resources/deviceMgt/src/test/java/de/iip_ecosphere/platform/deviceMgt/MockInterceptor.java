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

package de.iip_ecosphere.platform.deviceMgt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.aas.OperationsProvider.Interceptor;

/**
 * Test mocking interceptor, as mocking some objects does not work anymore through plugins, classloading. Might be
 * some side effects are now prevented.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MockInterceptor implements Interceptor {

    private Map<String, Function<Object[], Object>> operations = new HashMap<>();
    private Map<String, Integer> counters = new HashMap<>();

    /**
     * Returns an operation key.
     * 
     * @param category the category
     * @param name the operation name
     * @return the key
     */
    private String getKey(String category, String name) {
        return category + "_" + name;
    }
    
    @Override
    public Function<Object[], Object> getOperation(String category, String name,
        Function<Object[], Object> origin) {
        String key = getKey(category, name);
        counters.put(key, getCounter(key) + 1);
        Function<Object[], Object> result = operations.get(key);
        return null == result ? origin : result;
    }

    /**
     * Intercept an operation with default category.
     * 
     * @param name the name of the operation
     * @param function the replacement function to be used when the original is being called
     * @see #intercept(String, String, Function)
     */
    public void intercept(String name, Function<Object[], Object> function) {
        this.intercept("service", name, function);
    }

    /**
     * Intercept an operation with a given category.
     * 
     * @param category the category
     * @param name the name of the operation
     * @param function the replacement function to be used when the original is being called
     */
    public void intercept(String category, String name, Function<Object[], Object> function) {
        operations.put(getKey(category, name), function);
    }
        
    /**
     * Returns the number of calls to a given operation with default category.
     * 
     * @param name the name of the operation
     * @return the number of calls
     */
    public int getCalls(String name) {
        return getCalls("service", name);
    }

    /**
     * Returns the call counter for the given {@code key}.
     * 
     * @param key the key ({@link #getKey(String, String))
     * @return the number of calls
     */
    private int getCounter(String key) {
        Integer result = counters.get(key);
        return null == result ? 0 : result;
    }

    /**
     * Returns the number of calls to a given operation with default category.
     * 
     * @param category the category
     * @param name the name of the operation
     * @return the number of calls
     */
    public int getCalls(String category, String name) {
        return getCounter(getKey(category, name));
    }

    /**
     * Clears the intercepted operations and the number of calls. Resets the interceptor to no interception and no 
     * calls done.
     */
    public void clear() {
        operations.clear();
        counters.clear();
    }
        
}