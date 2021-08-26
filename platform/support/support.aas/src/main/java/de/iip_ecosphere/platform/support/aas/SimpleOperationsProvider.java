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
import java.util.Map;
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
    
    @Override
    public OperationsProvider defineOperation(String category, String name, Function<Object[], Object> function) {
        funcs.put(OP_PREFIX + category + "_" + name, function);
        return this;
    }

    @Override
    public Function<Object[], Object> getOperation(String category, String name) {
        return funcs.get(OP_PREFIX + category + "_" + name);
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
        return get.get(name);
    }

    @Override
    public Consumer<Object> getSetter(String name) {
        return set.get(name);
    }

}
