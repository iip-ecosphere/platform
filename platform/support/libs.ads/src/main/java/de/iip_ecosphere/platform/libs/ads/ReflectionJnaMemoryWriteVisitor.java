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
public class ReflectionJnaMemoryWriteVisitor extends JnaMemoryWriteVisitor<Object> {

    /**
     * Creates a memory write visitor.
     * 
     * @param mem the memory to write to
     */
    public ReflectionJnaMemoryWriteVisitor(Memory mem) {
        super(mem);
    }

    @Override
    public void write(Object value) throws IOException {
        write(value, value.getClass());
    }

    /**
     * Writes a value.
     * 
     * @param value the value
     * @param cls the type of the value
     * @throws IOException if the value cannot be written
     */
    private void write(Object value, Class<?> cls) throws IOException {
        if (cls != Object.class) {
            for (Field f : cls.getDeclaredFields()) {
                Class<?> type = f.getType();
                try {
                    if (type.isPrimitive()) {
                        if (type == Double.TYPE) {
                            writeLReal(f.getDouble(value));
                        } else if (type == Long.TYPE) {
                            writeLInt(f.getLong(value));
                        } else if (type == Integer.TYPE) {
                            writeDInt(f.getInt(value));
                        } else if (type == Float.TYPE) {
                            writeReal(f.getFloat(value));
                        } else if (type == Short.TYPE) {
                            writeInt(f.getShort(value));
                        } else if (type == Byte.TYPE) {
                            writeSInt(f.getByte(value));
                        } else {
                            throw new IOException("Cannot write value of type " + type.getName() 
                            + " for field " + f.getName());
                        }
                    } else if (type.isAssignableFrom(Number.class)) {
                        if (type == Double.class) {
                            writeLReal(f.getDouble(value));
                        } else if (type == Long.class) {
                            writeLInt(f.getLong(value));
                        } else if (type == Integer.class) {
                            writeDInt(f.getInt(value));
                        } else if (type == Float.class) {
                            writeReal(f.getFloat(value));
                        } else if (type == Short.class) {
                            writeInt(f.getShort(value));
                        } else if (type == Byte.class) {
                            writeSInt(f.getByte(value));
                        } else {
                            throw new IOException("Cannot write value of type " + type.getName() 
                            + " for field " + f.getName());
                        }
                    } else if (type.isArray()) {
                        writeArray(f, type, value);
                    } else if (String.class == cls) {
                        f.setAccessible(true);
                        writeString(f.get(value).toString());
                    } else {
                        f.setAccessible(true);
                        write(f.get(value), type);
                    }
                } catch (IllegalAccessException e) {
                    System.out.println("Ignoring field " + f.getName() + ": " + e.getMessage());
                }
            }
            if (null != cls.getSuperclass() && cls.getSuperclass() != Object.class) {
                write(value, cls.getSuperclass());
            }
        }
    }
    
    /**
     * Writes an array value.
     * 
     * @param field the field
     * @param type the array type
     * @param value the array value 
     * @throws IllegalAccessException if the value cannot be accessed
     * @throws IOException if the value cannot be written
     */
    private void writeArray(Field field, Class<?> type, Object value) throws IllegalAccessException, IOException {
        field.setAccessible(true);
        Object arr = field.get(value);
        int size = Array.getLength(arr);
        Class<?> compType = type.getComponentType();
        if (compType.isPrimitive()) {
            if (type == Double.TYPE) {
                writeLRealArray((double[]) arr, size);
            } else if (type == Long.TYPE) {
                writeLIntArray((long[]) arr, size);
            } else if (type == Integer.TYPE) {
                writeUIntArray((int[]) arr, size);
            } else if (type == Float.TYPE) {
                writeRealArray((float[]) arr, size);
            } else if (type == Short.TYPE) {
                writeSIntArray((byte[]) arr, size);
            }
        } else { // preliminary
            for (int i = 0; i < size; i++) { 
                write(Array.get(arr, i), compType);
            }
        }
    }

    @Override
    public void writeUDIntArray(long[] value, int size) {
        // TODO implement
    }

    @Override
    public void writeDIntArray(int[] value, int size) {
        // TODO implement
    }

    @Override
    public void writeSIntArray(byte[] value, int size) {
        // TODO implement
    }

    @Override
    public void writeUSIntArray(short[] value, int size) {
        // TODO implement
    }

}
