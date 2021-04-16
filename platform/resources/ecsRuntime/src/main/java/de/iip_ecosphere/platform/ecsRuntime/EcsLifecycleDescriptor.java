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

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;

/**
 * The basic ECS lifecycle descriptor for powering up the AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsLifecycleDescriptor extends AbstractAasLifecycleDescriptor {

    /**
     * Creates an instance for the service manager.
     */
    public EcsLifecycleDescriptor() {
        super("ECS", () -> EcsFactory.getConfiguration().getAas());
    }

    @Override
    public void startup(String[] args) {
        System.out.println("IIP-Ecosphere ECS Runtime.");
        super.startup(args);
    }
    
}
