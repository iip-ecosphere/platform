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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.IdentityTokenBuilder;

/**
 * Testing the creation of identity tokens.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentityTokenTest {

    /**
     * Tests identity token tests.
     */
    @Test
    public void testTokenCreation() {
        byte[] bytes = "aaa".getBytes();
        IdentityToken token;
        token = IdentityTokenBuilder.newBuilder("a", "b", bytes).build();
        Assert.assertNotNull(token);
        token = IdentityTokenBuilder.newBuilder("i", "j", bytes).setIssuedToken(bytes, "abc").build();
        Assert.assertNotNull(token);
        token = IdentityTokenBuilder.newBuilder("u", "k", bytes).setUsernameToken("me", bytes, "abc").build();
        Assert.assertNotNull(token);
        token = IdentityTokenBuilder.newBuilder("u", "k", bytes).setX509Token(bytes).build();
        
        token = IdentityTokenBuilder.newBuilder().setUsernameToken("me", bytes, "abc").build();
        Assert.assertNotNull(token);
    }

}
