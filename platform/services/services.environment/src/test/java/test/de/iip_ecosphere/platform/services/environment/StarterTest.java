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

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

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
    
    /**
     * Asserts that accessing the state of a non-existent service in the service environment does not fail.
     */
    @Test
    public void testFailingServiceCreation() {
        final String protocol = "";
        final int port = NetUtils.getEphemeralPort();
        
        Starter.main(new String[] {
            Starter.composeArgument(Starter.PARAM_IIP_PROTOCOL, protocol),
            Starter.composeArgument(Starter.PARAM_IIP_PORT, String.valueOf(port))    
        });
        boolean notifyOld = Starter.getSetup().getNotifyServiceNull();
        Starter.getSetup().setNotifyServiceNull(false); // prevent exception output
        Starter.mapService(null); // service does not exist
        Starter.getSetup().setNotifyServiceNull(notifyOld);
        InvocablesCreator iCreator = AasFactory.getInstance().createInvocablesCreator(protocol, "localhost", port);
        ServiceStub stub = new ServiceStub(iCreator, "1234"); // service does not exist
        Assert.assertNull(stub.getState());
        Assert.assertEquals("", stub.getDescription());
        Assert.assertEquals("", stub.getId());
        Assert.assertNull(stub.getKind());
        Assert.assertNull(stub.getVersion());
        Assert.assertFalse(stub.isDeployable());
        System.out.println("The following exception(s)/failing operations are intended!!!");
        try {
            stub.setState(ServiceState.ACTIVATING); // goes for 1min recovery
            Assert.fail();
        } catch (ExecutionException e) {
            // this is ok
        }
        try {
            stub.activate();
            Assert.fail();
        } catch (ExecutionException e) {
        }
        try {
            stub.passivate();
            Assert.fail();
        } catch (ExecutionException e) {
        }
        try {
            stub.update(new File("").toURI());
            Assert.fail();
        } catch (ExecutionException e) {
        }
        try {
            stub.switchTo("id");
            Assert.fail();
        } catch (ExecutionException e) {
        }
        try {
            stub.reconfigure(new HashMap<String, String>());
            Assert.fail();
        } catch (ExecutionException e) {
        }
    }

}
