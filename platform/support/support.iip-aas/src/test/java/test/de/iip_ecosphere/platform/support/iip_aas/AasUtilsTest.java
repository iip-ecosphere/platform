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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.AasUtils;

/**
 * Tests {@link AasUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasUtilsTest {
    
    /**
     * Tests {@link AasUtils#readString(Object[], int, String)} and variants.
     */
    @Test
    public void testString() {
        Object[] noArgs = new Object[0];
        Object[] args = new Object[2];
        args[0] = "abba";
        args[1] = 5;
        
        Assert.assertEquals("", AasUtils.readString(noArgs, 0, ""));
        Assert.assertEquals("abba", AasUtils.readString(args, 0, null));

        // default ""
        Assert.assertEquals("", AasUtils.readString(noArgs, 0));
        // default "", index 0
        Assert.assertEquals("", AasUtils.readString(noArgs));
    }

    /**
     * Tests {@link AasUtils#readInt(Object[], int, int)}.
     */
    @Test
    public void testInt() {
        Object[] noArgs = new Object[0];
        Object[] args = new Object[2];
        args[0] = "abba";
        args[1] = 5;
        
        Assert.assertEquals(-1, AasUtils.readInt(noArgs, 1, -1));
        Assert.assertEquals(5, AasUtils.readInt(args, 1, 0));
    }

    /**
     * Tests {@link AasUtils#readDouble(Object[], int, int)}.
     */
    @Test
    public void testDouble() {
        Object[] noArgs = new Object[0];
        Object[] args = new Object[2];
        args[0] = "abba";
        args[1] = 5.23;
        
        Assert.assertEquals(-1, AasUtils.readDouble(noArgs, 1, -1), 0.01);
        Assert.assertEquals(5.23, AasUtils.readDouble(args, 1, 0), 0.01);
    }
    
    /**
     * Tests {@link AasUtils#readUri(Object[], int, URI)}.
     */
    @Test
    public void testUri() {
        Object[] noArgs = new Object[0];
        Object[] args = new Object[1];

        // URI is ok
        args[0] = "http://me.here/my/file.txt";
        URI uri = AasUtils.readUri(noArgs, 0, null);
        Assert.assertNull(uri);
        uri = AasUtils.readUri(args, 0, null);
        Assert.assertNotNull(uri);
        Assert.assertEquals(args[0], uri.toString());

        // erroneous URI
        args[0] = "<x>";
        uri = AasUtils.readUri(args, 0, null);
        Assert.assertNull(uri);

        try {
            AasUtils.readUriEx(args, 0, null);
            Assert.fail("no exception");
        } catch (URISyntaxException e) {
            // this is intended
        }
    }
    
    /**
     * Tests {@link AasUtils#fixId(String)}.
     */
    @Test
    public void testFixId() {
        Assert.assertEquals("id", AasUtils.fixId("id"));
    }

    /**
     * Tests {@link AasUtils#resolveImage(String, de.iip_ecosphere.platform.support.iip_aas.AasUtils.ResourceResolver, 
     * boolean, de.iip_ecosphere.platform.support.iip_aas.AasUtils.ResourceHandler)}.
     */
    @Test
    public void resolveImageTest() {
        AasUtils.resolveImage("nix.png", AasUtils.CLASSPATH_RESOLVER, false, 
            (n, r, m) -> { Assert.fail("Shall not be called. Not resolved."); });
        AasUtils.resolveImage("nix.png", null, true, 
            (n, r, m) -> { Assert.assertEquals("", r); });
        String uri = "https://www.iip-ecosphere.de/wp-content/uploads/2020/08/Picture4.jpg";
        AasUtils.resolveImage(uri, AasUtils.CLASSPATH_RESOLVER, true, (n, r, m) -> { 
            Assert.assertEquals("text/x-uri", m);
            Assert.assertEquals(uri, r); 
        });
        AasUtils.resolveImage("IIP-Ecosphere-Platform.png", AasUtils.CLASSPATH_RESOLVER, false, (n, r, m) -> { 
            Assert.assertTrue(r.length() > 0); 
            Assert.assertEquals("image/png", m); 
        });
        AasUtils.resolveImage("IIP-Ecosphere-Platform.png", AasUtils.CLASSPATH_RESOURCE_RESOLVER, false, (n, r, m) -> { 
            Assert.assertTrue(r.length() > 0); 
            Assert.assertEquals("image/png", m); 
        });
    }

}
