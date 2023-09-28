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
import java.math.BigInteger;

import com.sun.jna.Memory;

/**
 * Writes data into memory for transfer to ADS.
 * 
 * @param <T> the type of the data
 * @author Holger Eichelberger, SSE
 * @author Alexander Weber, SSE
 */
public interface WriteVisitor<T> {

    /**
     * Supplies a write visitor.
     * 
     * @param <T> the type of the data
     * @author Holger Eichelberger, SSE
     */
    public interface WriteVisitorSupplier<T> {
     
        /**
         * Creates a write visitor.
         * 
         * @param mem the memory to operate on
         * @return the write visitor
         */
        public WriteVisitor<T> create(Memory mem);
        
    }
    
    /**
     * Writes an object.
     * 
     * @param value the object
     * @throws IOException if the value cannot be written
     */
    public void write(T value) throws IOException;
    
    /**
     * Writes a signed double value.
     * 
     * @param value the value
     */
    public void writeLReal(double value);
    
    /**
     * Writes a signed float value.
     * 
     * @param value the value
     */
    public void writeReal(float value);
    
    /**
     * Writes a signed long value.
     * 
     * @param value the value
     */
    public void writeLInt(long value);
    
    /**
     * Writes a signed int value.
     * 
     * @param value the value
     */
    public void writeDInt(int value);
    
    /**
     * Writes a signed int value.
     * 
     * @param value the value
     */
    public void writeUDInt(long value);
    
    /**
     * Writes a signed short value.
     * 
     * @param value the value
     */
    public void writeInt(short value);
    
    /**
     * Writes a signed short value.
     * 
     * @param value the value
     */
    public void writeUInt(int value);
    
    /**
     * Writes a signed byte value.
     * 
     * @param value the value
     */
    public void writeSInt(byte value);
    
    /**
     * Writes a signed byte value.
     * 
     * @param value the value
     */
    public void writeUSInt(short value);
    
    /**
     * Writes a String value.
     * 
     * @param value the value
     */
    public void writeString(String value);

    /**
     * Writes a signed LReal (64Bit) array.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeLRealArray(double[] value, int size);
    
    /**
     * Writes a signed Real (32Bit) array.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeRealArray(float[] value, int size);
    
    /**
     * Writes a signed/unsigned LInt (64Bit) or LWord array .
     * Issues with handling unsigned lInt values in Java as there is no primitive able to.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeLIntArray(long[] value, int size);
    
    /**
     * Writes a unsigned UInt (32Bit) or DWord array.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeUDIntArray(long[] value, int size);
   
    /**
     * Writes a signed DInt(32Bit) array.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeDIntArray(int[] value, int size);
    
    /**
     * Writes a unsigned int (16Bit) array value.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeUIntArray(int[] value, int size);
    
    /**
     * Writes a signed int (16Bit) array value.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeIntArray(short[] value, int size);
    
    /**
     * Writes a signed SInt (8Bit) array value.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeSIntArray(byte[] value, int size);
    
    /**
     * Writes a unsigned USInt (8Bit) array value.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeUSIntArray(short[] value, int size);

    /**
     * Writes a unsigned USInt (8Bit) array value.
     * 
     * @param value the value
     * @param size of the array to write (may be PLC limited)
     */
    public void writeULIntArray(BigInteger[] value, int size);

}