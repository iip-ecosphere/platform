/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.types.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Creates instances of {@link Element}, intended to be configured by transport mechanisms.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ElementFactory {

    private static final Map<Class<?>, Supplier<?>> SUPPLIERS = new HashMap<>();
    
    /**
     * Resets the creators registered with this factory.
     */
    public static void reset() {
        SUPPLIERS.clear();
    }

    /**
     * Creates an element instance for a given value type.
     * 
     * @param <T> the value type
     * @param cls the value type class
     * @return the element instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Supplier<Element<T>> getSupplier(Class<T> cls) {
        Supplier<?> supplier = SUPPLIERS.get(cls);
        if (null == supplier) {
            supplier = () -> new DefaultElement<T>();
            SUPPLIERS.put(cls, supplier);
        }
        return (Supplier<Element<T>>) supplier;
    }

    
    /**
     * Creates an element instance for a given value type.
     * 
     * @param <T> the value type
     * @param cls the value type class
     * @return the element instance
     */
    public static <T> Element<T> createElement(Class<T> cls) {
        return getSupplier(cls).get();
    }
    
    /**
     * Registers an element supplier. Nothing happens if {@code cls} or {@code supplier} are <b>null</b>.
     * 
     * @param <T> the element value type
     * @param cls the element value type class
     * @param supplier the corresponding supplier
     */
    public static <T> void registerElementSupplier(Class<T> cls, Supplier<Element<T>> supplier) {
        if (null != cls && null != supplier) {
            SUPPLIERS.put(cls, supplier);
        }
    }
    
}
