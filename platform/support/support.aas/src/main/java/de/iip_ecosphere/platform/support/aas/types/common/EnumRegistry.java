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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Allows to register alternative enums for "open" enum specifications.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EnumRegistry {
    
    private static final Map<Class<?>, Set<Class<?>>> ENUMS = new HashMap<>();

    /**
     * Registers an enum class. Registers {@code cls} for itself and its implementing interfaces.
     * 
     * @param <T> the enum type
     * @param cls the actual enum class
     */
    public static <T extends Enum<T>> void registerEnum(Class<T> cls) {
        process(cls, cls, true);
    }

    /**
     * Unregisters an enum class. Unregisters {@code cls} for itself and its implementing interfaces.
     * 
     * @param <T> the enum type
     * @param cls the actual enum class
     */
    public static <T extends Enum<T>> void unregisterEnum(Class<T> cls) {
        process(cls, cls, false);
    }

    /**
     * Processes a {@code cls} in the interface hierarchy of class {@code reg} to be registered/unregistered. 
     * Recursively processes interfaces of {@code cls}.
     * 
     * @param cls a class of the interface hierarchy of {@code reg}
     * @param reg the enum class to be registered/unregistered
     * @param add add/register ({@code true}) or remove ({@code false})
     */
    private static void process(Class<?> cls, Class<?> reg, boolean add) {
        Set<Class<?>> compatible = ENUMS.get(cls);
        if (add) {
            if (null == compatible) {
                compatible = new HashSet<>();
                ENUMS.put(cls, compatible);
            }
            compatible.add(reg);
        } else {
            if (null != compatible) {
                compatible.remove(reg);
            }
        }
        for (Class<?> i : cls.getInterfaces()) {
            process(i, reg, add);
        }
    }
    
    /**
     * Returns the registered enums for {@code cls}.
     * 
     * @param <T> the type of the return value
     * @param <E> the enumeration type
     * @param cls the type to return the registered enums for
     * @param dflt the type to return in any case if type compatible with {@code cls}
     * @return the registered enumeration types
     */
    public static <T, E extends Enum<E>> Iterable<Class<?>> getEnums(Class<T> cls, Class<E> dflt) {
        Set<Class<?>> result = ENUMS.get(cls);
        if (null == result) {
            result = new HashSet<>();
            if (cls.isAssignableFrom(dflt)) {
                result.add(dflt);
            }
        } else if (!result.contains(dflt)) {
            result = new HashSet<>(result);
            if (cls.isAssignableFrom(dflt)) {
                result.add(dflt);
            }
        }
        return result;
    }

}
