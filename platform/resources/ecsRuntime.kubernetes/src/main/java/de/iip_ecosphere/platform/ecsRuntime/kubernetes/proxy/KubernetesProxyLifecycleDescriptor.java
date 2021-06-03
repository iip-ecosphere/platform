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

package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;

/**
 * Defines the lifecycle for the Kubernetes proxy. A deployment instantiation can decide which lifecycle
 * descriptors to execute and, thus, may either container manager or proxy optional.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KubernetesProxyLifecycleDescriptor implements LifecycleDescriptor {

    @Override
    public void startup(String[] args) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Thread getShutdownHook() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int priority() {
        return INIT_PRIORITY;
    }

}
