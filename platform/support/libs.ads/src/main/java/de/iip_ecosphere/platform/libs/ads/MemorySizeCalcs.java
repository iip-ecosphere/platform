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

/**
 * Provides memory size calculators.
 * 
 * @author Alexander Weber, SSE
 * @author Holger Eichelberger, SSE
 */
public class MemorySizeCalcs {
    
    /**
     * For double arrays on the ADS.
     */
    private static MemorySizeCalculator<double[]> sizeCalculatorDoubleArray = new MemorySizeCalculator<double[]>() {
        
        @Override
        public int determineMemorySize(double[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_DOUBLE; 
            }
            return size;
        }
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_DOUBLE;
        }
        
    };
    
    /**
     * For float arrays on the ADS.
     */
    private static MemorySizeCalculator<float[]> sizeCalculatorFloatArray = new MemorySizeCalculator<float[]>() {
        
        @Override
        public int determineMemorySize(float[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_FLOAT; 
            }
            return size;
        }
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_FLOAT;
        }
        
    };
    
    /**
     * For Every 64bit integer value on the ADS.
     */
    private static MemorySizeCalculator<long[]> sizeCalculatorLongArray = new MemorySizeCalculator<long[]>() {
        
        @Override
        public int determineMemorySize(long[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_LONG; 
            }
            return size;
        }
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_LONG;
        }
        
    };
    
    /**
     * For Every 64bit integer value on the ADS for values over LInt.
     */
    private static MemorySizeCalculator<BigInteger[]> sizeCalculatorBigIntArray = new 
        MemorySizeCalculator<BigInteger[]>() {
            
        @Override
        public int determineMemorySize(BigInteger[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_LONG; 
            }
            return size;
        } 
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_LONG;
        }
        
    };
    
    /**
     * For every 32 bit integer type array.
     */
    private static MemorySizeCalculator<int[]> sizeCalculatorDIntArray = new MemorySizeCalculator<int[]>() {
        
        @Override
        public int determineMemorySize(int[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_INT; 
            }
            return size;
        }
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_INT;
        }
        
    };
    
    /**
     * For every 32 bit integer type array.
     */
    private static MemorySizeCalculator<long[]> sizeCalculatorUDIntArray = new MemorySizeCalculator<long[]>() {
        
        @Override
        public int determineMemorySize(long[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_INT; 
            }
            return size;
        } 
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_INT;
        }
        
    };
    
    /**
     * For every 16 bit integer type array.
     */
    private static MemorySizeCalculator<short[]> sizeCalculatorShortArray = new MemorySizeCalculator<short[]>() {
        
        @Override
        public int determineMemorySize(short[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_SHORT; 
            }
            return size;
        } 
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_SHORT;
        }
        
    };
    
    
    /**
     * For every 16 bit integer type array represented in java by a bigger type.
     */
    private static MemorySizeCalculator<int[]> sizeCalculatorUIntArray = new MemorySizeCalculator<int[]>() {
        
        @Override
        public int determineMemorySize(int[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_SHORT; 
            }
            return size;
        } 
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_SHORT;
        }
        
    };
    
    /**
     * For every 16 bit integer type array represented in java by a bigger type.
     */
    private static MemorySizeCalculator<byte[]> sizeCalculatorSIntArray = new MemorySizeCalculator<byte[]>() {
        
        @Override
        public int determineMemorySize(byte[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_BYTE; 
            }
            return size;
        } 
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_BYTE;
        }
        
    };
    
    /**
     * For every 16 bit integer type array represented in java by a bigger type.
     */
    private static MemorySizeCalculator<short[]> sizeCalculatorUSIntArray = new MemorySizeCalculator<short[]>() {
        
        @Override
        public int determineMemorySize(short[] value) throws IOException {
            int size = 0;
            for (int i = 0; i < value.length; i++) {
                size += AdsCommunication.SIZE_BYTE; 
            }
            return size;
        } 
        
        @Override
        public int getMemSize() {
            return AdsCommunication.SIZE_BYTE;
        }
        
    };
    
    /**
     * Gets MemorySize Calculator for double arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<double[]> getSizeCalculatorDoubleArray() {
        return sizeCalculatorDoubleArray;
    }
    
    /**
     * Gets MemorySize Calculator float arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<float[]> getSizeCalculatorFloatArray() {
        return sizeCalculatorFloatArray;
    }
    
    /**
     * Gets MemorySize Calculator float arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<long[]> getSizeCalculatorLongArray() {
        return sizeCalculatorLongArray;
    }
    
    /**
     * Gets MemorySize Calculator for BigInt Arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<BigInteger[]> getSizeCalculatorBigIntArray() {
        return sizeCalculatorBigIntArray;
    }
    
    /**
     * Gets MemorySize Calculator for Int Arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<int[]> getSizeCalculatorIntArray() {
        return sizeCalculatorDIntArray;
    }
    
    /**
     * Gets MemorySize Calculator for long Arrays that need to be written to 32bit UDInt or DWord.
     * There is no Check if the value of the long actually fits into UDInt size!
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<long[]> getSizeCalculatorUDIntArray() {
        return sizeCalculatorUDIntArray;
    }
    
    /**
     * Gets MemorySize Calculator for Int Arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<short[]> getSizeCalculatorShortArray() {
        return sizeCalculatorShortArray;
    }
    
    /**
     * Gets MemorySize Calculator for int Arrays that need to be written to 16bit UInt or Word.
     * There is no Check if the value of the long actually fits into UInt size!
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<int[]> getSizeCalculatorUIntArray() {
        return sizeCalculatorUIntArray;
    }
    
    /**
     * Gets MemorySize Calculator for byte Arrays.
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<byte[]> getSizeCalculatorSIntArray() {
        return sizeCalculatorSIntArray;
    }
    
    /**
     * Gets MemorySize Calculator for short Arrays that need to be written to 8bit USInt or SWord.
     * There is no Check if the value of the long actually fits into USInt size!
     * 
     * @return the Size Calculator.
     */
    public static MemorySizeCalculator<short[]> getSizeCalculatorUSIntArray() {
        return sizeCalculatorUSIntArray;
    }
    
}
