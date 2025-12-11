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

package test.de.iip_ecosphere.platform.services.environment;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.ConnectorPluginDescriptor;
import de.iip_ecosphere.platform.services.environment.ServicePluginDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager.PluginFilter;
import de.iip_ecosphere.platform.support.plugins.PluginManager.PluginInfo;

/**
 * Tests {@link ServicePluginDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicePluginDescriptorTest {

    /**
     * Tests {@link ServicePluginDescriptor#getServicePluginFilter()}.
     */
    @Test
    public void testServiceFilter() {
        PluginFilter pf = ServicePluginDescriptor.getServicePluginFilter();
        Assert.assertNotNull(pf);
        Assert.assertFalse(pf.accept(new PluginInfo(new File(ServicePluginDescriptor.PLUGIN_ID_PREFIX + "myService"), 
            null, 0, null)));
        Assert.assertTrue(pf.accept(new PluginInfo(new File("anyOtherPlugin"), null, 0, null)));
    }

    /**
     * Tests {@link ServicePluginDescriptor#getConnectorAndServicePluginFilter()}.
     */
    @Test
    public void testConnectorAndServiceFilter() {
        PluginFilter pf = ServicePluginDescriptor.getConnectorAndServicePluginFilter();
        Assert.assertNotNull(pf);
        Assert.assertFalse(pf.accept(new PluginInfo(new File(ServicePluginDescriptor.PLUGIN_ID_PREFIX + "myService"), 
            null, 0, null)));
        Assert.assertFalse(pf.accept(new PluginInfo(new File(ConnectorPluginDescriptor.PLUGIN_ID_PREFIX + "myConn"), 
                null, 0, null)));
        Assert.assertTrue(pf.accept(new PluginInfo(new File("anyOtherPlugin"), null, 0, null)));
    }

}
