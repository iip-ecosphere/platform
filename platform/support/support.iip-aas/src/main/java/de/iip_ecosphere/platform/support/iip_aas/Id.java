/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;

/**
 * Generic IDs for different purposes. For now, the IDs are determined upon startup, but they may be also determined
 * through configurable system properties later if needed.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Id {

    private static final String JVM_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final String JVM_NAME_AAS = fixId(JVM_NAME);
    private static final String DEVICE_ID;
    private static final String DEVICE_ID_AAS;
    private static final String DEVICE_NAME;
    private static final String DEVICE_NAME_AAS;
    private static final String DEVICE_IP;
    private static final String DEVICE_IP_AAS;
    
    static {
        String hostName = null;
        String macAddress = null;
        String ip = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            if (ni == null) { // Ubuntu :(
                // https://stackoverflow.com/questions/23900172/how-to-get-localhost-network-interface-in-java-or-scala
                Enumeration<NetworkInterface> ne = NetworkInterface.getNetworkInterfaces();
                while (ne.hasMoreElements()) {
                    ni = ne.nextElement();
                    break;
                }
            }
            if (null != ni) {
                byte[] hardwareAddress = ni.getHardwareAddress();
                String[] hexadecimal = new String[hardwareAddress.length];
                for (int i = 0; i < hardwareAddress.length; i++) {
                    hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                }
                macAddress = String.join("", hexadecimal);
            } else {
                macAddress = JVM_NAME;    
            }
            hostName = localHost.getHostName();
            ip = localHost.getHostAddress();
        } catch (IOException e) {
            LoggerFactory.getLogger(Id.class).error("Obtaining device ID: " + e.getMessage());
            hostName = JVM_NAME;
            macAddress = JVM_NAME;
            ip = "";
        }
        DEVICE_ID = macAddress;
        DEVICE_ID_AAS = fixId(macAddress);
        DEVICE_NAME = hostName;
        DEVICE_NAME_AAS = fixId(hostName);
        DEVICE_IP = ip;
        DEVICE_IP_AAS = fixId(ip);
    }
    
    /**
     * Returns a unique ID for the runtime environment.
     * 
     * @return the unique ID
     */
    public static final String getEnvId() {
        return JVM_NAME;
    }

    /**
     * Returns a unique ID for the runtime environment as valid AAS identifier.
     * 
     * @return the unique ID for AAS
     */
    public static final String getEnvIdAas() {
        return JVM_NAME_AAS;
    }

    /**
     * Returns a unique ID for the device.
     * 
     * @return the unique ID (may be {@link #getEnvId()} for no network)
     */
    public static final String getDeviceId() {
        return DEVICE_ID;
    }

    /**
     * Returns a unique ID for the device as valid AAS identifier.
     * 
     * @return the unique ID for AAS (may be {@link #getEnvIdAas()} for no network)
     */
    public static final String getDeviceIdAas() {
        return DEVICE_ID_AAS;
    }

    /**
     * Returns the device name.
     * 
     * @return the unique ID (may be {@link #getEnvId()} for no network)
     */
    public static final String getDeviceName() {
        return DEVICE_NAME;
    }

    /**
     * Returns the device name as valid AAS identifier.
     * 
     * @return the device name for AAS (may be {@link #getEnvIdAas()} for no network)
     */
    public static final String getDeviceNameAas() {
        return DEVICE_NAME_AAS;
    }

    /**
     * Returns the device IP address.
     * 
     * @return the IP address (may be empty for no network)
     */
    public static final String getDeviceIp() {
        return DEVICE_IP;
    }

    /**
     * Returns the device IP address as valid AAS identifier.
     * 
     * @return the device IP address for AAS (may be empty for no network)
     */
    public static final String getDeviceIpAas() {
        return DEVICE_IP_AAS;
    }

}
