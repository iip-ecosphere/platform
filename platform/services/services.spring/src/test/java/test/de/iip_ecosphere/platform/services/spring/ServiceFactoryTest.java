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

package test.de.iip_ecosphere.platform.services.spring;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;

/**
 * Simple test for the service factory.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceFactoryTest {

    /**
     * Tests the service factory reading from YAML.
     */
    @Test
    public void testFactory() {
        ServiceFactory.setYamlPath("service-mgr");
        ServiceSetup setup = ServiceFactory.getSetup();
        Assert.assertTrue(setup instanceof SpringCloudServiceSetup);
        SpringCloudServiceSetup spring = (SpringCloudServiceSetup) setup;
        Assert.assertEquals(30000, spring.getWaitingTime());
        Assert.assertEquals(false, spring.getDeleteArtifacts());
    }
    
}
