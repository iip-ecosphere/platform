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

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import org.junit.Assert;

/**
 * Tests the {@link Starter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StarterTest {
    
    /**
     * Tests the starter.
     */
    @Test
    public void testStarter() {
        String serviceId = "my Service - 1"; // shall be given without space -> normalize below
        assertStringContaining(Starter.getServiceCommandNetworkMgrKey(serviceId), serviceId);
        assertStringContaining(Starter.getServiceProcessNetworkMgrKey(serviceId), serviceId);
        
        int port = NetUtils.getEphemeralPort();
        Starter.parse("file", "--myParam", 
            Starter.composeArgument(Starter.PARAM_IIP_PROTOCOL, AasFactory.DEFAULT_PROTOCOL), 
            Starter.composeArgument(Starter.PARAM_IIP_PORT, port),
            Starter.composeArgument(Starter.getServicePortName(serviceId), 12345),
            "--endParam");
        Assert.assertNotNull(Starter.getProtocolBuilder());
        Assert.assertEquals(12345, Starter.getServicePort(serviceId));
        Assert.assertEquals(-1, Starter.getServicePort("unknown"));
        Starter.start();
        Starter.shutdown();
    }
    
    /**
     * Asserts that {@code str} is a non-empty string.
     * 
     * @param str the string to assert
     */
    private static void assertString(String str) {
        Assert.assertNotNull(str);
        Assert.assertTrue(str.length() > 0);
    }

    /**
     * Asserts that {@code str} is a non-empty string containing {@code expected}.
     * 
     * @param str the string to assert
     * @param expected expected substring of  {@code str}
     */
    private static void assertStringContaining(String str, String expected) {
        assertString(str);
        Assert.assertTrue(str.indexOf(expected) > 0);
    }

}
