/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.util.List;

import de.iip_ecosphere.platform.ecsRuntime.EcsFactoryDescriptor;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;

/**
 * The container plugin descriptor returning the implemented {@link EcsFactoryDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LxcContainerPluginDescriptor extends SingletonPluginDescriptor<EcsFactoryDescriptor> {

    /**
     * Creates the instance via JSL.
     */
    public LxcContainerPluginDescriptor() {
        super(EcsFactoryDescriptor.PLUGIN_ID, List.of(EcsFactoryDescriptor.PLUGIN_ID_PREFIX + "LXC"), 
            EcsFactoryDescriptor.class, p -> new LxcContainerManager.FactoryDescriptor());
    }

}
