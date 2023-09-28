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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.NativeLongByReference;

import de.iip_ecosphere.platform.libs.ads.ReadVisitor.ReadVisitorSupplier;
import de.iip_ecosphere.platform.libs.ads.WriteVisitor.WriteVisitorSupplier;

/**
 * Encapsulates lower level ADS communication. Incomplete, experimental.
 * 
 * @author Holger Eichelberger, SSE
 * @author Alexander Weber, SSE
 */
public class AdsCommunication {

    public static final int SIZE_DOUBLE = 8; // signed
    public static final int SIZE_FLOAT = 4; // signed
    public static final int SIZE_LONG = 8; // signed
    public static final int SIZE_INT = 4; // signed
    public static final int SIZE_SHORT = 2; // signed
    public static final int SIZE_BYTE = 1; // signed
    private static final Map<Long, String> ERROR_MSG = new HashMap<>();
    private String adsAddress;
    private int adsPort;
    private TcAds ads;
    private TcAds.AmsAddr addr;
    private long port;
    private Map<String, Long> indexOffsetCache = new HashMap<>();

    /**
     * Creates an instance.
     * 
     * @param adsAddress the ADS address to communicate with
     * @param adsPort the ADS port
     */
    public AdsCommunication(String adsAddress, int adsPort) {
        this.adsAddress = adsAddress;
        this.adsPort = adsPort;
        ads = Ads.getInstance();
    }

    /**
     * Initializes the communication.
     * 
     * @throws IOException in case that the ADS communication fails
     */
    public void initCommunication() throws IOException {
        // https://groups.google.com/g/jna-users/c/j0fw96PlOpM
        final Logger cleanerLogger = Logger.getLogger("com.sun.jna.internal.Cleaner");
        cleanerLogger.setLevel(Level.OFF);        
        if (Platform.getOSType() == Platform.LINUX) {
            System.out.println("operation on linux system not yet tested. Adding additional ads-rout to host");
            // unclear, seems to be unused; Port = self.__ADSPort
            // not yet there: pyads.add_route(self.__ADS_Address, '127.0.0.1')
        }
        addr = new TcAds.AmsAddr();
        TcAds.AmsNetId netId = new TcAds.AmsNetId();
        String[] adrParts = adsAddress.split("\\.");
        for (int i = 0; i < 6; i++) {
            byte val = 0;
            if (i < adrParts.length) {
                try {
                    //Seems like TCADS only cares for the actual bits, but we need values over 127
                    val = (byte) Short.parseShort(adrParts[i]);
                } catch (NumberFormatException e) {
                    System.out.println("Cannot parse " + adrParts[i] + " to byte. Using 0.");
                }
            }
            netId.b[i] = val;
        }
        addr.netId = netId;
        addr.port = (short) adsPort;
        
        port = ads.AdsPortOpenEx();
    }

    /**
     * Returns a handle/index number to a (known) data name.
     * 
     * @param dataName the data name
     * @return the handle
     * @throws IOException if the communication fails
     */
    private long getIndexOffset(String dataName) throws IOException {
        long result;
        //System.out.println("NAME: " + dataName);
        Long cachedResult = indexOffsetCache.get(dataName);
        if (null == cachedResult) { 
            Memory mem = new Memory(Native.LONG_SIZE); 
            Memory namePtr = new Memory((dataName.length() + 1)); 
            namePtr.setString(0, dataName);
            NativeLongByReference ret = new NativeLongByReference(); 
            long status = ads.AdsSyncReadWriteReqEx2(port, addr, TcAds.ADSIGRP_SYM_HNDBYNAME, 0x0, mem.size(), mem,
                namePtr.size(), namePtr, ret);
            handleStatus(status);
            result = mem.getInt(0);
            mem.close();
            namePtr.close();
            indexOffsetCache.put(dataName, result); // TODO release handle
        } else {
            result = cachedResult.longValue();
        }
        return result;
    }

    /**
     * Reads a value represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param result the result memory of the size of the data to be read
     * @throws IOException if the communication fails
     */
    private void readByName(String dataName, Memory result) throws IOException {
        long indexOffset = getIndexOffset(dataName);
        NativeLongByReference ret = new NativeLongByReference();
        long status = ads.AdsSyncReadReqEx2(port, addr, TcAds.ADSIGRP_SYM_VALBYHND, indexOffset, result.size(), 
            result, ret);
        handleStatus(status);
    }

    /**
     * Reads a LReal (64bit) from a variable represented by {@code dataName}.
     * Java equivalent double
     * 
     * @param dataName the data name
     * @return the double value
     * @throws IOException if the communication fails
     */
    public double readLRealByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_DOUBLE);
        readByName(dataName, mem);
        double result = mem.getDouble(0);
        mem.close();
        return result;
    }

    /**
     * Reads a real (32bit) from a variable represented by {@code dataName}.
     * Java equivalent float
     * 
     * @param dataName the data name
     * @return the float value
     * @throws IOException if the communication fails
     */
    public float readRealByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_FLOAT);
        readByName(dataName, mem);
        float result = mem.getFloat(0);
        mem.close();
        return result;
    }

    /**
     * Reads a ULint (64bit) from a variable represented by {@code dataName}.
     * Made for the numbers over the positive border of long.
     * 
     * @param dataName the data name
     * @return the BigInteger carrying the value.
     * @throws IOException if the communication fails
     */
    public BigInteger readULIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_LONG);
        readByName(dataName, mem);
        byte[] temp = new byte[8];
        mem.read(0, temp, 0, temp.length);
        BigInteger result = new BigInteger(temp);
        mem.close();
        return result;
    }
    
    /**
     * Reads a signed LInt (64bit) from a variable represented by {@code dataName}.
     * Or a unsigned long or a LWord, if BigInteger is not fitting.
     * BigInt may be an option
     * @param dataName the data name
     * @return the long value
     * @throws IOException if the communication fails
     */
    public long readLIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_LONG);
        readByName(dataName, mem);
        long result = mem.getLong(0);
        mem.close();
        return result;
    }

    /**
     * Reads a Dint (32bit) or DWord from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the int value
     * @throws IOException if the communication fails
     */
    public int readDIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_INT);
        readByName(dataName, mem);
        int result = mem.getInt(0);
        mem.close();
        return result;
    }

    /**
     * Reads a unsigned DInt or DWord (32bit) long from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the int value
     * @throws IOException if the communication fails
     */
    public long readUDIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_INT);
        readByName(dataName, mem);
        long result = mem.getInt(0) & 0xffffffffL;
        mem.close();
        return result;
    }
    
    /**
     * Reads a signed Int(16Bit) from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the short value
     * @throws IOException if the communication fails
     */
    public short readIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_SHORT);
        readByName(dataName, mem);
        short result = mem.getShort(0);
        mem.close();
        return result;
    }
    
    /**
     * Reads a UInt or a WORD (16Bit) from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the short value
     * @throws IOException if the communication fails
     */
    public int readUIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_SHORT);
        readByName(dataName, mem);
        int result = mem.getShort(0) & 0xffff;
        mem.close();
        return result;
    }

    /**
     * Reads a SInt (8Bit) by name from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the byte value
     * @throws IOException if the communication fails
     */
    public byte readSIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_BYTE);
        readByName(dataName, mem);
        byte result = mem.getByte(0);
        mem.close();
        return result;
    }
    
    /**
     * Reads a USint or BYTE (8Bit) by name from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the byte value
     * @throws IOException if the communication fails
     */
    public short readUSIntByName(String dataName) throws IOException {
        Memory mem = new Memory(SIZE_BYTE);
        readByName(dataName, mem);
        short result = (short) (mem.getByte(0) & 0xff);
        mem.close();
        return result;
    }
    
    /**
     * Reads a String from a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @return the byte value
     * @throws IOException if the communication fails
     */
    public String readStringByName(String dataName) throws IOException {
        Memory mem = new Memory(1024); // as in pyads
        readByName(dataName, mem); //ReadbyName using dataNAme and mem
        String result = mem.getString(0);
        mem.close();
        return result;
    }
    
    
    
    /**
     * Reads an object structure in given sequence through reflection.
     * 
     * @param dataName the data name
     * @param value the object value to be changed as side effect, nested references must be initialized to know about 
     *     their memory size
     * @throws IOException if the communication/writing fails
     */
    public void readObjectByName(String dataName, Object value) throws IOException {
        readStructByName(dataName, value, new ReflectionMemorySizeCalculator(), 
            m -> new ReflectionJnaMemoryReadVisitor(m));
    }
    
    /**
     * Writes an object structure through the given write visitor.
     * 
     * @param <T> the type to be written
     * @param dataName the data name
     * @param value the value value to write
     * @param sizeCalculator an instance that calculates the required memory size for writing the object
     * @param reader a supplier to create a corresponding read visitor which is bound internally against a used 
     *     JNA memory object
     * @throws IOException if the communication/writing fails
     */
    public <T> void readStructByName(String dataName, T value, MemorySizeCalculator<T> sizeCalculator, 
        ReadVisitorSupplier<T> reader) throws IOException {
        int size = sizeCalculator.determineMemorySize(value);
        Memory data = new Memory(size); //Allocation of space to the memory
        ReadVisitor<T> visitor = reader.create(data);
        visitor.read(value);
        readByName(dataName, data);
        data.getByteBuffer(0, size);
        data.close();
    }
    
    /**
     * Writes an object structure through the given write visitor.
     * 
     * @param <T> the type to be written
     * @param dataName the data name
     * @param value the value value to write
     * @param sizeCalculator an instance that calculates the required memory size for writing the object
     * @param reader a supplier to create a corresponding read visitor which is bound internally against a used 
     *     JNA memory object
     * @throws IOException if the communication/writing fails
     */
    public <T> void readStructByNameSimple(String dataName, T value, MemorySizeCalculator<T> sizeCalculator, 
        ReadVisitorSupplier<T> reader) throws IOException {
        int size = sizeCalculator.determineMemorySize(value);
        //Allocation of space to the memory
        Memory data = new Memory(size);
        ReadVisitor<T> visitor = reader.create(data);
        readByName(dataName, data);
        visitor.read(value);
        data.close();
    }
    
    
    
    /**
     * Writes data to a variable represented by {@code dataName}.
     * 
     * @param dataName the variable name
     * @param data the data
     * @throws IOException if getting the index in the ADS memory or writing the data fails
     */
    private void writeByName(String dataName, Memory data) throws IOException {
        long indexOffset = getIndexOffset(dataName);
        long status = ads.AdsSyncWriteReqEx(port, addr, TcAds.ADSIGRP_SYM_VALBYHND, indexOffset, data.size(), data);
        handleStatus(status);
    }

    /**
     * Writes a signed double to a LReal variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeLRealByName(String dataName, double value) throws IOException {
        Memory data = new Memory(SIZE_DOUBLE);
        data.setDouble(0, value);
        writeByName(dataName, data);
        data.close();
    }

    /**
     * Writes a signed float to Real a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeRealByName(String dataName, float value) throws IOException {
        Memory data = new Memory(SIZE_FLOAT);
        data.setFloat(0, value);
        writeByName(dataName, data);
        data.close();
    }
    
    /**
     * Writes a signed long to a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeLIntByName(String dataName, long value) throws IOException {
        Memory data = new Memory(SIZE_LONG);
        data.setLong(0, value);
        writeByName(dataName, data);
        data.close();
    }
    
    /**
     * writes a ULint or LWord (64bit) from a variable represented by {@code dataName}.
     * Made for the numbers over the positive border of long.
     * 
     * @param dataName the data name
     * @param value the value to be written from bigInteger
     * @throws IOException if the communication fails
     */
    public void writeULIntByName(String dataName, BigInteger value) throws IOException {
        writeLIntByName(dataName, value.longValue());
    }
    
    /**
     * Writes a signed int to a DInt a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeDIntByName(String dataName, int value) throws IOException {
        Memory data = new Memory(SIZE_INT);
        data.setInt(0, value);
        writeByName(dataName, data);
        data.close();
    }
    
    /**
     * Writes a UDInt or DWord to a variable represented by {@code dataName}.
     * Takes a long as this is the only way to store the values of UDInt in java
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeUDIntByName(String dataName, long value) throws IOException {
        writeDIntByName(dataName, (int) value);
    }
    
    /**
     * Writes a signed short to a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeIntByName(String dataName, short value) throws IOException {
        Memory data = new Memory(SIZE_SHORT);
        data.setShort(0, value);
        writeByName(dataName, data);
        data.close();
    }
    
    /**
     * Writes a UInt or Word to a variable represented by {@code dataName}.
     * Takes a int (32Bit) which stores the values for the unsigned Int.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeUIntByName(String dataName, int value) throws IOException {
        writeIntByName(dataName, (short) value);
    }

    /**
     * Writes a signed byte to a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeSIntByName(String dataName, byte value) throws IOException {
        Memory data = new Memory(SIZE_BYTE);
        data.setByte(0, value);
        writeByName(dataName, data);
        data.close();
    }
    
    /**
     * Writes a USInt or Byte to a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeUSIntByName(String dataName, short value) throws IOException {
        writeSIntByName(dataName, (byte) value);
    }

    /**
     * Writes a String to a variable represented by {@code dataName}.
     * 
     * @param dataName the data name
     * @param value the value to write
     * @throws IOException if the communication fails
     */
    public void writeStringByName(String dataName, String value) throws IOException {
        Memory data = new Memory(value.length() + 1);
        data.setString(0, value);
        writeByName(dataName, data);
        data.close();
    }
    
    // TODO datetime, arrays

    /**
     * Writes an object structure in given sequence through reflection.
     * 
     * @param dataName the data name
     * @param value the object value to write
     * @throws IOException if the communication/writing fails
     */
    public void writeObjectByName(String dataName, Object value) throws IOException {
        writeStructByName(dataName, value, new ReflectionMemorySizeCalculator(), 
            m -> new ReflectionJnaMemoryWriteVisitor(m));
    }
    
    /**
     * Writes an object structure through the given write visitor.
     * 
     * @param <T> the type to be written
     * @param dataName the data name
     * @param value the value value to write
     * @param sizeCalculator an instance that calculates the required memory size for writing the object
     * @param writer a supplier to create a corresponding writer visitor which is bound internally against a used 
     *     JNA memory object
     * @throws IOException if the communication/writing fails
     */
    public <T> void writeStructByName(String dataName, T value, MemorySizeCalculator<T> sizeCalculator, 
        WriteVisitorSupplier<T> writer) throws IOException {
        int size = sizeCalculator.determineMemorySize(value);
        Memory data = new Memory(size);
        WriteVisitor<T> visitor = writer.create(data);
        visitor.write(value);
        writeByName(dataName, data);
        data.close();
    }
    
    /**
     * Closes the ADS communication.
     * 
     * @throws IOException if the communication fails
     */
    public void close() throws IOException {
        long status = ads.AdsPortCloseEx(port);
        handleStatus(status);
    }

    /**
     * Handles a status code.
     * 
     * @param status the status code
     * @throws IOException if {@code status} indicates an error
     */
    private void handleStatus(long status) throws IOException {
        if (status != TcAds.ADSERR_NOERR) {
            String text = ERROR_MSG.get(status);
            if (null == text) {
                text = ERROR_MSG.get(status + TcAds.ERR_ADSERRS);
            }
            if (null == text) {
                text = "unknown/undocumented";
            }
            throw new IOException("ADS communication failed: " + text + " (code: " + status + ")");
        }
    }
    
    static {
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_ERROR, "Error class < device error >");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_SRVNOTSUPP, "Service is not supported by server");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDGRP, "invalid indexGroup");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDOFFSET, "invalid indexOffset");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDACCESS, "reading/writing not permitted");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDSIZE, "parameter size not correct");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDDATA, "invalid parameter value(s)");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOTREADY, "device is not in a ready state");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_BUSY, "device is busy");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDCONTEXT, "invalid context (must be InWindows)");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOMEMORY, "out of memory");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDPARM, "invalid parameter value(s)");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOTFOUND, "not found (files, ...)");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_SYNTAX, "syntax error in comamnd or file");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INCOMPATIBLE, "objects do not match");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_EXISTS, "object already exists");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_SYMBOLNOTFOUND, "symbol not found");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_SYMBOLVERSIONINVALID, "symbol version invalid");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDSTATE, "server is in invalid state");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_TRANSMODENOTSUPP, "AdsTransMode not supported");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOTIFYHNDINVALID, "Notification handle is invalid");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_CLIENTUNKNOWN, "Notification client not registered");

        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOMOREHDLS, "no more notification handles");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDWATCHSIZE, "size for watch to big");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOTINIT, "device not initialized");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_TIMEOUT, "device has a timeout");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_NOINTERFACE, "query interface failed");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDINTERFACE, "wrong interface required");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDCLSID, "class ID is invalid");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDOBJID, "object ID is invalid");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_PENDING, "request is pending");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_ABORTED, "request is aborted");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_WARNING, "signal warning");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_INVALIDARRAYIDX, "invalid array index");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_SYMBOLNOTACTIVE, "symbol not active -> release handle and try again");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_ACCESSDENIED, "access denied");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSENOTFOUND, "no license found");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSEEXPIRED, "license expired");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSEEXCEEDED, "license exceeded");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSEINVALID, "license invalid");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSESYSTEMID, "license invalid system id");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSENOTIMELIMIT, "license not time limited");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSEFUTUREISSUE, "license issue time in the future");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSETIMETOLONG, "license time period to long");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_EXCEPTION, "exception in device specific code");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_LICENSEDUPLICATED, "license file read twice");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_SIGNATUREINVALID, "invalid signature");
        ERROR_MSG.put(TcAds.ADSERR_DEVICE_CERTIFICATEINVALID, "public key certificate");
        //
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_ERROR, "Error class < client error >");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_INVALIDPARM, "invalid parameter at service call");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_LISTEMPTY, "polling list  is empty");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_VARUSED, "var connection already in use");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_DUPLINVOKEID, "invoke id in use");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_SYNCTIMEOUT, "timeout elapsed");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_W32ERROR, "error in win32 subsystem");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_TIMEOUTINVALID, "?");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_PORTNOTOPEN, "ads dll");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_NOAMSADDR, "ads dll");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_SYNCINTERNAL, "internal error in ads sync");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_ADDHASH, "hash table overflow");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_REMOVEHASH, "key not found in hash table");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_NOMORESYM, "no more symbols in cache");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_SYNCRESINVALID, "invalid response received");
        ERROR_MSG.put(TcAds.ADSERR_CLIENT_SYNCPORTLOCKED, "sync port is locked");
    }

}
