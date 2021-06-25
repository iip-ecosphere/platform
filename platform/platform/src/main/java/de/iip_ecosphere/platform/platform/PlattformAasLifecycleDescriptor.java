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

package de.iip_ecosphere.platform.platform;

import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;

/**
 * This descriptor is responsible for creating the AAS of the platform.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlattformAasLifecycleDescriptor extends AbstractAasLifecycleDescriptor {

    /**
     * Creates AAS an instance for the service manager.
     */
    public PlattformAasLifecycleDescriptor() {
        super("Platform", () -> PlatformConfiguration.getInstance().getAas());
    }

    @Override
    public void startup(String[] args) {
        System.out.println("IIP-Ecosphere Platform Server.");
        super.startup(args);
    }
}
