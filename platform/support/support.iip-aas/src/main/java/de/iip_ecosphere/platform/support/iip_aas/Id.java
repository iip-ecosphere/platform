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
import java.util.Optional;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

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
        IdProvider provider;
        Optional<IdProviderDescriptor> desc = ServiceLoaderUtils.findFirst(IdProviderDescriptor.class);
        if (desc.isPresent()) {
            provider = desc.get().createProvider();
        } else {
            provider = new MacIdProvider(); // fallback
        }
        String deviceId = null;
        String hostName = null;
        String ip = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
            ip = localHost.getHostAddress();
        } catch (IOException e) {
            LoggerFactory.getLogger(Id.class).error("Obtaining device ID: " + e.getMessage());
            hostName = JVM_NAME;
            ip = "";
        }
        String providerName = "?";
        if (provider.allowsConsoleOverride() && null != LifecycleHandler.getCmdArgs()) {
            String overrideId = CmdLine.getArg(LifecycleHandler.getCmdArgs(), IdProvider.ID_PARAM_NAME, null);
            if (null != overrideId) {
                deviceId = overrideId;
                providerName = "command line";
            }
        }
        if (null == deviceId) {
            deviceId = provider.provideId();
            providerName = provider.getClass().getName();
        }
        if (null == deviceId) {
            deviceId = JVM_NAME;
            providerName = "fallback";
        }
        LoggerFactory.getLogger(Id.class).info("USING id " + deviceId + " from " + providerName);
        
        DEVICE_ID = deviceId;
        DEVICE_ID_AAS = fixId(deviceId);
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
