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

package de.oktoflow.platform.support.processInfo.oshi;

import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.plugins.SingletonPluginDescriptor;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactoryProviderDescriptor;

/**
 * The OSHI plugin descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
@ExcludeFirst // reduce priority if there is a plugin
public class OshiProcessInfoFactoryPluginDescriptor extends SingletonPluginDescriptor<ProcessInfoFactory> 
    implements ProcessInfoFactoryProviderDescriptor {
    
    /**
     * Creates the descriptor.
     */
    public OshiProcessInfoFactoryPluginDescriptor() {
        super("processInfo-oshi", null, ProcessInfoFactory.class, p -> new OshiProcessInfoFactory());
    }
    
    @Override
    public ProcessInfoFactory create() {
        return new OshiProcessInfoFactory();
    }
    
}
