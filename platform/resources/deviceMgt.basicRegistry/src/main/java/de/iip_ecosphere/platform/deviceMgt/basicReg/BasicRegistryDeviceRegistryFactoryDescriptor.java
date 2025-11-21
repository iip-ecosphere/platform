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

package de.iip_ecosphere.platform.deviceMgt.basicReg;

import java.util.List;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactoryDescriptor;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The factory registry descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicRegistryDeviceRegistryFactoryDescriptor 
    extends SingletonPluginDescriptor<DeviceRegistry> implements DeviceRegistryFactoryDescriptor {

    /**
     * Creates the instance via JSL.
     */
    public BasicRegistryDeviceRegistryFactoryDescriptor() {
        super(PLUGIN_ID, List.of(PLUGIN_ID_PREFIX + "basic"), DeviceRegistry.class, null);
    }
    
    @Override
    protected PluginSupplier<DeviceRegistry> initPluginSupplier(PluginSupplier<DeviceRegistry> pluginSupplier) {
        return p -> createDeviceRegistryInstance();
    }

    @Override
    public DeviceRegistry createDeviceRegistryInstance() {
        return new BasicDeviceRegistry();
    }

}
