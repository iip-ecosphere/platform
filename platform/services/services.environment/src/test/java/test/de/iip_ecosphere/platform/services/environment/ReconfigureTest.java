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

package test.de.iip_ecosphere.platform.services.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ParameterConfigurer;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import org.junit.Assert;

/**
 * Tests {@link AbstractService#reconfigure(java.util.Map, java.util.Map, boolean, 
 * de.iip_ecosphere.platform.services.environment.ServiceState)}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ReconfigureTest {

    private int param1;
    private String param2;
    private int param3;

    /**
     * Tests a failing reconfiguration with recovery, i.e., nothing shall happen.
     */
    @Test
    public void testReconfigureFailWithRecovery() {
        param1 = 0;
        param2 = null;
        param3 = 10;

        Map<String, ParameterConfigurer<?>> configurers = new HashMap<>();
        AbstractService.addConfigurer(configurers, "param1", Integer.class, TypeTranslators.INTEGER, 
            v -> param1 = v, () -> param1);
        AbstractService.addConfigurer(configurers, "param2", String.class, TypeTranslators.STRING, 
            v -> param2 = v, () -> param2);
        AbstractService.addConfigurer(configurers, "param3", Integer.class, TypeTranslators.INTEGER, 
            v -> param3 = v);

        Map<String, String> values = new TreeMap<>();
        values.put("param1", "20");
        values.put("param2", "abba");
        values.put("param3", "abba"); // shall fail and lead to recovery

        try {
            AbstractService.reconfigure(values, n -> configurers.get(n), true, ServiceState.AVAILABLE);
            Assert.fail();
        } catch (ExecutionException e) {
            // this is ok
        }
        
        Assert.assertEquals(0, param1);
        Assert.assertNull(param2);
        Assert.assertEquals(10, param3);
    }

    /**
     * Tests a failing reconfiguration without recovery, i.e., some values may be changed or not.
     */
    @Test
    public void testReconfigureFailWithoutRecovery() {
        param1 = 0;
        param2 = null;
        param3 = 10;

        Map<String, ParameterConfigurer<?>> configurers = new HashMap<>();
        AbstractService.addConfigurer(configurers, "param1", Integer.class, TypeTranslators.INTEGER, 
            v -> param1 = v, () -> param1);
        AbstractService.addConfigurer(configurers, "param2", String.class, TypeTranslators.STRING, 
            v -> param2 = v, () -> param2);
        AbstractService.addConfigurer(configurers, "param3", Integer.class, TypeTranslators.INTEGER, 
            v -> param3 = v);

        Map<String, String> values = new HashMap<>();
        values.put("param1", "20");
        values.put("param2", "abba");
        values.put("param3", "abba"); // shall fail and lead to recovery

        try {
            AbstractService.reconfigure(values, n -> configurers.get(n), false, ServiceState.AVAILABLE);
            Assert.fail();
        } catch (ExecutionException e) {
            // this is ok
        }
        
        Assert.assertTrue(param1 == 0 || param1 == 20);
        Assert.assertTrue(param2 == null || param2.equals("abba"));
        Assert.assertEquals(10, param3); // cannot be changed, will fail
    }

    /**
     * Tests a successful reconfiguration without.
     */
    @Test
    public void testReconfigureSuccessful() {
        param1 = 0;
        param2 = null;
        param3 = 10;

        Map<String, ParameterConfigurer<?>> configurers = new HashMap<>();
        AbstractService.addConfigurer(configurers, "param1", Integer.class, TypeTranslators.INTEGER, 
            v -> param1 = v, () -> param1);
        AbstractService.addConfigurer(configurers, "param2", String.class, TypeTranslators.STRING, 
            v -> param2 = v, () -> param2);
        AbstractService.addConfigurer(configurers, "param3", Integer.class, TypeTranslators.INTEGER, 
            v -> param3 = v);

        Map<String, String> values = new HashMap<>();
        configurers.get("param1").addValue(values, 20);
        configurers.get("param2").addValue(values, "abba");
        configurers.get("param3").addValue(values, -1);

        try {
            AbstractService.reconfigure(values, n -> configurers.get(n), true, ServiceState.AVAILABLE);
        } catch (ExecutionException e) {
            Assert.fail();
        }
        
        Assert.assertEquals(20, param1);
        Assert.assertEquals("abba", param2);
        Assert.assertEquals(-1, param3);

        param1 = 0;
        param2 = null;
        param3 = 10;
    
        try {
            AbstractService.reconfigure(values, n -> configurers.get(n), false, ServiceState.AVAILABLE);
        } catch (ExecutionException e) {
            Assert.fail();
        }
        
        Assert.assertEquals(20, param1);
        Assert.assertEquals("abba", param2);
        Assert.assertEquals(-1, param3);
    }
    
    /**
     * Tests whether reconfiguration with certain <b>null</b> values does not harm.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testReconfigureNull() throws ExecutionException {
        AbstractService.reconfigure(null, null, false, ServiceState.AVAILABLE);
    }
    
}
