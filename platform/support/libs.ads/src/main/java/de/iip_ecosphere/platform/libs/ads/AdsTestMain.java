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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.sun.jna.Memory;

import de.iip_ecosphere.platform.libs.ads.ReadVisitor.ReadVisitorSupplier;
import de.iip_ecosphere.platform.libs.ads.WriteVisitor.WriteVisitorSupplier;

/**
 * Performance test for ADS library. So far, cannot be moved into the test part.
 * 
 * @author Holger Eichelberger, SSE
 * @author Alexander Weber, SSE
 */
public class AdsTestMain {

    // ADS communication: Default port is 851, If you run the sever on the same host as the client
    // the ADS_Adress should be the same as defined here. If not you can change it directly in the code
    // or (better option) use the load settings function with a settings JSON
    //private static String adsAddress = "127.0.0.1.1.1";
    private static int adsPort = 851;
    private static String adsAddress = "169.254.214.106.1.1"; //Address for the IFW tests
    
    private static final int RUNS = 1;
    
    private static final int NORMALIZE_TO_SECONDS = 1000000000;
    
    private static final String GVL_NAME = "GVL_ADStest."; 
    //private static final String GVL_NAME = "GVL_ADAS."; 
    
    private static final boolean TEST_OUTPUT = false;
    
    private static File startEnd = new File("times.txt");
    private static File measurementGrp = new File("measurement.txt");
    private static File singleMeasurement = new File("measurementSingle.txt");
    
    private static AdsCommunication communicator;
    
    /**
     * Simple main program, similar to Svenja's but emitting two values - not sure whether long double works here.
     * 
     * @param args ignored
     * @throws IOException if the communication fails (rather harsh, but ok for now
     */
    public static void main(String[] args) throws IOException {
        communicator = new AdsCommunication(adsAddress, adsPort);
        System.out.println("Initialize ADS Communication");
        communicator.initCommunication();
        
        if (!measurementGrp.exists()) {
            measurementGrp.createNewFile();
        }
        if (!singleMeasurement.exists()) {
            singleMeasurement.createNewFile();
        }
        if (!startEnd.exists()) {
            startEnd.createNewFile();
        }
        
//        System.out.println(communicator.readRealByName("GVL_ADAS.rTest"));
//        System.out.println(communicator.readLIntByName("GVL_ADAS.liTest"));
//        System.out.println(communicator.readDIntByName("GVL_ADAS.diTest"));
//        System.out.println(communicator.readUDIntByName("GVL_ADAS.udiTest"));
//        System.out.println(communicator.readIntByName("GVL_ADAS.iTest"));
//        System.out.println(communicator.readUIntByName("GVL_ADAS.uiTest"));
//        System.out.println(communicator.readUSIntByName("GVL_ADAS.usiTest"));
//        System.out.println(communicator.readULIntByName("GVL_ADAS.uliTest"));
//        
//        testReadSimpleLRealArray(communicator);
//        testReadSimpleDIntArray(communicator);
//        testReadSimpleIntArray(communicator);
//        
//        int writeTest = 1845325825;
//        long writeDIntTest = 3845325825L;
//        short usintWriteTest = 245;
//        long writeLIntTest = 6845325855L;
//        short writeIntTest = 3333;
//        int writeUIntTest = 56666;
//        byte writeSIntTest = 126;
//        
//        communicator.writeLIntByName("GVL_ADAS.liTest", writeLIntTest);
//        communicator.writeDIntByName("GVL_ADAS.diTest", writeTest);
//        communicator.writeUDIntByName("GVL_ADAS.udiTest", writeDIntTest);
//        communicator.writeIntByName("GVL_ADAS.iTest", writeIntTest);
//        communicator.writeUIntByName("GVL_ADAS.uiTest", writeUIntTest);
//        communicator.writeSIntByName("GVL_ADAS.siTest", writeSIntTest);
//        communicator.writeUSIntByName("GVL_ADAS.usiTest", usintWriteTest);
//        
//        testWriteUIntArrayToADS(communicator);
//        int[] values = new int[3];
//        communicator.readStructByNameSimple("GVL_ADAS.aDIntCounter", values
//                , MemorySizeCalcs.getSizeCalculatorIntArray(), ReadVisitorsArrays.getReaderDIntArray());
        System.out.println("Performing read speed test:");
        
        writeTimestamp();
        
        for (int i = 0; i < 10; i++) {
            testSingleValueReadsSpeeds();
            testSingleValueWriteSpeeds();
            testArrayValuesReadSpeeds();
            testArrayValuesWriteSpeeds();
        }
        
        writeTimestamp();
        communicator.close();

    }
    /**
     * All Read Tests for the single values.
     * @throws IOException
     */
    public static void testSingleValueReadsSpeeds() throws IOException {
        testLRealR();
        testRealR();
        testLIntR();
        testULIntR();
        testLWordR();
        testDIntR();
        testUDIntR();
        testDWordR();
        testIntR();
        testUIntR();
        testWordR();
        testSIntR();
        testUSIntR();
        testBR();
    }
    /**
     * all Single value writing measurements.
     * @throws IOException 
     */
    public static void testSingleValueWriteSpeeds() throws IOException {
        testLRealW();
        testRealW();
        testLIntW();
        testULIntW();
        testLWordW();
        testDIntW();
        testUDIntW();
        testDWordW();
        testIntW();
        testUIntW();
        testWordW();
        testSIntW();
        testUSIntW();
        testBW();
    }
    /**
     * All test for reading arrays.
     * @throws IOException 
     */
    public static void testArrayValuesReadSpeeds() throws IOException {
        timeReadLRealArray();
        timeReadRealArray();
        timeReadLIntArray();
        timeReadULIntArray();
        timeReadLWordArray();
        timeReadDIntArray();
        timeReadUDIntArray();
        timeReadDWordArray();
        timeReadIntArray();
        timeReadUIntArray();
        timeReadWordArray();
        timeReadSIntArray();
        timeReadUSIntArray();
        timeReadByteArray();
    }
    /**
     * all Array writing Tests.
     * @throws IOException
     */
    public static void testArrayValuesWriteSpeeds() throws IOException {
        timeWriteLRealArray();
        timeWriteRealArray();
        timeWriteLIntArray();
        timeWriteULIntArray();
        timeWriteLWordArray();
        timeWriteDIntArray();
        timeWriteUDIntArray();
        timeWriteDWordArray();
        timeWriteIntArray();
        timeWriteUIntArray();
        timeWriteWordArray();
        timeWriteSIntArray();
        timeWriteUSIntArray();
        timeWriteByteArray();
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testLRealR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readLRealByName(GVL_NAME + "lRTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("lreal", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testLRealW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeLRealByName(GVL_NAME + "lRTest", 1222345343425462.2434);
        }
        long diff = System.nanoTime() - start;
        writeOutput("lreal", "w", diff);
    }
    
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testRealR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readRealByName(GVL_NAME + "rTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("real", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype. Writing seems to be an issue? this ends up as 1.2243454E+10
     * scheint hart gerundet...?
     * @throws IOException if ads not answering.
     */
    public static void testRealW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeRealByName(GVL_NAME + "rTest", 12243453935.34f);
        }
        long diff = System.nanoTime() - start;
        writeOutput("real", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testLWordR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readLIntByName(GVL_NAME + "lWTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("lword", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testLWordW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeLIntByName(GVL_NAME + "lWTest", 1222345343425462L);
        }
        long diff = System.nanoTime() - start;
        writeOutput("lword", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testDWordR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readUDIntByName(GVL_NAME + "dWTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("dword", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testDWordW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeUDIntByName(GVL_NAME + "dWTest", 122234534);
        }
        long diff = System.nanoTime() - start;
        writeOutput("dword", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testWordR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readUIntByName(GVL_NAME + "wTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("word", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testWordW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeUIntByName(GVL_NAME + "wTest", (short) 53134);
        }
        long diff = System.nanoTime() - start;
        writeOutput("word", "w", diff);
    }
    
    /**
     * Test run of the byte Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testBR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readUSIntByName(GVL_NAME + "bTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("byte", "r", diff);
    }
    
    /**
     * Test run of the byte Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testBW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeSIntByName(GVL_NAME + "bTest", (byte) 156);
        }
        long diff = System.nanoTime() - start;
        writeOutput("byte", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testLIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readLIntByName(GVL_NAME + "liTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("lint", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testLIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeLIntByName(GVL_NAME + "liTest", 1222345343425462L);
        }
        long diff = System.nanoTime() - start;
        writeOutput("lint", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testULIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readULIntByName(GVL_NAME + "uliTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("ulint", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testULIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeULIntByName(GVL_NAME + "uliTest", new BigInteger("1222345343425462436"));
        }
        long diff = System.nanoTime() - start;
        writeOutput("ulint", "w", diff);
    }

    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testDIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readDIntByName(GVL_NAME + "diTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("dint", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testDIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeDIntByName(GVL_NAME + "diTest", 122234534);
        }
        long diff = System.nanoTime() - start;
        writeOutput("dint", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testUDIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readUDIntByName(GVL_NAME + "udiTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("udint", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testUDIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeUDIntByName(GVL_NAME + "udiTest", 522234534);
        }
        long diff = System.nanoTime() - start;
        writeOutput("udint", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readIntByName(GVL_NAME + "iTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("int", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeIntByName(GVL_NAME + "iTest", (short) 31534);
        }
        long diff = System.nanoTime() - start;
        writeOutput("int", "w", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testUIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readUIntByName(GVL_NAME + "uiTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("uint", "r", diff);
    }
    
    /**
     * Test run of the lInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testUIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeUIntByName(GVL_NAME + "uiTest", 52534);
        }
        long diff = System.nanoTime() - start;
        writeOutput("uint", "w", diff);
    }
    
    /**
     * Test run of the sInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testSIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readSIntByName(GVL_NAME + "siTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("sint", "r", diff);
    }
    
    /**
     * Test run of the sInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testSIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeSIntByName(GVL_NAME + "siTest", (byte) 122);
        }
        long diff = System.nanoTime() - start;
        writeOutput("sint", "w", diff);
    }
    
    /**
     * Test run of the sInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testUSIntR() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readUSIntByName(GVL_NAME + "usiTest");
        }
        long diff = System.nanoTime() - start;
        writeOutput("usint", "r", diff);
    }
    
    /**
     * Test run of the sInt Datatype.
     * @throws IOException if ads not answering.
     */
    public static void testUSIntW() throws IOException {
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeUSIntByName(GVL_NAME + "usiTest", (byte) 225);
        }
        long diff = System.nanoTime() - start;
        writeOutput("usint", "w", diff);
    }
    
    /**
     * To write the measurements to a file.
     * @param type Name of the type in the output
     * @param operation Name of the operation "r" or "w"
     * @param diff "difference between start and end of test in nano seconds"
     * @throws IOException
     */
    public static void writeOutput(String type, String operation, long diff) throws IOException {
        FileWriter writer = new FileWriter(measurementGrp, true);
        writer.write(type + "," + operation + "," + RUNS + "," + (diff / (double) NORMALIZE_TO_SECONDS)
                + "," + (diff / (double) RUNS) + "\n");
        writer.close();
    }
    /**
     * Writes a timestamp into the correct file, used to marking start and end of process.
     * @throws IOException
     */
    public static void writeTimestamp() throws IOException {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.GERMANY);
        FileWriter writer = new FileWriter(startEnd, true);
        writer.write(sdf.format(date));
        writer.close();
        
        
    }
    
    /**
     * Timing the reading of a LReal Array.
     * @throws IOException
     */
    public static void timeReadLRealArray() throws IOException {
        double[] values = new double[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aCounter", values
                    , MemorySizeCalcs.getSizeCalculatorDoubleArray(), ReadVisitorsArrays.getReaderDouble());
        }
        long diff = System.nanoTime() - start;
        writeOutput("lrealarr", "r", diff);
    }
    /**
     * Timing the writing of LReal Array.
     * @throws IOException
     */
    public static void timeWriteLRealArray() throws IOException {
        double[] values = {0, 1234215343.343, 75474564234.3245};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aCounter", values
                    , MemorySizeCalcs.getSizeCalculatorDoubleArray(), WriteVisitorsArrays.getWriteSupplierLReal());
        }
        long diff = System.nanoTime() - start;
        writeOutput("lrealarr", "w", diff);
    }
    
    /**
     * Timing the reading of a Real Array.
     * @throws IOException
     */
    public static void timeReadRealArray() throws IOException {
        float[] values = new float[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorFloatArray(), ReadVisitorsArrays.getReaderFloat());
        }
        long diff = System.nanoTime() - start;
        writeOutput("realarr", "r", diff);
    }
    /**
     * Timing the writing of Real Array.
     * @throws IOException
     */
    public static void timeWriteRealArray() throws IOException {
        float[] values = {0, 123421543.343F, 754745234.3245F};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorFloatArray(), WriteVisitorsArrays.getWriteSupplierReal());
        }
        long diff = System.nanoTime() - start;
        writeOutput("realarr", "w", diff);
    }
    
    /**
     * Timing the reading of a Real Array.
     * @throws IOException
     */
    public static void timeReadLIntArray() throws IOException {
        long[] values = new long[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aLIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorLongArray(), ReadVisitorsArrays.getReaderLIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("lintarr", "r", diff);
    }
    /**
     * Timing the writing of Real Array.
     * @throws IOException
     */
    public static void timeWriteLIntArray() throws IOException {
        long[] values = {0, 1234215432343534L, 754523443978520394L};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aLIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorLongArray(), WriteVisitorsArrays.getWriteSupplierLInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("lintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a Real Array.
     * @throws IOException
     */
    public static void timeReadULIntArray() throws IOException {
        BigInteger[] values = new BigInteger[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aULIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorBigIntArray(), ReadVisitorsArrays.getReaderULIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("ulintarr", "r", diff);
    }
    /**
     * Timing the writing of Real Array.
     * @throws IOException
     */
    public static void timeWriteULIntArray() throws IOException {
        BigInteger[] values = {new BigInteger(String.valueOf(0)), 
            new BigInteger(String.valueOf(7675234353L)), new BigInteger(String.valueOf(43367634303949L))};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aULIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorBigIntArray(), WriteVisitorsArrays.getWriteSupplierULInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("ulintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a Real Array.
     * @throws IOException
     */
    public static void timeReadLWordArray() throws IOException {
        BigInteger[] values = new BigInteger[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "alWRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorBigIntArray(), ReadVisitorsArrays.getReaderULIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("lwordarr", "r", diff);
//        for (BigInteger d : values) {
//            System.out.println(Long.toUnsignedString(d.longValue()));
//        }
    }
    /**
     * Timing the writing of Real Array.
     * @throws IOException
     */
    public static void timeWriteLWordArray() throws IOException {
        BigInteger[] values = {new BigInteger(String.valueOf(0)), 
            new BigInteger(String.valueOf(123421543234353L)), new BigInteger(String.valueOf(9897634303949L))};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "alWRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorBigIntArray(), WriteVisitorsArrays.getWriteSupplierULInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("lwordarr", "w", diff);
    }
    
    /**
     * Timing the reading of a Real Array.
     * @throws IOException
     */
    public static void timeReadDIntArray() throws IOException {
        int[] values = new int[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aDIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorIntArray(), ReadVisitorsArrays.getReaderDIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("dintarr", "r", diff);
        if (TEST_OUTPUT) {
            for (int d : values) {
                System.out.println(d);
            }
        }
    }
    /**
     * Timing the writing of Real Array.
     * @throws IOException
     */
    public static void timeWriteDIntArray() throws IOException {
        int[] values = {0, 12342323, 754523397};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aDIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorIntArray(), WriteVisitorsArrays.getWriteSupplierDInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("dintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a UDInt Array.
     * @throws IOException
     */
    public static void timeReadUDIntArray() throws IOException {
        long[] values = new long[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aUDIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUDIntArray(), ReadVisitorsArrays.getReaderUDIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("udintarr", "r", diff);
        if (TEST_OUTPUT) {
            for (long d : values) {
                System.out.println(d);
            }
        }

    }
    /**
     * Timing the writing of UDInt Array.
     * @throws IOException
     */
    public static void timeWriteUDIntArray() throws IOException {
        long[] values = {0, 1234232234L, 754523397L};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aUDIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUDIntArray(), WriteVisitorsArrays.getWriteSupplierUDInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("udintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a Real Array.
     * @throws IOException
     */
    public static void timeReadDWordArray() throws IOException {
        long[] values = new long[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "adWRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUDIntArray(), ReadVisitorsArrays.getReaderUDIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("dwordarr", "r", diff);
//        for (long d : values) {
//            System.out.println(d);
//        }
    }
    
    /**
     * Timing the writing of Real Array.
     * @throws IOException
     */
    public static void timeWriteDWordArray() throws IOException {
        long[] values = {0, 12342323, 754523397};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "adWRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUDIntArray(), WriteVisitorsArrays.getWriteSupplierUDInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("dwordarr", "w", diff);
    }
    
    /**
     * Timing the reading of a int Array.
     * @throws IOException
     */
    public static void timeReadIntArray() throws IOException {
        short[] values = new short[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorShortArray(), ReadVisitorsArrays.getReaderIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("intarr", "r", diff);
        if (TEST_OUTPUT) {
            for (short d : values) {
                System.out.println(d);
            }
        }
    }
    /**
     * Timing the writing of Int Array.
     * @throws IOException
     */
    public static void timeWriteIntArray() throws IOException {
        short[] values = {0, (short) 23423, (short) 25452};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorShortArray(), WriteVisitorsArrays.getWriteSupplierInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("intarr", "w", diff);
    }
    
    /**
     * Timing the reading of a uint Array.
     * @throws IOException
     */
    public static void timeReadUIntArray() throws IOException {
        int[] values = new int[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aUIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUIntArray(), ReadVisitorsArrays.getReaderUIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("uintarr", "r", diff);
        if (TEST_OUTPUT) {
            for (int d : values) {
                System.out.println(d);
            }
        }
    }
    /**
     * Timing the writing of uint Array.
     * @throws IOException
     */
    public static void timeWriteUIntArray() throws IOException {
        int[] values = {0, 23423, 7542};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aUIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUIntArray(), WriteVisitorsArrays.getWriteSupplierUInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("uintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a uint Array.
     * @throws IOException
     */
    public static void timeReadWordArray() throws IOException {
        int[] values = new int[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "awRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUIntArray(), ReadVisitorsArrays.getReaderUIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("wordarr", "r", diff);
        if (TEST_OUTPUT) {            
            for (int d : values) {
                System.out.println(d);
            }
        }
    }
    /**
     * Timing the writing of uint Array.
     * @throws IOException
     */
    public static void timeWriteWordArray() throws IOException {
        int[] values = {0, 33423, 25452};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "awRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUIntArray(), WriteVisitorsArrays.getWriteSupplierUInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("wordarr", "w", diff);
    }
    
    /**
     * Timing the reading of a sint Array.
     * @throws IOException
     */
    public static void timeReadSIntArray() throws IOException {
        byte[] values = new byte[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aSIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorSIntArray(), ReadVisitorsArrays.getReaderSIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("sintarr", "r", diff);
        if (TEST_OUTPUT) {            
            for (byte d : values) {
                System.out.println(d);
            }
        }
    }
    
    /**
     * Timing the writing of sint Array.
     * @throws IOException
     */
    public static void timeWriteSIntArray() throws IOException {
        byte[] values = {0, 13, 123};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aSIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorSIntArray(), WriteVisitorsArrays.getWriteSupplierSInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("sintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a usint Array.
     * @throws IOException
     */
    public static void timeReadUSIntArray() throws IOException {
        short[] values = new short[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "aUSIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUSIntArray(), ReadVisitorsArrays.getReaderUSIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("usintarr", "r", diff);
        if (TEST_OUTPUT) {
            for (short d : values) {
                System.out.println(d);
            }
        }
    }
    
    /**
     * Timing the writing of usint Array.
     * @throws IOException
     */
    public static void timeWriteUSIntArray() throws IOException {
        short[] values = {0, 200, 173};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "aUSIntCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUSIntArray(), WriteVisitorsArrays.getWriteSupplierUSInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("usintarr", "w", diff);
    }
    
    /**
     * Timing the reading of a usint Array.
     * @throws IOException
     */
    public static void timeReadByteArray() throws IOException {
        short[] values = new short[3];
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.readStructByNameSimple(GVL_NAME + "abRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUSIntArray(), ReadVisitorsArrays.getReaderUSIntArray());
        }
        long diff = System.nanoTime() - start;
        writeOutput("bytearr", "r", diff);
        if (TEST_OUTPUT) {
            for (short d : values) {
                System.out.println(d);
            }
        }
    }
    
    /**
     * Timing the writing of usint Array.
     * @throws IOException
     */
    public static void timeWriteByteArray() throws IOException {
        short[] values = {0, 203, 163};
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) {
            communicator.writeStructByName(GVL_NAME + "abRCounter", values
                    , MemorySizeCalcs.getSizeCalculatorUSIntArray(), WriteVisitorsArrays.getWriteSupplierUSInt());
        }
        long diff = System.nanoTime() - start;
        writeOutput("bytearr", "w", diff);
    }
    
    
    /**
     * Method to test the reading of a simple LRealArray.
     * @param communicator the communicator to the ads
     * @throws IOException if the communication does fail.
     */
    public static void testReadSimpleLRealArray(AdsCommunication communicator) throws IOException {
        MemorySizeCalculator<double[]> sizeCalculator = new MemorySizeCalculator<double[]>() {

            @Override
            public int determineMemorySize(double[] value) throws IOException {
                int size = 0;
                for (int i = 0; i < value.length; i++) {
                    size += AdsCommunication.SIZE_DOUBLE; 
                }
                return size;
            }
            
            public int getMemSize() {
                return AdsCommunication.SIZE_DOUBLE;
            }
        };
        
        double[] values = new double[3];
        
        ReadVisitorSupplier<double[]> reader = new ReadVisitorSupplier<double[]>() {

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
        
        communicator.readStructByNameSimple("GVL_ADAS.aCounter", values, sizeCalculator, reader);
        for (double i : values) {
            System.out.println(i);
        }
    }
    
    /**
     * Method to test the reading of a simple IntArray.
     * @param communicator the communicator to the ads
     * @throws IOException if the communication does fail.
     */
    public static void testReadSimpleIntArray(AdsCommunication communicator) throws IOException {
       
        
        MemorySizeCalculator<short[]> sizeCalculatorInt = new MemorySizeCalculator<short[]>() {

            @Override
            public int determineMemorySize(short[] value) throws IOException {
                int size = 0;
                for (int i = 0; i < value.length; i++) {
                    size += AdsCommunication.SIZE_SHORT; 
                }
                return size;
            }
            
            public int getMemSize() {
                return AdsCommunication.SIZE_SHORT;
            }
        };
        
        short[] valuesIntArray = new short[3];
        
        ReadVisitorSupplier<short[]> readerInt = new ReadVisitorSupplier<short[]>() {

            @Override
            public ReadVisitor<short[]> create(Memory mem) {
                
                return new JnaMemoryReadVisitor<short[]>(mem) {

                    @Override
                    public void read(short[] value) throws IOException {
                        readIntArray(valuesIntArray, valuesIntArray.length);
                    }
                };
            }
        };
        
        communicator.readStructByNameSimple("GVL_ADAS.aIntCounter", valuesIntArray, sizeCalculatorInt, readerInt);
        for (short i : valuesIntArray) {
            System.out.println(i);
        }
    }
    
    /**
     * Method to test the reading of a simple UIntArray.
     * @param communicator the communicator to the ads
     * @throws IOException if the communication does fail.
     */
    public static void testReadSimpleDIntArray(AdsCommunication communicator) throws IOException {
        MemorySizeCalculator<int[]> sizeCalculatorUInt = new MemorySizeCalculator<int[]>() {

            @Override
            public int determineMemorySize(int[] value) throws IOException {
                int size = 0;
                for (int i = 0; i < value.length; i++) {
                    size += AdsCommunication.SIZE_SHORT; 
                }
                return size;
            }
            
            public int getMemSize() {
                return AdsCommunication.SIZE_SHORT;
            }
        };
        
        int[] valuesUIntArray = new int[3];
        
        ReadVisitorSupplier<int[]> readerUInt = new ReadVisitorSupplier<int[]>() {

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
        
        communicator.readStructByNameSimple("GVL_ADAS.aUIntCounter", valuesUIntArray, sizeCalculatorUInt, readerUInt);
        for (int i : valuesUIntArray) {
            System.out.println(i);
        }
    }
    
    /**
     * Method to test the writing of a simple UIntArray.
     * @param communicator the communicator to the ads
     * @throws IOException if the communication does fail.
     */
    public static void testWriteUIntArrayToADS(AdsCommunication communicator) throws IOException {
        MemorySizeCalculator<int[]> calc = new MemorySizeCalculator<int[]>() {

            @Override
            public int determineMemorySize(int[] value) throws IOException {
                int result = 0;
                for (int i = 0; i < value.length; i++) {
                    result += AdsCommunication.SIZE_SHORT;
                }
                return result;
            }

            @Override
            public int getMemSize() {
                return AdsCommunication.SIZE_SHORT;
            }
        };
        
        WriteVisitorSupplier<int[]> writeSupplier = new WriteVisitorSupplier<int[]>() {

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
        int[] uIntValues = {0, 65533, 65532};
        communicator.writeStructByName("GVL_ADAS.aUIntCounter", uIntValues, calc, writeSupplier);
    }
    
    /**
     * Method to test the writing of a simple UIntArray.
     * @param communicator the communicator to the ads
     * @throws IOException if the communication does fail.
     */
    public static void testWriteIntArrayToADS(AdsCommunication communicator) throws IOException {
        MemorySizeCalculator<short[]> calc = new MemorySizeCalculator<short[]>() {

            @Override
            public int determineMemorySize(short[] value) throws IOException {
                int result = 0;
                for (int i = 0; i < value.length; i++) {
                    result += AdsCommunication.SIZE_SHORT;
                }
                return result;
            }

            @Override
            public int getMemSize() {
                return AdsCommunication.SIZE_SHORT;
            }
        };
        
        WriteVisitorSupplier<short[]> writeSupplier = new WriteVisitorSupplier<short[]>() {

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
        short[] uIntValues = {0, 32766, -32765};
        communicator.writeStructByName("GVL_ADAS.aIntCounter", uIntValues, calc, writeSupplier);
    }
}
