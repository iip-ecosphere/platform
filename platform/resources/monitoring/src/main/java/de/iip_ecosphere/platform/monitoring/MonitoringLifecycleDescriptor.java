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

package de.iip_ecosphere.platform.monitoring;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.transport.Transport;

/**
 * Platform lifecycle descriptor for monitoring. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoringLifecycleDescriptor implements LifecycleDescriptor {
    
    @Override
    public void startup(String[] args) {
        Transport.setTransportSetup(() -> MonitoringSetup.getInstance().getTransport());
    } 

    @Override
    public void shutdown() {
    }
    
    @Override
    public Thread getShutdownHook() {
        return null;
    }
    
    @Override
    public int priority() {
        return LifecycleDescriptor.INIT_PRIORITY;
    }
    
}
