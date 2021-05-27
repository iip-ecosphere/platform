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

package test.de.iip_ecosphere.platform.platform;

import org.junit.Test;

import de.iip_ecosphere.platform.platform.PlatformConfiguration;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;

/**
 * Platform test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformTest {
    
    /**
     * Simple platform test.
     */
    @Test
    public void testPlatform() {
        PlatformConfiguration cfg = PlatformConfiguration.getInstance();
        cfg.setAas(AasSetup.createLocalEphemeralSetup(false));
        LifecycleHandler.startup(new String[] {});
        // check server instances
        LifecycleHandler.shutdown();
    }
    
}
