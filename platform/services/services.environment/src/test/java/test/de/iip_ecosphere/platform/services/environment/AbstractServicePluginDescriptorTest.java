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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.AbstractGenericMultiServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.AbstractGenericServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.AbstractServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.AbstractSpecificServicePluginDescriptor;
import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Tests {@link AbstractServicePluginDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractServicePluginDescriptorTest {
    
    /**
     * Tests {@link AbstractService#getArg(int, Object[], Object)} and 
     * {@link AbstractService#getStringArg(int, Object[], String)}.
     */
    @Test
    public void testArgs() {
        Assert.assertEquals(1, AbstractService.getArg(-1, null, 1));
        Assert.assertEquals(1, AbstractService.getArg(0, null, 1));
        Assert.assertEquals(2, AbstractService.getArg(0, new Object[0], 2));
        
        Object[] tmp = {1, 2};
        Assert.assertEquals(tmp[0], AbstractService.getArg(0, tmp, 7));
        Assert.assertEquals(tmp[1], AbstractService.getArg(1, tmp, 7));
        
        tmp = new Object[] {"abba", 2};
        Assert.assertEquals(tmp[0], AbstractService.getStringArg(0, tmp, null));
        Assert.assertEquals(String.valueOf(tmp[1]), AbstractService.getStringArg(1, tmp, null));
        Assert.assertEquals("1", AbstractService.getStringArg(2, tmp, "1"));
    }
    
    /**
     * A service plugin descriptor for testing.
     * 
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
        public <I, O> Service createService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
            ReceptionCallback<O> callback, YamlService yaml, Object... args) {
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
     * A generic SISO service plugin descriptor for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Desc3 extends AbstractGenericServicePluginDescriptor<Service> {

        /**
         * Creates an instance.
         */
        public Desc3() {
            super(PLUGIN_ID_PREFIX + "3", List.of());
        }
        
        @Override
        public <I, O> Service createService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
            ReceptionCallback<O> callback, YamlService yaml, Object... args) {
            return null;
        }
        
    }

    /**
     * A generic MIMO service plugin descriptor for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Desc4 extends AbstractGenericMultiServicePluginDescriptor<Service> {

        /**
         * Creates an instance.
         */
        public Desc4() {
            super(PLUGIN_ID_PREFIX + "4", List.of());
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
        Assert.assertNull(d2.createService((TypeTranslator<Object, String>) null, null, null, (YamlService) null));

        Desc3 d3 = new Desc3();
        Assert.assertNotNull(d3.create());
        Assert.assertNull(d3.createService());
        Assert.assertNull(d3.createService(""));
        Assert.assertNull(d3.createService("", null));
        Assert.assertNull(d3.createService((YamlService) null));

        Desc4 d4 = new Desc4();
        Assert.assertNotNull(d4.create());
        Assert.assertNull(d4.createService());
        Assert.assertNull(d4.createService(""));
        Assert.assertNull(d4.createService("", null));
        Assert.assertNull(d4.createService((TypeTranslator<Object, String>) null, null, null, (YamlService) null));
    }

}
