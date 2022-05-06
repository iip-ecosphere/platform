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

package test.de.iip_ecosphere.platform.monitoring;

import org.junit.Test;

import de.iip_ecosphere.platform.monitoring.MonitoringSetup;
import org.junit.Assert;

/**
 * Tests {@link MonitoringSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoringSetupTest {
    
    /**
     * Rather basic setup test.
     */
    @Test
    public void testSetup() {
        // loading already tested
        MonitoringSetup setup = MonitoringSetup.getInstance();
        Assert.assertNotNull(setup);
        Assert.assertNotNull(setup.getTransport()); // already tested
        Assert.assertNotNull(setup.getAas());  // already tested
    }

}
