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
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(1235, params.getActionTimeout());
        Assert.assertEquals("app", params.getApplicationId());
        Assert.assertEquals(false, params.getAutoApplicationId());
        Assert.assertEquals(1236, params.getKeepAlive());
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
