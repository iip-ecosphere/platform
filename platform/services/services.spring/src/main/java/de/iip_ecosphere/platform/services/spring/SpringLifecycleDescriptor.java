/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;

/**
 * The lifecycle descriptor for the spring cloud service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringLifecycleDescriptor implements LifecycleDescriptor {

    @Override
    public void startup(String[] args) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Thread getShutdownHook() {
        return null; // not needed
    }

}
