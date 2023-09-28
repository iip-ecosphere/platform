package de.iip_ecosphere.platform.libs.ads;

import java.math.BigInteger;

import com.sun.jna.Memory;

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

/**
 * Reads primitive data from JNA memory.
 * 
 * @param <T> the type of data to read
 * @author Holger Eichelberger, SSE
 */
public abstract class JnaMemoryReadVisitor<T> implements ReadVisitor<T> {

    private Memory mem;
    private long offset;

    /**
     * Creates a visitor instance.
     * 
     * @param mem the memory to read from
     */
    protected JnaMemoryReadVisitor(Memory mem) {
        this.mem = mem;
    }
    
    @Override
    public double readLReal() {
        double result = mem.getDouble(offset);
        offset += AdsCommunication.SIZE_DOUBLE;
        return result;
    }
    
    @Override
    public float readReal() {
        float result = mem.getFloat(offset);
        offset += AdsCommunication.SIZE_FLOAT;
        return result;
    }
    
    @Override
    public BigInteger readULInt() {
        byte[] temp = new byte[8];
        mem.read(offset, temp, 0, temp.length);
        BigInteger result = new BigInteger(temp);
        offset += AdsCommunication.SIZE_LONG;
        return result;
    }
    
    @Override
    public long readLInt() {
        long result = mem.getLong(offset);
        offset += AdsCommunication.SIZE_LONG;
        return result;
    }
    
    @Override
    public int readDInt() {
        int result = mem.getInt(offset);
        offset += AdsCommunication.SIZE_INT;
        return result;
    }
    
    @Override
    public long readUDInt()  {
        long result = mem.getInt(0) & 0xffffffffL;
        offset += AdsCommunication.SIZE_INT;
        return result;
        
    }
    
    @Override
    public int readUInt() {
        int result = mem.getShort(0) & 0xffff;
        offset += AdsCommunication.SIZE_SHORT;
        return result;
    }
    
    @Override
    public short readInt() {
        short result = mem.getShort(offset);
        offset += AdsCommunication.SIZE_SHORT;
        return result;
    }
    
    @Override
    public short readUSInt() {
        short result = (short) (mem.getShort(offset) & 0xff);
        offset += AdsCommunication.SIZE_BYTE;
        return result;
    }
    
    
    @Override
    public byte readSInt() {
        byte result = mem.getByte(offset);
        offset += AdsCommunication.SIZE_BYTE;
        return result;
    }
    
    @Override
    public String readString() {
        String result = mem.getString(offset);
        offset += result.length() + 1; // +1?
        return result;
    }
    
    @Override
    public void readLRealArray(double[] value, int size) {
        mem.read(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void readRealArray(float[] value, int size) {
        mem.read(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void readLIntArray(long[] value, int size) {
        mem.read(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void readULIntArray(BigInteger[] value, int size) {
        long[] temp = new long[value.length];
        mem.read(offset, temp, 0, size);
        for (int i = 0; i < value.length; i++) {
            //Not Good, value of will likely return a negative value if we pass signed space
            value[i] = new BigInteger(String.valueOf(temp[i]));
        }
        offset += size;
    }
    
    @Override
    public void readDIntArray(int[] value, int size) {
        mem.read(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void readUDIntArray(long[] value, int size) {
        int[] temp = new int[value.length];
        mem.read(offset, temp, 0, size);
        for (int i = 0; i < temp.length; i++) {
            //Java assumens leading 0 for 0xffff (by default int range) 
            value[i] = ((long) (temp[i] & 0xffffffffL));
        }
    }
    
    @Override
    public void readUIntArray(int[] value, int size) {
        short[] temp = new short[value.length];
        mem.read(offset, temp, 0, size);
        for (int i = 0; i < temp.length; i++) {
            //Java assumens leading 0 for 0xffff (by default int range) 
            value[i] = ((int) (temp[i] & 0xffff));
        }
        offset += size;
    }
    
    @Override
    public void readIntArray(short[] value, int size) {
        mem.read(offset, value, 0, size);
        offset += size;
    }
    
    @Override
    public void readUSIntArray(short[] value, int size) {
        byte[] temp = new byte[value.length];
        mem.read(offset, temp, 0, size);
        
        for (int i = 0; i < temp.length; i++) {
            //Java assumens leading 0 for 0xffff (by default int range) 
            value[i] = ((short) (temp[i] & 0xff));
        }
        
        offset += size;
    }
    
    @Override
    public void readSIntArray(byte[] value, int size) {
        mem.read(offset, value, 0, size);
        offset += size;
    }

}
