/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple, default operations provider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleOperationsProvider implements OperationsProvider {

    private static final String OP_PREFIX = "op_";
    private static final String SERVICE_PREFIX = "service_";
    
    private Map<String, Function<Object[], Object>> funcs = new HashMap<>();
    private Map<String, Supplier<Object>> get = new HashMap<>();
    private Map<String, Consumer<Object>> set = new HashMap<>();
    private Interceptor interceptor;
    
    @Override
    public OperationsProvider defineOperation(String category, String name, Function<Object[], Object> function) {
        funcs.put(OP_PREFIX + category + "_" + name, function);
        return this;
    }

    @Override
    public Function<Object[], Object> getOperation(String category, String name) {
        Function<Object[], Object> result = funcs.get(OP_PREFIX + category + "_" + name);
        if (interceptor != null) {
            result = interceptor.getOperation(category, name, result);
        }
        return result;
    }

    @Override
    public Function<Object[], Object> getServiceFunction(String name) {
        return funcs.get(SERVICE_PREFIX + name);
    }

    @Override
    public OperationsProvider defineServiceFunction(String name, Function<Object[], Object> function) {
        funcs.put(SERVICE_PREFIX + name, function);
        return this;
    }

    @Override
    public OperationsProvider defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
        this.get.put(name, get);
        this.set.put(name, set);
        return this;
    }

    @Override
    public Supplier<Object> getGetter(String name) {
        Supplier<Object> result = get.get(name);
        if (interceptor != null) {
            result = interceptor.getGetter(name, result);
        }
        return result;
    }

    @Override
    public Consumer<Object> getSetter(String name) {
        Consumer<Object> result = set.get(name);
        if (interceptor != null) {
            result = interceptor.getSetter(name, result);
        }
        return result;
    }
    
    @Override
    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }
    
    @Override
    public Set<String> getOperations(boolean qualified) {
        return getOperations(null, qualified);
    }

    @Override
    public Set<String> getOperations(String category, boolean qualified) {
        Set<String> result = new HashSet<>();
        if (category != null && !category.endsWith("_")) {
            category += "_";
        }
        for (String name: funcs.keySet()) {
            if (null == category || name.startsWith(category) || name.startsWith(OP_PREFIX + category)) {
                if (!qualified) {
                    int pos = name.indexOf("_");
                    if (pos > 0 && name.startsWith(OP_PREFIX)) {
                        pos = name.indexOf("_", pos + 1);
                    }
                    if (pos > 0) {
                        name = name.substring(pos + 1);
                    }
                }
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public Set<String> getServiceOperations(boolean qualified) {
        return getOperations(SERVICE_PREFIX, qualified);
    }    

}
