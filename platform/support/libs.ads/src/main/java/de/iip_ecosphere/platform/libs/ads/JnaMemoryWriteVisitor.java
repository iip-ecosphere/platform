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

import java.math.BigInteger;

import com.sun.jna.Memory;

/**
 * Writes primitive data to a JNA memory.
 * 
 * @param <T> the type of data to read
 * @author Holger Eichelberger, SSE
 */
public abstract class JnaMemoryWriteVisitor<T> implements WriteVisitor<T> {
    
    private Memory mem;
    private long offset;
    
    /**
     * Creates a visitor instance.
     * 
     * @param mem the memory to write to
     */
    protected JnaMemoryWriteVisitor(Memory mem) {
        this.mem = mem;
    }
    
    @Override
    public void writeLReal(double value) {
        mem.setDouble(offset, value);
        offset += AdsCommunication.SIZE_DOUBLE;
    }

    @Override
    public void writeReal(float value) {
        mem.setFloat(offset, value);
        offset += AdsCommunication.SIZE_FLOAT;
    }
    
    @Override
    public void writeLInt(long value) {
        mem.setLong(offset, value);
        offset += AdsCommunication.SIZE_LONG;
    }
    
    @Override
    public void writeDInt(int value) {
        mem.setInt(offset, value);
        offset += AdsCommunication.SIZE_INT;
    }
    
    @Override
    public void writeUDInt(long value) {
        mem.setInt(offset, (int) value);
        offset += AdsCommunication.SIZE_INT;
    }
    
    
    @Override
    public void writeInt(short value) {
        mem.setShort(offset, value);
        offset += AdsCommunication.SIZE_SHORT;
    }
    
    @Override
    public void writeUInt(int value) {
        mem.setShort(offset, (short) value);
        offset += AdsCommunication.SIZE_SHORT;
    }
    
    @Override
    public void writeSInt(byte value) {
        mem.setByte(offset, value);
        offset += AdsCommunication.SIZE_BYTE;
    }
    
    @Override
    public void writeUSInt(short value) {
        mem.setByte(offset, (byte) value);
        offset += AdsCommunication.SIZE_BYTE;
    }
    
    @Override
    public void writeString(String value) {
        mem.setString(offset, value);
        offset += value.length() + 1;
    }
    
    
    
    @Override
    public void writeLRealArray(double[] value, int size) {
        mem.write(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void writeRealArray(float[] value, int size) {
        mem.write(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void writeULIntArray(BigInteger[] value, int size) {
        long[] temp = new long[value.length];
        for (int i = 0; i < value.length; i++) {
            temp[i] = value[i].longValue();
        }
        mem.write(offset, temp, 0, size);
        offset += size;
    }
    
    @Override
    public void writeLIntArray(long[] value, int size) {
        mem.write(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void writeUIntArray(int[] value, int size) {
        short[] temp = new short[value.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = (short) value[i];
        }
        mem.write(offset, temp, 0, size);
        offset += size;
    }
    
    @Override
    public void writeIntArray(short[] value, int size) {
        mem.write(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void writeSIntArray(byte[] value, int size) {
        mem.write(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void writeUDIntArray(long[] value, int size) {
        int[] temp = new int[value.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = (int) value[i];
        }
        mem.write(offset, temp, 0, size);
        offset += size;
    }

    @Override
    public void writeDIntArray(int[] value, int size) {
        mem.write(offset, value, 0, size);
        offset += size;
    }

    @Override
    public void writeUSIntArray(short[] value, int size) {
        byte[] temp = new byte[value.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = (byte) value[i];
        }
        mem.write(offset, temp, 0, size);
        offset += size;
    }
}