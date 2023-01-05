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

package test.de.iip_ecosphere.platform.services.environment.spring;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.spring.YamlSetup;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Tests {@link YamlSetup} via the application.yml in src/test/resources.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlSetupTest {
    
    /**
     * Tests {@link YamlSetup}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYamlSetup() throws IOException {
        TransportSetup internal = YamlSetup.getInternalTransportSetup();
        Assert.assertNotNull(internal);
        Assert.assertEquals(8888, internal.getPort());
        Assert.assertEquals("localhost", internal.getHost());
        
        TransportSetup external = YamlSetup.getExternalTransportSetup();
        Assert.assertNotNull(external);
        Assert.assertEquals(8883, external.getPort());
        Assert.assertEquals("192.168.2.1", external.getHost());
    }

}
