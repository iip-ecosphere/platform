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

package de.iip_ecosphere.platform.ecsRuntime;

import java.util.Optional;

import de.iip_ecosphere.platform.ecsRuntime.deviceAas.YamlDeviceAasProvider;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Describes a simple device Aas provider.
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
     * Returns the default provider instance.
     * 
     * @return the default provider instance
     */
    public static DeviceAasProvider getInstance() {
        if (null == instance) {
            Optional<DeviceAasProviderDescriptor> desc 
                = ServiceLoaderUtils.findFirst(DeviceAasProviderDescriptor.class);
            if (desc.isPresent()) {
                instance = desc.get().createInstance();
            } else {
                instance = new YamlDeviceAasProvider();
            }
        }
        return instance; 
    }

}
