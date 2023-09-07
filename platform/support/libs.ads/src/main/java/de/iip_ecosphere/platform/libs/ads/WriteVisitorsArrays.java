/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
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

import de.iip_ecosphere.platform.libs.ads.WriteVisitor.WriteVisitorSupplier;

/**
 * Provides write visitors for arrays.
 * 
 * @author Alexander Weber, SSE
 */
public class WriteVisitorsArrays {
    
    /**
     * Write for LReal Arrays.
     */
    private static WriteVisitorSupplier<double[]> writeSupplierLReal = new WriteVisitorSupplier<double[]>() {
        @Override
        public WriteVisitor<double[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<double[]>(mem) {
                @Override
                public void write(double[] value) throws IOException {
                    writeLRealArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for LReal Arrays.
     */
    private static WriteVisitorSupplier<float[]> writeSupplierReal = new WriteVisitorSupplier<float[]>() {
        @Override
        public WriteVisitor<float[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<float[]>(mem) {
                @Override
                public void write(float[] value) throws IOException {
                    writeRealArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for LInt Arrays.
     */
    private static WriteVisitorSupplier<long[]> writeSupplierLInt = new WriteVisitorSupplier<long[]>() {
        @Override
        public WriteVisitor<long[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<long[]>(mem) {
                @Override
                public void write(long[] value) throws IOException {
                    writeLIntArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for ULInt Arrays.
     */
    private static WriteVisitorSupplier<BigInteger[]> writeSupplierULInt = new WriteVisitorSupplier<BigInteger[]>() {
        @Override
        public WriteVisitor<BigInteger[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<BigInteger[]>(mem) {
                @Override
                public void write(BigInteger[] value) throws IOException {
                    writeULIntArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for DInt Arrays.
     */
    private static WriteVisitorSupplier<int[]> writeSupplierDInt = new WriteVisitorSupplier<int[]>() {
        @Override
        public WriteVisitor<int[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<int[]>(mem) {
                @Override
                public void write(int[] value) throws IOException {
                    writeDIntArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for UDInt and DWordsArrays.
     */
    private static WriteVisitorSupplier<long[]> writeSupplierUDInt = new WriteVisitorSupplier<long[]>() {
        @Override
        public WriteVisitor<long[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<long[]>(mem) {
                @Override
                public void write(long[] value) throws IOException {
                    writeUDIntArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for Int and DWordsArrays.
     */
    private static WriteVisitorSupplier<short[]> writeSupplierInt = new WriteVisitorSupplier<short[]>() {
        @Override
        public WriteVisitor<short[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<short[]>(mem) {
                @Override
                public void write(short[] value) throws IOException {
                    writeIntArray(value, value.length);
                }
            };
        }
    };
    
    
    /**
     * Write for UInt and Words Arrays.
     */
    private static WriteVisitorSupplier<int[]> writeSupplierUInt = new WriteVisitorSupplier<int[]>() {
        @Override
        public WriteVisitor<int[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<int[]>(mem) {
                @Override
                public void write(int[] value) throws IOException {
                    writeUIntArray(value, value.length);
                }
            };
        }
    };
    
    
    /**
     * Write for UInt and Words Arrays.
     */
    private static WriteVisitorSupplier<byte[]> writeSupplierSInt = new WriteVisitorSupplier<byte[]>() {
        @Override
        public WriteVisitor<byte[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<byte[]>(mem) {
                @Override
                public void write(byte[] value) throws IOException {
                    writeSIntArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Write for UInt and Words Arrays.
     */
    private static WriteVisitorSupplier<short[]> writeSupplierUSInt = new WriteVisitorSupplier<short[]>() {
        @Override
        public WriteVisitor<short[]> create(Memory mem) {           
            return new JnaMemoryWriteVisitor<short[]>(mem) {
                @Override
                public void write(short[] value) throws IOException {
                    writeUSIntArray(value, value.length);
                }
            };
        }
    };
    
    /**
     * Writing 64 bit floating point values to the ADS system. As LReal.
     * @return the Writer
     */
    public static WriteVisitorSupplier<double[]> getWriteSupplierLReal() {
        return writeSupplierLReal;
    }
    
    /**
     * Writing 32 bit floating point values to the ADS system. As Real.
     * @return the Writer
     */
    public static WriteVisitorSupplier<float[]> getWriteSupplierReal() {
        return writeSupplierReal;
    }
    
    /**
     * Writing 64 bit integer values to the ADS system. As LInt.
     * @return the Writer
     */    
    public static WriteVisitorSupplier<long[]> getWriteSupplierLInt() {
        return writeSupplierLInt;
    }
    
    /**
     * Writing 64 bit integer values to the ADS system that go beyond limits of long. As LInt.
     * @return the Writer
     */  
    public static WriteVisitorSupplier<BigInteger[]> getWriteSupplierULInt() {
        return writeSupplierULInt;
    }
    
    /**
     * Writing 32 bit integer values to the ADS system as DInt.
     * @return the Writer
     */  
    public static WriteVisitorSupplier<int[]> getWriteSupplierDInt() {
        return writeSupplierDInt;
    }
    
    /**
     * Writing 32 bit integer values to the ADS system that go beyond limits of int. As UDInt or DWord.
     * @return the Writer
     */  
    public static WriteVisitorSupplier<long[]> getWriteSupplierUDInt() {
        return writeSupplierUDInt;
    }
    
    /**
     * Writing 16 bit integer values to the ADS system as Int or.
     * @return the Writer
     */ 
    public static WriteVisitorSupplier<short[]> getWriteSupplierInt() {
        return writeSupplierInt;
    }
    
    /**
     * Writing 16 bit integer values to the ADS system that go beyond limits of short. As UInt or Word.
     * @return the Writer
     */  
    public static WriteVisitorSupplier<int[]> getWriteSupplierUInt() {
        return writeSupplierUInt;
    }
    
    /**
     * Writing 8 bit integer values to the ADS system as byte or.
     * @return the Writer
     */ 
    public static WriteVisitorSupplier<byte[]> getWriteSupplierSInt() {
        return writeSupplierSInt;
    }
    
    /**
     * Writing 16 bit short values to the ADS system as byte with out of limit of a signed byte.
     * @return the Writer
     */ 
    public static WriteVisitorSupplier<short[]> getWriteSupplierUSInt() {
        return writeSupplierUSInt;
    }
    
}
