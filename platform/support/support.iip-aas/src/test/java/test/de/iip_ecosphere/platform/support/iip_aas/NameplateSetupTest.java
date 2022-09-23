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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup.Service;

/**
 * Tests {@link NameplateSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NameplateSetupTest {

    /**
     * Tests {@link NameplateSetup}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testNameplateSetup() throws IOException {
        NameplateSetup init = NameplateSetup.obtainNameplateSetup();
        Aas aas = init.createAas("urn:::AAS:::a1234#", "a1234");
        NameplateSetup setup = NameplateSetup.readFromAas(aas);
        Assert.assertNotNull(setup);
        // TODO further asserts
        Assert.assertNotNull(setup.getServices());
        Map<String, Service> services = NameplateSetup.getServicesAsMap(setup.getServices());
        Assert.assertEquals(2, services.size());
        
        Service s = services.get("opcua");
        Assert.assertNotNull(s);
        Assert.assertEquals("opcua", s.getKey());
        Assert.assertEquals(4840, s.getPort());
        Assert.assertTrue(s.getHost().length() > 0);
        Assert.assertNull(s.getNetmask());
        Assert.assertNull(s.getVersion());

        s = services.get("mqtt");
        Assert.assertNotNull(s);
        Assert.assertEquals("mqtt", s.getKey());
        Assert.assertEquals(1883, s.getPort());
        Assert.assertTrue(s.getHost().length() > 0);
        Assert.assertNotNull(s.getNetmask());
        Assert.assertTrue(s.getNetmask().length() > 0);
        Assert.assertNotNull(s.getVersion());
        Assert.assertEquals("5", s.getVersion().toString());
    }

}
