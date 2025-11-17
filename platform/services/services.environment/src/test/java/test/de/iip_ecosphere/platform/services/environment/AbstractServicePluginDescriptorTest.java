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

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.AbstractGenericServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.AbstractServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.AbstractSpecificServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.YamlService;

/**
 * Tests {@link AbstractServicePluginDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractServicePluginDescriptorTest {
    
    /**
     * Tests {@link AbstractServicePluginDescriptor#getArg(int, Object[], Object)} and 
     * {@link AbstractServicePluginDescriptor#getStringArg(int, Object[], String)}.
     */
    @Test
    public void testArgs() {
        Assert.assertEquals(1, AbstractServicePluginDescriptor.getArg(-1, null, 1));
        Assert.assertEquals(1, AbstractServicePluginDescriptor.getArg(0, null, 1));
        Assert.assertEquals(2, AbstractServicePluginDescriptor.getArg(0, new Object[0], 2));
        
        Object[] tmp = {1, 2};
        Assert.assertEquals(tmp[0], AbstractServicePluginDescriptor.getArg(0, tmp, 7));
        Assert.assertEquals(tmp[1], AbstractServicePluginDescriptor.getArg(1, tmp, 7));
        
        tmp = new Object[] {"abba", 2};
        Assert.assertEquals(tmp[0], AbstractServicePluginDescriptor.getStringArg(0, tmp, null));
        Assert.assertEquals(String.valueOf(tmp[1]), AbstractServicePluginDescriptor.getStringArg(1, tmp, null));
        Assert.assertEquals("1", AbstractServicePluginDescriptor.getStringArg(2, tmp, "1"));
    }
    
    /**
     * A service plugin descriptor for testing.
     * 
     * @param <S> the service type
     * @author Holger Eichelberger, SSE
     */
    private static class Desc1 extends AbstractServicePluginDescriptor<Service> {

        /**
         * Creates an instance.
         */
        public Desc1() {
            super(PLUGIN_ID_PREFIX + "1", null);
        }

        @Override
        public Service createService(YamlService yaml, Object... args) {
            return null;
        }

        @Override
        public Service createService(String serviceId, InputStream ymlFile) {
            return null;
        }

        @Override
        public Service createService(String serviceId) {
            return null;
        }

        @Override
        public Service createService() {
            return null;
        }
        
    }

    /**
     * A specific service plugin descriptor for testing.
     * 
     * @param <S> the service type
     * @author Holger Eichelberger, SSE
     */
    private static class Desc2 extends AbstractSpecificServicePluginDescriptor<Service> {

        /**
         * Creates an instance.
         */
        public Desc2() {
            super(PLUGIN_ID_PREFIX + "2", null);
        }

        @Override
        public Service createService(String serviceId, InputStream ymlFile) {
            return null;
        }

        @Override
        public Service createService(String serviceId) {
            return null;
        }

        @Override
        public Service createService() {
            return null;
        }
        
    }

    /**
     * A generic service plugin descriptor for testing.
     * 
     * @param <S> the service type
     * @author Holger Eichelberger, SSE
     */
    private static class Desc3 extends AbstractGenericServicePluginDescriptor<Service> {

        /**
         * Creates an instance.
         */
        public Desc3() {
            super(PLUGIN_ID_PREFIX + "3", null);
        }

        @Override
        public Service createService(YamlService yaml, Object... args) {
            return null;
        }
        
    }

    /**
     * Tests the abstract service descriptors and their generic implementations.
     */
    @Test
    public void testInitPluginClass() {
        Desc1 d1 = new Desc1();
        Assert.assertNotNull(d1.create());

        Desc2 d2 = new Desc2();
        Assert.assertNotNull(d2.create());
        Assert.assertNull(d2.createService((YamlService) null));

        Desc3 d3 = new Desc3();
        Assert.assertNotNull(d3.create());
        Assert.assertNull(d3.createService());
        Assert.assertNull(d3.createService(""));
        Assert.assertNull(d3.createService("", null));
    }
    

}
