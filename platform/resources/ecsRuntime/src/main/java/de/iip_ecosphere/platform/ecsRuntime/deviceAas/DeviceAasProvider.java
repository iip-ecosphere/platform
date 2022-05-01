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

package de.iip_ecosphere.platform.ecsRuntime.deviceAas;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Describes a device AAS provider. 
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class DeviceAasProvider {
    
    private static DeviceAasProvider instance;
    
    /**
     * Returns the device AAS address for this device.
     * 
     * @return the device AAS address, may be <b>null</b> if there is no AAS
     */
    public abstract String getDeviceAasAddress();

    /**
     * Returns the URN of the device AAS.
     *  
     * @return the (envisioned) URN, may be <b>null</b>, if there is no AAS
     */
    public abstract String getURN();
    
    /**
     * Returns the short ID of the device AAS.
     *  
     * @return the (envisioned) short ID, may be <b>null</b>, if there is no AAS
     */
    public abstract String getIdShort();
    
    /**
     * Returns the actual provider instance. Either the first multi-provider delivered by
     * JSL (with precedence) or the first single-provider is returned and installed. If
     * no descriptor is specified, the {@link YamlDeviceAasProvider} is used as fallback.
     * 
     * @return the provider instance
     */
    public static DeviceAasProvider getInstance() {
        if (null == instance) {
            DeviceAasProviderDescriptor multi = null;
            DeviceAasProviderDescriptor single = null;
            Iterator<DeviceAasProviderDescriptor> iter = ServiceLoader
                .load(DeviceAasProviderDescriptor.class)
                .iterator();
            while (iter.hasNext()) {
                DeviceAasProviderDescriptor d = iter.next();
                if (d.createsMultiProvider()) {
                    if (null == multi) {
                        multi = d;
                    }
                } else {
                    if (null == single) {
                        single = d;
                    }
                }
            }
            if (multi != null) {
                instance = multi.createInstance();
            } else if (single != null) {
                instance = single.createInstance();
            } else {
                instance = new YamlDeviceAasProvider();
            }
        }
        return instance; 
    }

}
