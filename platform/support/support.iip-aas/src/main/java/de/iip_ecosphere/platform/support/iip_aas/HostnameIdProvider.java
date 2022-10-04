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

package de.iip_ecosphere.platform.support.iip_aas;

import de.iip_ecosphere.platform.support.NetUtils;

/**
 * A device ID provider based on the hostname of the device.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HostnameIdProvider implements IdProvider {

    /**
     * Implements the provider descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class HostnameIdProviderDescriptor implements IdProviderDescriptor {

        @Override
        public IdProvider createProvider() {
            return new HostnameIdProvider();
        }
        
    }
    
    @Override
    public String provideId() {
        return NetUtils.getOwnHostname();
    }

    @Override
    public boolean allowsConsoleOverride() {
        return true; // enabled, in particular for debugging
    }

}
