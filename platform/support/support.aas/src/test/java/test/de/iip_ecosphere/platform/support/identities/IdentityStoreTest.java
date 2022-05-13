/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.identities;

import org.junit.Test;

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.identities.YamlIdentityStore;

import org.junit.Assert;

/**
 * Tests the identity store.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentityStoreTest {
    
    /**
     * Tests the identity store.
     */
    @Test
    public void testIdentityStore() {
        IdentityStore store = IdentityStore.getInstance();
        Assert.assertNotNull(store);
        
        if (!(store instanceof YamlIdentityStore)) {
            store = new YamlIdentityStore();
        }
        Assert.assertNull(store.getToken("xxx")); // no anonymous fallback
        Assert.assertEquals(TokenType.ANONYMOUS, store.getToken("xxx", true).getType());
        Assert.assertEquals(TokenType.ANONYMOUS, store.getToken("aas", false).getType());
        Assert.assertEquals(TokenType.X509, store.getToken("secureStore", false).getType());
        Assert.assertEquals(TokenType.USERNAME, store.getToken("axc3151-2", false).getType());
        Assert.assertEquals(TokenType.ISSUED, store.getToken("UI", false).getType());
        Assert.assertEquals(TokenType.USERNAME, store.getToken("myEdge", false, "plcEdges", "axc3151-2").getType());
    }

}
