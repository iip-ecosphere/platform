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

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * Represents the TcAds native library. Texts are based on the 
 * <a href="https://infosys.beckhoff.com/index.php?content=../content/1031/tc3_adsdll2/117513611.html">
 * Beckhoff C++ API specification</a>.
 * 
 * @author Holger Eichelberger, SSE
 * @author Alexander Weber, SSE
 */
public interface TcAds extends Library {
    
    // selected constants from TcAdsDef.h
    public static final long ADSIGRP_SYM_HNDBYNAME = 0xF003;
    public static final long ADSIGRP_SYM_VALBYNAME = 0xF004;
    public static final long ADSIGRP_SYM_VALBYHND = 0xF005;
    public static final long ADSIGRP_SYM_RELEASEHND = 0xF006;
    public static final long ADSIGRP_SYM_INFOBYNAME = 0xF007;
    public static final long ADSIGRP_SYM_VERSION = 0xF008;
    public static final long ADSIGRP_SYM_INFOBYNAMEEX = 0xF009;

    public static final long ADSIGRP_SYM_DOWNLOAD = 0xF00A;
    public static final long ADSIGRP_SYM_UPLOAD = 0xF00B;
    public static final long ADSIGRP_SYM_UPLOADINFO = 0xF00C;
    public static final long ADSIGRP_SYM_DOWNLOAD2 = 0xF00D;
    public static final long ADSIGRP_SYM_DT_UPLOAD = 0xF00E;
    public static final long ADSIGRP_SYM_UPLOADINFO2 = 0xF00F;
    
    public static final long ADSERR_NOERR = 0x00;
    public static final long ERR_ADSERRS = 0x0700;

    public static final long ADSERR_DEVICE_ERROR =                (0x00 + ERR_ADSERRS); // Error class < device error >
    public static final long ADSERR_DEVICE_SRVNOTSUPP =           (0x01 + ERR_ADSERRS); // Service not supp. by server
    public static final long ADSERR_DEVICE_INVALIDGRP =           (0x02 + ERR_ADSERRS); // invalid indexGroup
    public static final long ADSERR_DEVICE_INVALIDOFFSET =        (0x03 + ERR_ADSERRS); // invalid indexOffset
    public static final long ADSERR_DEVICE_INVALIDACCESS =        (0x04 + ERR_ADSERRS); // reading/writing not permitted
    public static final long ADSERR_DEVICE_INVALIDSIZE =          (0x05 + ERR_ADSERRS); // parameter size not correct
    public static final long ADSERR_DEVICE_INVALIDDATA =          (0x06 + ERR_ADSERRS); // invalid parameter value(s)
    public static final long ADSERR_DEVICE_NOTREADY =             (0x07 + ERR_ADSERRS); // device is not in ready state
    public static final long ADSERR_DEVICE_BUSY =                 (0x08 + ERR_ADSERRS); // device is busy
    public static final long ADSERR_DEVICE_INVALIDCONTEXT =       (0x09 + ERR_ADSERRS); // invalid context (->InWindows)
    public static final long ADSERR_DEVICE_NOMEMORY =             (0x0A + ERR_ADSERRS); // out of memory
    public static final long ADSERR_DEVICE_INVALIDPARM =          (0x0B + ERR_ADSERRS); // invalid parameter value(s)
    public static final long ADSERR_DEVICE_NOTFOUND =             (0x0C + ERR_ADSERRS); // not found (files, ...)
    public static final long ADSERR_DEVICE_SYNTAX =               (0x0D + ERR_ADSERRS); // syntax error in cmd or file
    public static final long ADSERR_DEVICE_INCOMPATIBLE =         (0x0E + ERR_ADSERRS); // objects do not match
    public static final long ADSERR_DEVICE_EXISTS =               (0x0F + ERR_ADSERRS); // object already exists
    public static final long ADSERR_DEVICE_SYMBOLNOTFOUND =       (0x10 + ERR_ADSERRS); // symbol not found
    public static final long ADSERR_DEVICE_SYMBOLVERSIONINVALID = (0x11 + ERR_ADSERRS); // symbol version invalid
    public static final long ADSERR_DEVICE_INVALIDSTATE =         (0x12 + ERR_ADSERRS); // server is in invalid state
    public static final long ADSERR_DEVICE_TRANSMODENOTSUPP =     (0x13 + ERR_ADSERRS); // AdsTransMode not supported
    public static final long ADSERR_DEVICE_NOTIFYHNDINVALID =     (0x14 + ERR_ADSERRS); // Notif. handle is invalid
    public static final long ADSERR_DEVICE_CLIENTUNKNOWN =        (0x15 + ERR_ADSERRS); // Notif. client not registered
    public static final long ADSERR_DEVICE_NOMOREHDLS =           (0x16 + ERR_ADSERRS); // no more notification handles
    public static final long ADSERR_DEVICE_INVALIDWATCHSIZE =     (0x17 + ERR_ADSERRS); // size for watch to big
    public static final long ADSERR_DEVICE_NOTINIT =              (0x18 + ERR_ADSERRS); // device not initialized
    public static final long ADSERR_DEVICE_TIMEOUT =              (0x19 + ERR_ADSERRS); // device has a timeout
    public static final long ADSERR_DEVICE_NOINTERFACE =          (0x1A + ERR_ADSERRS); // query interface failed
    public static final long ADSERR_DEVICE_INVALIDINTERFACE =     (0x1B + ERR_ADSERRS); // wrong interface required
    public static final long ADSERR_DEVICE_INVALIDCLSID =         (0x1C + ERR_ADSERRS); // class ID is invalid
    public static final long ADSERR_DEVICE_INVALIDOBJID =         (0x1D + ERR_ADSERRS); // object ID is invalid
    public static final long ADSERR_DEVICE_PENDING =              (0x1E + ERR_ADSERRS); // request is pending
    public static final long ADSERR_DEVICE_ABORTED =              (0x1F + ERR_ADSERRS); // request is aborted
    public static final long ADSERR_DEVICE_WARNING =              (0x20 + ERR_ADSERRS); // signal warning
    public static final long ADSERR_DEVICE_INVALIDARRAYIDX =      (0x21 + ERR_ADSERRS); // invalid array index
    public static final long ADSERR_DEVICE_SYMBOLNOTACTIVE =      (0x22 + ERR_ADSERRS); // symbol not active
    public static final long ADSERR_DEVICE_ACCESSDENIED =         (0x23 + ERR_ADSERRS); // access denied
    public static final long ADSERR_DEVICE_LICENSENOTFOUND =      (0x24 + ERR_ADSERRS); // no license found
    public static final long ADSERR_DEVICE_LICENSEEXPIRED =       (0x25 + ERR_ADSERRS); // license expired
    public static final long ADSERR_DEVICE_LICENSEEXCEEDED =      (0x26 + ERR_ADSERRS); // license exceeded
    public static final long ADSERR_DEVICE_LICENSEINVALID =       (0x27 + ERR_ADSERRS); // license invalid
    public static final long ADSERR_DEVICE_LICENSESYSTEMID =      (0x28 + ERR_ADSERRS); // license invalid system id
    public static final long ADSERR_DEVICE_LICENSENOTIMELIMIT =   (0x29 + ERR_ADSERRS); // license not time limited
    public static final long ADSERR_DEVICE_LICENSEFUTUREISSUE =   (0x2A + ERR_ADSERRS); // license issue time in future
    public static final long ADSERR_DEVICE_LICENSETIMETOLONG =    (0x2B + ERR_ADSERRS); // license time period to long
    public static final long ADSERR_DEVICE_EXCEPTION =            (0x2C + ERR_ADSERRS); // exc. in device specific code
    public static final long ADSERR_DEVICE_LICENSEDUPLICATED =    (0x2D + ERR_ADSERRS); // license file read twice
    public static final long ADSERR_DEVICE_SIGNATUREINVALID =     (0x2E + ERR_ADSERRS); // invalid signature
    public static final long ADSERR_DEVICE_CERTIFICATEINVALID =   (0x2F + ERR_ADSERRS); // public key certificate
    //
    public static final long ADSERR_CLIENT_ERROR =                (0x40 + ERR_ADSERRS); // Error class < client error >
    public static final long ADSERR_CLIENT_INVALIDPARM =          (0x41 + ERR_ADSERRS); // invalid parameter at svc call
    public static final long ADSERR_CLIENT_LISTEMPTY =            (0x42 + ERR_ADSERRS); // polling list  is empty
    public static final long ADSERR_CLIENT_VARUSED =              (0x43 + ERR_ADSERRS); // var connection already in use
    public static final long ADSERR_CLIENT_DUPLINVOKEID =         (0x44 + ERR_ADSERRS); // invoke id in use
    public static final long ADSERR_CLIENT_SYNCTIMEOUT =          (0x45 + ERR_ADSERRS); // timeout elapsed
    public static final long ADSERR_CLIENT_W32ERROR =             (0x46 + ERR_ADSERRS); // error in win32 subsystem
    public static final long ADSERR_CLIENT_TIMEOUTINVALID =       (0x47 + ERR_ADSERRS); // ?
    public static final long ADSERR_CLIENT_PORTNOTOPEN =          (0x48 + ERR_ADSERRS); // ads dll
    public static final long ADSERR_CLIENT_NOAMSADDR =            (0x49 + ERR_ADSERRS); // ads dll
    public static final long ADSERR_CLIENT_SYNCINTERNAL =         (0x50 + ERR_ADSERRS); // internal error in ads sync
    public static final long ADSERR_CLIENT_ADDHASH =              (0x51 + ERR_ADSERRS); // hash table overflow
    public static final long ADSERR_CLIENT_REMOVEHASH =           (0x52 + ERR_ADSERRS); // key not found in hash table
    public static final long ADSERR_CLIENT_NOMORESYM =            (0x53 + ERR_ADSERRS); // no more symbols in cache
    public static final long ADSERR_CLIENT_SYNCRESINVALID =       (0x54 + ERR_ADSERRS); // invalid response received
    public static final long ADSERR_CLIENT_SYNCPORTLOCKED =       (0x55 + ERR_ADSERRS); // sync port is locked


    // keeping Java primitive types for now, alternative would be NativeLong, ...
    // https://github.com/java-native-access/jna/blob/master/www/Mappings.md
    // https://www.baeldung.com/java-jna-dynamic-libraries
    // https://code.google.com/archive/p/jnaerator/downloads
    
    // checkstyle: stop names check
    
    /**
     * The NetId of and ADS device can be represented in this structure.
     * 
     * @author Holger Eichelberger, SSE
     */
    @FieldOrder({"b"})
    public class AmsNetId extends Structure {

        /**
         * NetId, consisting of 6 digits.
         */
        public byte[] b = new byte[6];
        
    }
    
    /**
     * The complete address of an ADS device can be stored in this structure.
     */
    @FieldOrder({"netId","port"})
    public class AmsAddr extends Structure {

        /**
         * The NetId.
         */
        public AmsNetId netId = new AmsNetId();
        
        /**
         * The port.
         */
        public short port;
        
    };
    
    /**
     * The structure contains the version number, revision number and build number.
     * 
     * @author Holger Eichelberger, SSE
     */
    @FieldOrder({"version", "revision", "build"})
    public class AdsVersion extends Structure {
        
        /**
         * Version number.
         */
        public byte version;
        
        /**
         * Revision number.
         */
        public byte revision;
        
        /**
         * Build number.
         */
        public short build;
        
    };
    
    /**
     * Ads transfer mode.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ADSTRANSMODE {
        
        /**
         * No transfer.
         */
        public static final int ADSTRANS_NOTRANS        = 0;

        /**
         * The notification's callback function is invoked cyclically. 
         */
        public static final int ADSTRANS_CLIENTCYCLE    = 1;

        /**
         *  The notification's callback function is only invoked when the value changes.
         */
        public static final int ADSTRANS_CLIENTONCHA    = 2;

        /**
         * The notification's callback function is invoked cyclically. 
         */
        public static final int ADSTRANS_SERVERCYCLE    = 3;

        /**
         *  The notification's callback function is only invoked when the value changes.
         */
        public static final int ADSTRANS_SERVERONCHA    = 4;
        
    };
    
    /**
     * This structure contains all the attributes for the definition of a notification.
     * 
     * @author Holger Eichelberger, SSE
     */
    @FieldOrder({"cbLength", "nTransMode", "nMaxDelay", "nCycleTime", "dwChangeFilter"})
    public class AdsNotificationAttrib extends Structure {
        
        /**
         * Length of the data that is to be passed to the callback function.
         */
        public long cbLength;
        
        /**
         * The trans mode, use {@link ADSTRANSMODE#ADSTRANS_SERVERCYCLE} or {@link ADSTRANSMODE#ADSTRANS_SERVERONCHA}. 
         */
        public ADSTRANSMODE nTransMode;
        
        /**
         * The notification's callback function is invoked at the latest when this time has elapsed. The unit is 100 ns.
         */
        public long nMaxDelay;
        
        //start of union?? 
        
        /**
         * The ADS server checks whether the variable has changed after this time interval. The unit is 100 ns.
         */
        public long nCycleTime;
        public long dwChangeFilter;
        
        //end of union??
        
    };
    
    @FieldOrder({"hNotification", "nTimeStamp", "cbSampleSize", "data"})
    public class AdsNotificationHeader extends Structure {
        
        /**
         * Handle for the notification. Is specified when the notification is defined;
         */
        public long hNotification;
        
        /**
         * Time stamp in FILETIME format.
         */
        public long nTimeStamp;
        
        /**
         * Number of bytes transferred.
         */
        public long cbSampleSize;
        
        /**
         * Array with the transferred data.
         */
        public byte[] data;
        
    };
    
    /**
     * Type definition of the callback function required by the {@link TcAds#AdsSyncAddDeviceNotificationReq(
     * AmsAddr, long, long, AdsNotificationAttrib, PAdsNotificationFuncEx, long, NativeLongByReference)} or the
     * {@link TcAds#AdsSyncAddDeviceNotificationReqEx(long, AmsAddr, long, long, AdsNotificationAttrib, 
     * PAdsNotificationFuncEx, long, NativeLongByReference)} function.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PAdsNotificationFuncEx extends Callback {
        
        /**
         * Callback function, called as notification.
         * 
         * @param pAddr Ams address of ADS server
         * @param pNotification notification header
         * @param hUser user handle
         */
        public void invoke(AmsAddr pAddr, AdsNotificationHeader pNotification, long hUser);
        
    }
    
    /**
     * Callback function for {@link TcAds#AdsAmsRegisterRouterNotification(PAmsRouterNotificationFuncEx)}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface PAmsRouterNotificationFuncEx extends Callback {

        /**
         * Callback function.
         * 
         * @param nEvent no documentation found
         */
        public void invoke(long nEvent);
        
    }

    /**
     * Returns the version number, revision number and build number of the ADS-DLL.
     * 
     * @return version number in coded form these three items related to the ADS-DLL.
     */
    public long AdsGetDllVersion();
    
    /**
     * Establishes a connection (communication port) to the TwinCAT message router.
     * 
     * @return a port number that has been assigned to the program by the ADS router is returned.
     */
    public long AdsPortOpen();

    /**
     * The connection (communication port) to the TwinCAT message router is closed.
     * 
     * @return returns the function's error status.
     */
    public long AdsPortClose();
    
    /**
     * Returns the local NetId and port number.
     * 
     * @param pAddr the address to modify as side-effect
     * @return the function's error status.
     */
    public long AdsGetLocalAddress(AmsAddr pAddr);
    
    /**
     * Writes data synchronously to an ADS device.
     * 
     * @param pServerAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param length count of bytes to write
     * @param pData the data to write
     * @return returns the function's error status
     */
    public long AdsSyncWriteReq(AmsAddr pServerAddr, long indexGroup, long indexOffset, long length, Pointer pData);

    /**
     * Reads data synchronously from an ADS server.
     * 
     * @param pAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param length count of bytes to read
     * @param pData pointer to the client buffer
     * @return returns the function's error status
     */
    public long AdsSyncReadReq(AmsAddr pAddr, long indexGroup, long indexOffset, long length, Pointer pData);

    /**
     * Reads data synchronously from an ADS server.
     * 
     * @param pAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param length count of bytes to read
     * @param pData pointer to the client buffer
     * @param pcbReturn count of bytes read
     * @return returns the function's error status
     */
    public long AdsSyncReadReqEx(AmsAddr pAddr, long indexGroup, long indexOffset, long length, Pointer pData, 
        Long pcbReturn);

    /**
     * Writes data synchronously into an ADS server and receives data back from the ADS device.
     * 
     * @param pAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param cbReadLength count of bytes to read
     * @param pReadData pointer to the client buffer
     * @param cbWriteLength count of bytes to write
     * @param pWriteData pointer to the client buffer
     * @return returns the function's error status
     */
    public long AdsSyncReadWriteReq(AmsAddr pAddr, long indexGroup, long indexOffset, long cbReadLength, 
        Pointer pReadData, long cbWriteLength, Pointer pWriteData);

    /**
     * Writes data synchronously into an ADS server and receives data back from the ADS device.
     * 
     * @param pAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param cbReadLength count of bytes to read
     * @param pReadData pointer to the client buffer
     * @param cbWriteLength count of bytes to write
     * @param pWriteData pointer to the client buffer
     * @param pcbReturn count of bytes read
     * @return returns the function's error status
     */
    public long AdsSyncReadWriteReqEx(AmsAddr pAddr, long indexGroup, long indexOffset, long cbReadLength, 
        Pointer pReadData, long cbWriteLength, Pointer pWriteData, NativeLongByReference pcbReturn);

    /**
     * Reads the identification and version number of an ADS server.
     * 
     * @param pAddr Ams address of ADS server
     * @param pDevName fixed length string (16 Byte)
     * @param pVersion client buffer to store server version
     * @return Returns the function's error status.
     */
    public long AdsSyncReadDeviceInfoReq(AmsAddr pAddr, ByteBuffer pDevName, AdsVersion pVersion);

    /**
     * Changes the ADS status and the device status of an ADS server.
     * 
     * @param pAddr Ams address of ADS server
     * @param adsState index group in ADS server interface
     * @param deviceState index offset in ADS server interface
     * @param length count of bytes to write
     * @param pData pointer to the client buffer
     * @return returns the function's error status
     */
    public long AdsSyncWriteControlReq(AmsAddr pAddr, short adsState, short deviceState, long length, Pointer pData);

    /**
     * Reads the ADS status and the device status from an ADS server.
     * 
     * @param pAddr Ams address of ADS server
     * @param pAdsState pointer to client buffer
     * @param pDeviceState pointer to the client buffer
     * @return returns the function's error status
     */
    public long AdsSyncReadStateReq(AmsAddr pAddr, ShortBuffer pAdsState, ShortBuffer pDeviceState);

    /**
     * A notification is defined within an ADS server (e.g. PLC). When a certain event occurs a function (the callback 
     * function) is invoked in the ADS client (C program). Per ADS-Port a limited number of 550 notifications are 
     * available.
     * 
     * @param pAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param pNoteAttrib attributes of notification request
     * @param pNoteFunc address of notification callback
     * @param hUser user handle
     * @param pNotification pointer to notification handle (return value)
     * @return returns the function's error status
     */
    public long AdsSyncAddDeviceNotificationReq(AmsAddr pAddr, long indexGroup, long indexOffset, 
       AdsNotificationAttrib pNoteAttrib, PAdsNotificationFuncEx pNoteFunc, long hUser, 
       NativeLongByReference pNotification);

    /**
     * A notification defined previously is deleted from an ADS server.
     * 
     * @param pAddr Ams address of ADS server
     * @param hNotification notification handle
     * @return returns the function's error status
     */
    public long AdsSyncDelDeviceNotificationReq(AmsAddr pAddr, long hNotification);

    /**
     * Alters the timeout for the ADS functions. The standard value is 5000 ms.
     * 
     * @param nMs timeout in ms
     * @return returns the function's error status.
     */
    public long AdsSyncSetTimeout(long nMs);

    /**
     * Returns the last error.
     * 
     * @return returns the last error status
     */
    public long AdsGetLastError();

    /**
     * This function can be used to detect a change in the status of the TwinCAT router. The given callback function is 
     * invoked each time the status changes. Monitoring of the router's status is ended once more by the 
     * {@link #AdsAmsUnRegisterRouterNotification()} function.
     * 
     * @param pNoteFunc callback function
     * @return returns the function's error status
     */
    public long AdsAmsRegisterRouterNotification(PAmsRouterNotificationFuncEx pNoteFunc);

    /**
     * Monitoring the router's status is ended. See also 
     * {@link #AdsAmsRegisterRouterNotification(PAmsRouterNotificationFuncEx)}.
     * 
     * @return Returns the function's error status.
     */
    public long AdsAmsUnRegisterRouterNotification();

    /**
     * Returns the timeout for the ADS functions. The standard value is 5000 ms.
     * 
     * @param pnMs client buffer to store timeout in ms
     * @return Returns the function's error status.
     */
    public long AdsSyncGetTimeout(NativeLongByReference pnMs);

    /**
     * Returns status of the ADS client connection.
     * 
     * @param pbEnabled buffer to store status value.
     * @return Returns the function's error status.
     */
    public long AdsAmsPortEnabled(Pointer pbEnabled);

    //new Ads functions for multithreading applications
    
    /**
     * Multithreaded: Establishes a connection (communication port) to the TwinCAT message router.
     * 
     * @return a port number that has been assigned to the program by the ADS router is returned.
     */
    public long AdsPortOpenEx();

    /**
     * Multithreaded: The connection (communication port) to the TwinCAT message router is closed.
     * 
     * @param port Ams port of ADS client
     * @return returns the function's error status.
     */
    public long AdsPortCloseEx(long port);

    /**
     * Multithreaded: Returns the local NetId and port number.
     * 
     * @param port Ams port of ADS client
     * @param pAddr the address to modify as side-effect
     * @return the function's error status.
     */
    public long AdsGetLocalAddressEx(long port, AmsAddr pAddr);

    /**
     * Multithreaded: Writes data synchronously to an ADS device.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param length count of bytes to write
     * @param pData the data to write
     * @return returns the function's error status
     */
    public long AdsSyncWriteReqEx(long port, AmsAddr pServerAddr, long indexGroup, long indexOffset, 
        long length, Pointer pData);

    /**
     * Multithreaded: Reads data synchronously from an ADS server.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param length count of bytes to read
     * @param pData pointer to the client buffer
     * @param pcbReturn count of bytes read
     * @return returns the function's error status
     */
    public long AdsSyncReadReqEx2(long port, AmsAddr pServerAddr, long indexGroup, long indexOffset,
        long length, Pointer pData, NativeLongByReference pcbReturn);

    /**
     * Multithreaded: Writes data synchronously into an ADS server and receives data back from the ADS device.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param cbReadLength count of bytes to read
     * @param pReadData pointer to the client buffer
     * @param cbWriteLength count of bytes to write
     * @param pWriteData pointer to the client buffer
     * @param pcbReturn count of bytes read
     * @return returns the function's error status
     */
    public long AdsSyncReadWriteReqEx2(long port, AmsAddr pServerAddr, long indexGroup, long indexOffset,
        long cbReadLength, Pointer pReadData, long cbWriteLength, Pointer pWriteData, NativeLongByReference pcbReturn);

    /**
     * Multithreaded: Reads the identification and version number of an ADS server.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param pDevName fixed length string (16 Byte)
     * @param pVersion client buffer to store server version
     * @return returns the function's error status
     */
    public long AdsSyncReadDeviceInfoReqEx( long port, AmsAddr pServerAddr, ByteBuffer pDevName, AdsVersion pVersion);

    /**
     * Multithreaded: Changes the ADS status and the device status of an ADS server.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param adsState index group in ADS server interface
     * @param deviceState index offset in ADS server interface
     * @param length count of bytes to write
     * @param pData pointer to the client buffer
     * @return returns the function's error status
     */
    public long AdsSyncWriteControlReqEx(long port, AmsAddr pServerAddr, short adsState, short deviceState, 
        long length, Pointer pData);

    /**
     * Multithreaded: Reads the ADS status and the device status from an ADS server.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param pAdsState pointer to client buffer
     * @param pDeviceState pointer to the client buffer
     * @return returns the function's error status
     */
    public long AdsSyncReadStateReqEx(long port, AmsAddr pServerAddr, ShortBuffer pAdsState, ShortBuffer pDeviceState);

    /**
     * A notification is defined within an ADS server (e.g. PLC). When a certain event occurs a function (the callback 
     * function) is invoked in the ADS client (C program). Per ADS-Port a limited number of 550 notifications are 
     * available.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param indexGroup index group in ADS server interface
     * @param indexOffset index offset in ADS server interface
     * @param pNoteAttrib attributes of notification request
     * @param pNoteFunc address of notification callback
     * @param hUser user handle
     * @param pNotification pointer to notification handle (return value)
     * @return Returns the function's error status.
     */
    public long AdsSyncAddDeviceNotificationReqEx(long port, AmsAddr pServerAddr, long indexGroup, long indexOffset,
        AdsNotificationAttrib pNoteAttrib, PAdsNotificationFuncEx pNoteFunc, long hUser, 
        NativeLongByReference pNotification);
    
    /**
     * Multithreaded: A notification defined previously is deleted from an ADS server.
     * 
     * @param port Ams port of ADS client
     * @param pServerAddr Ams address of ADS server
     * @param hNotification notification handle
     * @return Returns the function's error status.
     */
    public long AdsSyncDelDeviceNotificationReqEx( long port, AmsAddr pServerAddr, long hNotification);

    /**
     * Multithreaded: Alters the timeout for the ADS functions. The standard value is 5000 ms.
     * 
     * @param port Ams port of ADS client
     * @param nMs timeout in ms
     * @return Returns the function's error status.
     */
    public long AdsSyncSetTimeoutEx(long port, long nMs);

    /**
     * Multithreaded: Returns the timeout for the ADS functions. The standard value is 5000 ms.
     * 
     * @param port Ams port of ADS client
     * @param pnMs client buffer to store timeout in ms
     * @return Returns the function's error status.
     */
    public long AdsSyncGetTimeoutEx(long port, NativeLongByReference pnMs);

    /**
     * Multithreaded: Returns status of the ADS client connection.
     * 
     * @param nPort port number of an Ads port that had previously been opened with AdsPortOpenEx or AdsPortOpen.
     * @param pbEnabled buffer to store status value.
     * @return Returns the function's error status.
     */
    public long AdsAmsPortEnabledEx(long nPort, Pointer pbEnabled);
    
    // checkstyle: resume names check
    
}
