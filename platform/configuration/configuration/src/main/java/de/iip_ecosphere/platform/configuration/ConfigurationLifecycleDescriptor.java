/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration;

import de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactory;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;

/**
 * Delegating lifecycle descriptor, if available delegates to {@link ConfigurationFactory#getLifecycleDescriptor()}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationLifecycleDescriptor implements LifecycleDescriptor {

    @Override
    public void startup(String[] args) {
        LifecycleDescriptor desc = ConfigurationFactory.getLifecycleDescriptor();
        if (null != desc) {
            desc.startup(args);
        }
    }

    @Override
    public void shutdown() {
        LifecycleDescriptor desc = ConfigurationFactory.getLifecycleDescriptor();
        if (null != desc) {
            desc.shutdown();
        }
    }

    @Override
    public Thread getShutdownHook() {
        Thread hook = null;
        LifecycleDescriptor desc = ConfigurationFactory.getLifecycleDescriptor();
        if (null != desc) {
            hook = desc.getShutdownHook();
        }
        return hook;
    }

    @Override
    public int priority() {
        int priority = INIT_PRIORITY;
        LifecycleDescriptor desc = ConfigurationFactory.getLifecycleDescriptor();
        if (null != desc) {
            priority = desc.priority();
        }
        return priority;
    }

}
