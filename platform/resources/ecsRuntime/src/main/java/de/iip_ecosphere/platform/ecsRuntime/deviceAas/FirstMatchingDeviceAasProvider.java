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
 * Implements a device AAS provider that queries all JSL-specified 
 * {@link DeviceAasProviderDescriptor} instances and takes the first non-null result from a 
 * non-{@link DeviceAasProviderDescriptor#createsMultiProvider() multi-provider}. Must be 
 * specified first in JSL file.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FirstMatchingDeviceAasProvider extends DeviceAasProvider {

    private DeviceAasProvider match = null;
    private String address = null;
    
    /**
     * Implements the JSL descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements DeviceAasProviderDescriptor {

        @Override
        public DeviceAasProvider createInstance() {
            return new FirstMatchingDeviceAasProvider();
        }

        @Override
        public boolean createsMultiProvider() {
            return true;
        }
        
    }
    
    @Override
    public String getDeviceAasAddress() {
        if (null == address) {
            Iterator<DeviceAasProviderDescriptor> iter = ServiceLoader
                .load(DeviceAasProviderDescriptor.class)
                .iterator();
            while (iter.hasNext()) {
                DeviceAasProviderDescriptor d = iter.next();
                if (!d.createsMultiProvider()) {
                    DeviceAasProvider provider = d.createInstance();
                    String ad = provider.getDeviceAasAddress();                    
                    if (null != ad) {
                        match = provider;
                        address = ad;
                    }
                }
            }
        }
        return address;
    }

    @Override
    public String getURN() {
        return null == match ? null : match.getURN();
    }

    @Override
    public String getIdShort() {
        return null == match ? null : match.getIdShort();
    }

}
