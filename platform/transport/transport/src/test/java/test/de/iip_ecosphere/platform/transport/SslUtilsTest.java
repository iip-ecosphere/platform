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

package test.de.iip_ecosphere.platform.transport;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import de.iip_ecosphere.platform.transport.connectors.SslUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link SslUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SslUtilsTest {
    
    /**
     * Tests the basic keystore access methods.
     */
    @Test
    public void testKeystoreAccess() {
        File keystore = new File("./src/test/resources/keystore.jks");
        String passwd = "a1234567"; // determined on test keystore creation
        try {
            Assert.assertNull(SslUtils.createTrustManagerFactory(null, passwd));
            Assert.assertNull(SslUtils.createTrustManagerFactory(new File("here.jks"), passwd));
            
            TrustManagerFactory tmf = SslUtils.createTrustManagerFactory(keystore, passwd);
            Assert.assertNotNull(tmf);
            
            Assert.assertNull(SslUtils.createTlsContext(new File("here.jks"), passwd));
            Assert.assertNull(SslUtils.createTlsContext(null, passwd));
            SSLContext ctx = SslUtils.createTlsContext(keystore, passwd);
            Assert.assertNotNull(ctx);

            ctx = SslUtils.createTlsContext(keystore, passwd, "qpid");
            Assert.assertNotNull(ctx);
        } catch (IOException e) {
            Assert.fail("No I/O exception expected. " + e.getMessage());
        }
    }
    
    /**
     * Tests the creation of keystore properties.
     */
    public void testKeystoreProperties() {
        Assert.assertNull(SslUtils.createIbmTlsProperties(null, null));
        Assert.assertNull(SslUtils.createIbmTlsProperties(new File("here.jks"), null));
        
        File keystore = new File("./src/test/resources/keystore.jks");
        String passwd = "a1234567"; // determined on test keystore creation
        Properties prop = SslUtils.createIbmTlsProperties(keystore, passwd);
        Assert.assertTrue(prop.containsKey("com.ibm.ssl.protocol"));
        Assert.assertTrue(prop.containsKey("com.ibm.ssl.trustStore"));
        Assert.assertEquals(prop.get("com.ibm.ssl.trustStore"), keystore.getAbsoluteFile());
        Assert.assertTrue(prop.containsKey("com.ibm.ssl.trustStorePassword"));
        Assert.assertEquals(prop.get("com.ibm.ssl.trustStorePassword"), passwd);
        Assert.assertTrue(prop.containsKey("com.ibm.ssl.trustStoreTypeType"));
        Assert.assertEquals(prop.get("com.ibm.ssl.trustStoreTypeType"), SslUtils.getKeystoreType(keystore));
        
        prop = SslUtils.createIbmTlsProperties(keystore, null);
        Assert.assertFalse(prop.containsKey("com.ibm.ssl.trustStorePassword"));
    }

}
