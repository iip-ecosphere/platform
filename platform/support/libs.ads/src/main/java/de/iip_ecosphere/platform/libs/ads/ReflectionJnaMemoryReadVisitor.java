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

import com.sun.jna.Memory;

/**
 * Writes an object to JNA memory via reflection. This class is supplied only for convenience. Type-specific 
 * implementations are recommended.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ReflectionJnaMemoryReadVisitor extends JnaMemoryReadVisitor<Object> {

    /**
     * Creates a memory read visitor.
     * 
     * @param mem the memory to write to
     */
    public ReflectionJnaMemoryReadVisitor(Memory mem) {
        super(mem);
    }
    
    @Override
    public void read(Object value) throws IOException {
        read(value, value.getClass());
    }

    /**
     * Reads {@code value}.
     * 
     * @param value the value to read
     * @param cls the type of the value
     * @throws IOException if the value cannot be read
     */
    private void read(Object value, Class<?> cls) throws IOException {
        if (cls != Object.class) {
            for (Field f : cls.getDeclaredFields()) {
                Class<?> type = f.getType();
                try {
                    if (type.isPrimitive()) {
                        if (type == Double.TYPE) {
                            f.setDouble(value, readLReal());
                        } else if (type == Long.TYPE) {
                            f.setLong(value, readLInt());
                        } else if (type == Integer.TYPE) {
                            f.setInt(value, readDInt());
                        } else if (type == Float.TYPE) {
                            f.setFloat(value, readReal());
                        } else if (type == Short.TYPE) {
                            f.setShort(value, readInt());
                        } else if (type == Byte.TYPE) {
                            f.setByte(value, readSInt());
                        } else {
                            throw new IOException("Cannot read value of type " + type.getName() 
                            + " for field " + f.getName());
                        }
                    } else if (type.isAssignableFrom(Number.class)) {
                        if (type == Double.class) {
                            f.setDouble(value, readLReal());
                        } else if (type == Long.class) {
                            f.setLong(value, readLInt());
                        } else if (type == Integer.class) {
                            f.setInt(value, readDInt());
                        } else if (type == Float.class) {
                            f.setFloat(value, readReal());
                        } else if (type == Short.class) {
                            f.setShort(value, readInt());
                        } else if (type == Byte.class) {
                            f.setByte(value, readSInt());
                        } else {
                            throw new IOException("Cannot read value of type " + type.getName() 
                                + " for field " + f.getName());
                        }
                    } else if (type.isArray()) {
                        readArray(f, type, value);
                    } else if (String.class == cls) {
                        f.setAccessible(true);
                        f.set(value, readString());
                    } else {
                        f.setAccessible(true);
                        read(f.get(value), type);
                    }
                } catch (IllegalAccessException e) {
                    System.out.println("Ignoring field " + f.getName() + ": " + e.getMessage());
                }
            }
            if (null != cls.getSuperclass() && cls.getSuperclass() != Object.class) {
                read(value, cls.getSuperclass());
            }
        }
    }
    
    /**
     * Reads an array value.
     * 
     * @param field the field
     * @param type the array type
     * @param value the array value 
     * @throws IllegalAccessException if the value cannot be accessed
     * @throws IOException if the value cannot be read
     */
    private void readArray(Field field, Class<?> type, Object value) throws IllegalAccessException, IOException {
        field.setAccessible(true);
        Object arr = field.get(value);
        int size = Array.getLength(arr);
        Class<?> compType = type.getComponentType();
        if (compType.isPrimitive()) {
            if (type == Double.TYPE) {
                readLRealArray((double[]) arr, size);
            } else if (type == Long.TYPE) {
                readLIntArray((long[]) arr, size);
            } else if (type == Integer.TYPE) {
                readDIntArray((int[]) arr, size);
            } else if (type == Float.TYPE) {
                readRealArray((float[]) arr, size);
            } else if (type == Short.TYPE) {
                readIntArray((short[]) arr, size);
            } else if (type == Byte.TYPE) {
                readSIntArray((byte[]) arr, size);
            }
        } else { // preliminary
            for (int i = 0; i < size; i++) { 
                read(Array.get(arr, i), compType);
            }
        }
    }
}
