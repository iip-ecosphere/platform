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

package test.de.iip_ecosphere.platform.connectors;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.IdentityTokenBuilder;

/**
 * Tests {@link ConnectorParameter} and the related builder. Data is irrelevant/fake as we test the implementation
 * holding that data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorParameterTest {

    /**
     * Tests creating connector parameters with default settings.
     */
    @Test
    public void testDefaultConnectorParameter() {
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder("aaa", 1234)
            .build();
        Assert.assertEquals("aaa", params.getHost());
        Assert.assertEquals(1234, params.getPort());
        Assert.assertEquals("", params.getApplicationId());
        Assert.assertEquals("", params.getApplicationDescription());
        Assert.assertEquals("", params.getEndpointPath());
        Assert.assertEquals(ConnectorParameter.DEFAULT_SCHEMA, params.getSchema());
        Assert.assertEquals(ConnectorParameter.DEFAULT_KEEP_ALIVE, params.getKeepAlive());
        Assert.assertEquals(ConnectorParameter.DEFAULT_NOTIFICATION_INTERVAL, params.getNotificationInterval());
        Assert.assertNull(params.getIdentityToken(ConnectorParameter.ANY_ENDPOINT));
    }
    
    /**
     * Tests the TLS setup.
     */
    @Test
    public void testTransportTlsParameter() {
        ServerAddress addr = new ServerAddress(Schema.IGNORE, "local", 1234);
        String alias = "alias";
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder(addr)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(null, params.getKeyAlias());
        Assert.assertFalse(params.getHostnameVerification());
        
        params = ConnectorParameterBuilder
            .newBuilder(addr)
            .setHostnameVerification(false)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(null, params.getKeyAlias());
        Assert.assertFalse(params.getHostnameVerification());

        params = ConnectorParameterBuilder
            .newBuilder(addr)
            .setKeyAlias(alias)
            .setHostnameVerification(true)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(alias, params.getKeyAlias());
        Assert.assertTrue(params.getHostnameVerification());
    }

    /**
     * Tests creating connector parameters with custom settings.
     */
    @Test
    public void testCustomConnectorParameter() {
        byte[] bytes = "aaa".getBytes();
        Map<String, IdentityToken> tokens = new HashMap<String, IdentityToken>();
        tokens.put("a", IdentityTokenBuilder.newBuilder("a", "b", bytes).build());
        tokens.put("i", IdentityTokenBuilder.newBuilder("i", "j", bytes).setIssuedToken(bytes, "abc").build());
        tokens.put("u", IdentityTokenBuilder.newBuilder("u", "k", bytes).setUsernameToken("me", bytes, "abc").build());
        tokens.put("x", IdentityTokenBuilder.newBuilder("u", "k", bytes).setX509Token(bytes).build());
        
        ConnectorParameter params = ConnectorParameterBuilder
            .newBuilder("aaa", 1234, Schema.TCP)
            .setApplicationInformation("aI", "aD")
            .setAutoApplicationId(false)
            .setEndpointPath("epp/")
            .setKeepAlive(2345)
            .setNotificationInterval(9999)
            .setRequestTimeout(3421)
            .setIdentities(tokens)
            .build();

        Assert.assertEquals("aaa", params.getHost());
        Assert.assertEquals(1234, params.getPort());
        Assert.assertEquals(Schema.TCP, params.getSchema());
        Assert.assertEquals("aI", params.getApplicationId());
        Assert.assertEquals("aD", params.getApplicationDescription());
        Assert.assertFalse(params.getAutoApplicationId());
        Assert.assertEquals("epp/", params.getEndpointPath());
        Assert.assertEquals(2345, params.getKeepAlive());
        Assert.assertEquals(9999, params.getNotificationInterval());
        Assert.assertEquals(3421, params.getRequestTimeout());
        Assert.assertTrue(tokens.get("a") == params.getIdentityToken("a"));
        Assert.assertTrue(tokens.get("i") == params.getIdentityToken("i"));
        Assert.assertTrue(tokens.get("u") == params.getIdentityToken("u"));
        Assert.assertTrue(tokens.get("x") == params.getIdentityToken("x"));
       
        params = ConnectorParameterBuilder.newBuilder(params, "local", null, null).build();

        Assert.assertEquals("local", params.getHost());
        Assert.assertEquals(1234, params.getPort());
        Assert.assertEquals(Schema.TCP, params.getSchema());
        Assert.assertEquals("aI", params.getApplicationId());
        Assert.assertEquals("aD", params.getApplicationDescription());
        Assert.assertFalse(params.getAutoApplicationId());
        Assert.assertEquals("epp/", params.getEndpointPath());
        Assert.assertEquals(2345, params.getKeepAlive());
        Assert.assertEquals(9999, params.getNotificationInterval());
        Assert.assertEquals(3421, params.getRequestTimeout());
        Assert.assertTrue(tokens.get("a") == params.getIdentityToken("a"));
        Assert.assertTrue(tokens.get("i") == params.getIdentityToken("i"));
        Assert.assertTrue(tokens.get("u") == params.getIdentityToken("u"));
        Assert.assertTrue(tokens.get("x") == params.getIdentityToken("x"));

        ServerAddress addr = new ServerAddress(Schema.TCP, "aaa", 1234);
        params = ConnectorParameterBuilder
            .newBuilder(addr)
            .build();
        Assert.assertEquals(addr.getHost(), params.getHost());
        Assert.assertEquals(addr.getPort(), params.getPort());
        Assert.assertEquals(addr.getSchema(), params.getSchema());
    }

}
