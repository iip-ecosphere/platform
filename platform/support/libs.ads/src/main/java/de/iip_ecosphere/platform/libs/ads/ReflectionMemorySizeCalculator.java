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

package de.iip_ecosphere.platform.libs.ads;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflection-based memory size calculator. This class is supplied only for convenience. Type-specific implementations 
 * are recommended.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ReflectionMemorySizeCalculator implements MemorySizeCalculator<Object> {
    
    private static final Map<Class<?>, Integer> MEM_SIZES = new HashMap<>();
    
    static {
        MEM_SIZES.put(Double.TYPE, AdsCommunication.SIZE_DOUBLE);
        MEM_SIZES.put(Double.class, AdsCommunication.SIZE_DOUBLE);
        MEM_SIZES.put(Float.TYPE, AdsCommunication.SIZE_FLOAT);
        MEM_SIZES.put(Float.class, AdsCommunication.SIZE_FLOAT);
        MEM_SIZES.put(Long.TYPE, AdsCommunication.SIZE_LONG);
        MEM_SIZES.put(Long.class, AdsCommunication.SIZE_LONG);
        MEM_SIZES.put(Integer.TYPE, AdsCommunication.SIZE_INT);
        MEM_SIZES.put(Integer.class, AdsCommunication.SIZE_INT);
        MEM_SIZES.put(Short.TYPE, AdsCommunication.SIZE_SHORT);
        MEM_SIZES.put(Short.class, AdsCommunication.SIZE_SHORT);
        MEM_SIZES.put(Byte.TYPE, AdsCommunication.SIZE_BYTE);
        MEM_SIZES.put(Byte.class, AdsCommunication.SIZE_BYTE);
    }
    
    @Override
    public int determineMemorySize(Object value) throws IOException {
        return determineMemorySize(value, value.getClass());
    }
    
    /**
     * Determines the ADS memory size of {@code value} with type {@code cls}.
     *  
     * @param value the value to determine the memory size for
     * @param cls the type of {@code value}
     * @return the memory size
     * @throws IOException if value contains elements that cannot be written
     */
    private int determineMemorySize(Object value, Class<?> cls) throws IOException {
        int size = 0;
        if (cls != Object.class) {
            for (Field f : cls.getDeclaredFields()) {
                Class<?> type = f.getType();
                try {
                    Integer tmp = MEM_SIZES.get(type);
                    if (null != tmp) {
                        size += tmp;
                    } else if (type.isArray()) {
                        f.setAccessible(true);
                        int compTypeSize = determineMemorySize(type.getComponentType());
                        size += Array.getLength(f.get(value)) * compTypeSize;
                    } else if (String.class == cls) {
                        f.setAccessible(true);
                        size += f.get(value).toString().length() + 1;
                    } else {
                        f.setAccessible(true);
                        size += determineMemorySize(f.get(value), type);
                    }
                } catch (IllegalAccessException e) {
                    System.out.println("Ignoring field " + f.getName() + ": " + e.getMessage());
                }
            }
            if (null != cls.getSuperclass() && cls.getSuperclass() != Object.class) {
                size += determineMemorySize(value, cls.getSuperclass());
            }
        }
        return size;
    }

    @Override
    public int getMemSize() {
        return 0;
    }
    
}