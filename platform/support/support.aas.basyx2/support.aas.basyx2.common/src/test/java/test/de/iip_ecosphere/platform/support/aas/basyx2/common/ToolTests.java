/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx2.common;

import java.io.IOException;
import java.net.http.HttpClient;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.aas.SetupSpec.State;
import de.iip_ecosphere.platform.support.aas.basyx2.common.Tools;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Tests {@link Tools}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ToolTests {

    private class Api {
        
        private String uri;
        
        /**
         * Creates an API without URI.
         */
        private Api() {
        }
        
        /**
         * Creates a wrapped API taking over the URI.
         * 
         * @param api the API to "wrap"
         */
        private Api(Api api) {
            this.uri = api.uri;
        }

        /**
         * Configures the URI.
         * 
         * @param uri the URI
         */
        private void setUri(String uri) {
            this.uri = uri;
        }
        
    }

    /**
     * Tests {@link Tools).
     */
    @Test
    public void testCreateHttpClientAndApi() throws IOException {
        boolean verOld = Tools.setJdkHostnameVerification(true);
        KeyStoreDescriptor desc = KeyStoreDescriptor.create("keystore", "tomcat", true, false);
        Assert.assertNotNull(desc);
        Tools.setJdkHostnameVerification(desc);
        HttpClient.Builder builder = Tools.createHttpClient(desc);
        Assert.assertNotNull(builder);
        HttpClient client = builder.build();
        Assert.assertNotNull(client);
        Tools.setJdkHostnameVerification(verOld);

        ComponentSetup setup = new ComponentSetup() {
            
            @Override
            public void notifyStateChange(State state) {
            }
            
            @Override
            public State getState() {
                return State.STOPPED;
            }
            
            @Override
            public KeyStoreDescriptor getKeyStore() {
                return desc;
            }
            
            @Override
            public Endpoint getEndpoint() {
                return new Endpoint(Schema.HTTP, 1234, "api");
            }
            
            @Override
            public AuthenticationDescriptor getAuthentication() {
                return null;
            }
        };
        
        Api api = Tools.createApi(setup, null, new Api(), 
            (b, c, i) -> { },      // configure, here nothing
            (u, c) -> c.setUri(u), // configure API with URI
            (u, c) -> new Api(c),  // if the API needs to be wrapped
            Api.class);
        
        Assert.assertNotNull(api);
        Assert.assertEquals(setup.getEndpoint().toServerUri(), api.uri);
    }

}
