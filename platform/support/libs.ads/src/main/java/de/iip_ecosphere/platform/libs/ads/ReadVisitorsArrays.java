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

import de.iip_ecosphere.platform.libs.ads.ReadVisitor.ReadVisitorSupplier;

/**
 * Provides read visitors for arrays.
 * 
 * @author Alexander Weber, SSE
 */
public class ReadVisitorsArrays {

    /**
     * Reader for LReal Arrays.
     */
    private static ReadVisitorSupplier<double[]> readerDouble = new ReadVisitorSupplier<double[]>() {
        @Override
        public ReadVisitor<double[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<double[]>(mem) {
                @Override
                public void read(double[] value) throws IOException {
                    readLRealArray((double[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for Real Arrays.
     */
    private static ReadVisitorSupplier<float[]> readerFloat = new ReadVisitorSupplier<float[]>() {
        @Override
        public ReadVisitor<float[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<float[]>(mem) {
                @Override
                public void read(float[] value) throws IOException {
                    readRealArray((float[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for LInt Arrays.
     */
    private static ReadVisitorSupplier<long[]> readerLIntArray = new ReadVisitorSupplier<long[]>() {
        @Override
        public ReadVisitor<long[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<long[]>(mem) {
                @Override
                public void read(long[] value) throws IOException {
                    readLIntArray((long[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for ULInt Arrays.
     */
    private static ReadVisitorSupplier<BigInteger[]> readerULIntArray = new ReadVisitorSupplier<BigInteger[]>() {
        @Override
        public ReadVisitor<BigInteger[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<BigInteger[]>(mem) {
                @Override
                public void read(BigInteger[] value) throws IOException {
                    readULIntArray((BigInteger[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for DInt or DWord Arrays.
     */
    private static ReadVisitorSupplier<int[]> readerDIntArray = new ReadVisitorSupplier<int[]>() {
        @Override
        public ReadVisitor<int[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<int[]>(mem) {
                @Override
                public void read(int[] value) throws IOException {
                    readDIntArray((int[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for UDInt or DWord Arrays.
     */
    private static ReadVisitorSupplier<long[]> readerUDIntArray = new ReadVisitorSupplier<long[]>() {
        @Override
        public ReadVisitor<long[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<long[]>(mem) {
                @Override
                public void read(long[] value) throws IOException {
                    readUDIntArray((long[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for Int Arrays.
     */
    private static ReadVisitorSupplier<short[]> readerIntArray = new ReadVisitorSupplier<short[]>() {
        @Override
        public ReadVisitor<short[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<short[]>(mem) {
                @Override
                public void read(short[] value) throws IOException {
                    readIntArray((short[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for Int Arrays.
     */
    private static ReadVisitorSupplier<int[]> readerUIntArray = new ReadVisitorSupplier<int[]>() {
        @Override
        public ReadVisitor<int[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<int[]>(mem) {
                @Override
                public void read(int[] value) throws IOException {
                    readUIntArray((int[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for SInt Arrays.
     */
    private static ReadVisitorSupplier<byte[]> readerSIntArray = new ReadVisitorSupplier<byte[]>() {
        @Override
        public ReadVisitor<byte[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<byte[]>(mem) {
                @Override
                public void read(byte[] value) throws IOException {
                    readSIntArray((byte[]) value, value.length);
                }
            };
        }
    };
    
    /**
     * Reader for USInt Arrays.
     */
    private static ReadVisitorSupplier<short[]> readerUSIntArray = new ReadVisitorSupplier<short[]>() {
        @Override
        public ReadVisitor<short[]> create(Memory mem) {   
            return new JnaMemoryReadVisitor<short[]>(mem) {
                @Override
                public void read(short[] value) throws IOException {
                    readUSIntArray((short[]) value, value.length);
                }
            };
        }
    };
    
    /** 
     * A reader for arrays with 64 bit floating point values.
     * @return the reader
     */
    public static ReadVisitorSupplier<double[]> getReaderDouble() {
        return readerDouble;
    }
    /**
     * A reader for arrays with 32 bit floating point values.
     * @return the reader
     */
    public static ReadVisitorSupplier<float[]> getReaderFloat() {
        return readerFloat;
    }
    /**
     * A reader for arrays with 64 bit integer values.
     * @return the reader
     */
    public static ReadVisitorSupplier<long[]> getReaderLIntArray() {
        return readerLIntArray;
    }
    /**
     * A reader for arrays with 64 bit integer values over the limit of long (ULInt or LWord).
     * @return the reader
     */
    public static ReadVisitorSupplier<BigInteger[]> getReaderULIntArray() {
        return readerULIntArray;
    }
    /**
     * A reader for arrays with 32 bit integer values int(DInt or DWord).
     * @return the reader
     */
    public static ReadVisitorSupplier<int[]> getReaderDIntArray() {
        return readerDIntArray;
    }
    
    /**
     * A reader for arrays with 32 bit integer values over the limit of int (UDInt or DWord).
     * @return the reader
     */
    public static ReadVisitorSupplier<long[]> getReaderUDIntArray() {
        return readerUDIntArray;
    }
    /**
     * A reader for arrays with 16 bit integer values short(Int).
     * @return the reader
     */
    public static ReadVisitorSupplier<short[]> getReaderIntArray() {
        return readerIntArray;
    }
    /**
     * A reader for arrays with 16 bit integer values over the limit of short (UInt or Word).
     * @return the reader
     */
    public static ReadVisitorSupplier<int[]> getReaderUIntArray() {
        return readerUIntArray;
    }
    
    /**
     * A reader for arrays with 8 bit integer values byte.
     * @return the reader
     */
    public static ReadVisitorSupplier<byte[]> getReaderSIntArray() {
        return readerSIntArray;
    }
    
    /**
     * A reader for arrays with 8 bit integer values over the limit of byte (USInt or sWord).
     * @return the reader
     */
    public static ReadVisitorSupplier<short[]> getReaderUSIntArray() {
        return readerUSIntArray;
    }
}
