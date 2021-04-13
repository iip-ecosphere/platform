package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.support.LifecycleHandler;

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

/**
 * Requires container management implementation and AAS implementation to be hooked in 
 * properly via JSL. Depending on the setup, also requires a proper setup of the network manager via JSL and may require
 * a service management implementation to be hooked in as lifecycle handler. Intended 
 * to be called from a separate project with adequate dependencies.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    /**
     * Starts the ECS runtime.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        LifecycleHandler.waitUntilEnd(args);
    }

}
