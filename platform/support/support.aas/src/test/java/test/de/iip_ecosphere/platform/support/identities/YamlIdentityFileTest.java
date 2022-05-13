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

import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.identities.YamlIdentityFile;
import de.iip_ecosphere.platform.support.identities.YamlIdentityFile.IdentityInformation;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic tests for {@link YamlIdentityFile}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlIdentityFileTest {
    
    /**
     * Basic tests for {@link YamlIdentityFile}.
     */
    @Test
    public void testIdentityFile() throws IOException {
        YamlIdentityFile file = YamlIdentityFile.load(null);
        Assert.assertNotNull(file);
        
        FileInputStream fis = new FileInputStream("src/test/resources/identityStore.yml");
        file = YamlIdentityFile.load(fis);
        Assert.assertNotNull(file);
        
        IdentityInformation info = file.getData("axc3151-1");
        Assert.assertNull(info);
        
        info = file.getData("axc3151-2");
        Assert.assertNotNull(info);
        Assert.assertEquals(TokenType.USERNAME, info.getType());
        Assert.assertEquals("abba", info.getUserName());
        Assert.assertEquals("babbab", info.getTokenData());
        Assert.assertNotNull(info.getTokenDataAsBytes());
        Assert.assertEquals("UTF-8", info.getTokenEncryptionAlgorithm());
        
        info = file.getData("aas");
        Assert.assertEquals(TokenType.ANONYMOUS, info.getType());
        
        info = file.getData("secureStore");
        Assert.assertEquals(TokenType.X509, info.getType());
        Assert.assertEquals("polId", info.getTokenPolicyId());
        Assert.assertEquals("mySigAlg", info.getSignatureAlgorithm());
        Assert.assertEquals("sig-1", info.getSignature());
        Assert.assertNotNull(info.getSignatureAsBytes());
        Assert.assertEquals("token-1", info.getTokenData());
        
        info = file.getData("UI");
        Assert.assertEquals(TokenType.ISSUED, info.getType());
        Assert.assertEquals("polId-2", info.getTokenPolicyId());
        Assert.assertEquals("yourSigAlg", info.getSignatureAlgorithm());
        Assert.assertEquals("sig-2", info.getSignature());
        Assert.assertNotNull(info.getSignatureAsBytes());
        Assert.assertEquals("token", info.getTokenData());
        Assert.assertEquals("bla", info.getTokenEncryptionAlgorithm());
    }

}
