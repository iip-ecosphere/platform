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

package test.de.iip_ecosphere.platform.support.iip_aas;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.iip_aas.HostnameIdProvider;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.IdProvider;
import de.iip_ecosphere.platform.support.iip_aas.IdProviderDescriptor;
import test.de.iip_ecosphere.platform.support.LifecycleHandlerTest;

/**
 * Tests {@code Id} with startup command line (requires a separate virtual machine).
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdTest2 {
    
    /**
     * Tests {@code Id}.
     */
    @Test
    public void testId() {
        String[] args = new String[] {"--" + IdProvider.ID_PARAM_NAME + "=a789"};
        LifecycleHandlerTest.setCmdArgs(args);
        LifecycleHandler.startup(args);
        Assert.assertEquals("a789", Id.getDeviceId());
        Assert.assertNotNull("a789", Id.getDeviceIdAas()); // same as we start with character
    }
    
    /**
     * Tests {@link HostnameIdProvider}.
     */
    @Test
    public void testHostnameIdProvider() {
        IdProviderDescriptor desc = new HostnameIdProvider.HostnameIdProviderDescriptor();
        
        IdProvider provider = desc.createProvider();
        Assert.assertNotNull(provider);
        
        provider.allowsConsoleOverride(); // does not matter, don't fix to true here
        String id = provider.provideId();
        Assert.assertNotNull(id);
        Assert.assertTrue(id.length() > 0);
        
        String id2 = provider.provideId();
        Assert.assertEquals(id, id2); // shall remain stable
    }

}
