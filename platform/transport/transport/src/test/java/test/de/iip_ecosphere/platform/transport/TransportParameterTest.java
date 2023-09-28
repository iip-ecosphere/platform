/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter.CloseAction;
import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

import org.junit.Assert;

/**
 * Tests transport parameter settings and some common methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportParameterTest {
    
    /**
     * Tests transport parameter settings.
     */
    @Test
    public void testTransportParameter() {
        ServerAddress addr = new ServerAddress(Schema.IGNORE, "local", 1234);
        TransportParameter params = TransportParameter.TransportParameterBuilder
            .newBuilder(addr)
            .setActionTimeout(1235)
            .setApplicationId("app")
            .setAutoApplicationId(false)
            .setKeepAlive(1236)
            .setMqttQoS(null) // no effect
            .setMqttQoS(MqttQoS.EXACTLY_ONCE)
            .setCloseAction(null) // no effect
            .setCloseAction(CloseAction.NONE) // no effect
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(1235, params.getActionTimeout());
        Assert.assertEquals("app", params.getApplicationId());
        Assert.assertEquals(false, params.getAutoApplicationId());
        Assert.assertEquals(1236, params.getKeepAlive());
        Assert.assertEquals(null, params.getKeystoreKey());
        Assert.assertEquals(MqttQoS.EXACTLY_ONCE, params.getMqttQoS());
        Assert.assertEquals(CloseAction.NONE, params.getCloseAction());
        
        params = TransportParameter.TransportParameterBuilder.newBuilder(params)
            .setMqttQoS(MqttQoS.AT_LEAST_ONCE)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(1235, params.getActionTimeout());
        Assert.assertEquals("app", params.getApplicationId());
        Assert.assertEquals(false, params.getAutoApplicationId());
        Assert.assertEquals(1236, params.getKeepAlive());
        Assert.assertEquals(null, params.getKeystoreKey());
        Assert.assertEquals(MqttQoS.AT_LEAST_ONCE, params.getMqttQoS());
        Assert.assertEquals(CloseAction.NONE, params.getCloseAction());
    }
    
    /**
     * Tests the TLS setup.
     */
    @Test
    public void testTransportTlsParameter() {
        ServerAddress addr = new ServerAddress(Schema.IGNORE, "local", 1234);
        String alias = "alias";
        TransportParameter params = TransportParameter.TransportParameterBuilder
            .newBuilder(addr)
            .setKeystoreKey(null)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(null, params.getKeystoreKey());
        Assert.assertEquals(null, params.getKeyAlias());
        Assert.assertFalse(params.getHostnameVerification());
        
        params = TransportParameter.TransportParameterBuilder
            .newBuilder(addr)
            .setKeystoreKey("myKeystore")
            .setHostnameVerification(false)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals("myKeystore", params.getKeystoreKey());
        Assert.assertEquals(null, params.getKeyAlias());
        Assert.assertFalse(params.getHostnameVerification());

        params = TransportParameter.TransportParameterBuilder
            .newBuilder(addr)
            .setKeystoreKey("myKeystore")
            .setKeyAlias(alias)
            .setHostnameVerification(true)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals("myKeystore", params.getKeystoreKey());
        Assert.assertEquals(alias, params.getKeyAlias());
        Assert.assertTrue(params.getHostnameVerification());
    }

    /**
     * Tests {@link AbstractTransportConnector#getApplicationId(String, String, boolean)}. This is a bit out
     * of place, but tested here as a common method.
     */
    @Test
    public void testApplicationId() {
        Assert.assertEquals("", AbstractTransportConnector.getApplicationId(null, null, false));
        Assert.assertEquals("", AbstractTransportConnector.getApplicationId("", null, false));
        Assert.assertEquals("", AbstractTransportConnector.getApplicationId(null, "", false));
        Assert.assertEquals("", AbstractTransportConnector.getApplicationId("", "", false));
        Assert.assertEquals("a", AbstractTransportConnector.getApplicationId("a", "", false));
        Assert.assertEquals("a-b", AbstractTransportConnector.getApplicationId("a", "b", false));
        Assert.assertEquals("b", AbstractTransportConnector.getApplicationId("", "b", false));
        Assert.assertTrue(AbstractTransportConnector.getApplicationId("a", "b", true).startsWith("a-b-"));
    }

}
