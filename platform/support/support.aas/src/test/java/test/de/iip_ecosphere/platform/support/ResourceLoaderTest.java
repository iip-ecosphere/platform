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

package test.de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;

/**
 * Tests {@link ResourceLoader}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ResourceLoaderTest {

    private static int myResolverCalled = 0;
    private static int myResolver2Called = 0;
    
    /**
     * A test resolver.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyResolver implements ResourceResolver {

        @Override
        public InputStream resolve(ClassLoader loader, String resource) {
            myResolverCalled++;
            return null;
        }
        
    }

    /**
     * Another test resolver.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyResolver2 implements ResourceResolver {

        @Override
        public InputStream resolve(ClassLoader loader, String resource) {
            myResolver2Called++;
            return null;
        }
        
    }

    /**
     * Tests resource resolution.
     * 
     * @throws IOException shall not occur in successful tests
     */
    @Test
    public void testResolver() throws IOException {
        MyResolver2 res = new MyResolver2();
        ResourceLoader.registerResourceResolver(res);
        InputStream is = ResourceLoader.getResourceAsStream("Logo.jpg");
        Assert.assertNotNull(is);
        is = ResourceLoader.getResourceAsStream("/Logo.jpg");
        Assert.assertNotNull(is);
        is.close();
        
        // via classloader
        Assert.assertTrue(myResolverCalled == 0);
        Assert.assertTrue(myResolver2Called == 0);
        
        // does not exist
        is = ResourceLoader.getResourceAsStream("BadBadFile.xyz");
        Assert.assertNull(is);
        // also the other resolvers are taken into account
        Assert.assertTrue(myResolverCalled > 0);
        Assert.assertTrue(myResolver2Called > 0);
        
        // if we need a resolver for somewhere else
        is = ResourceLoader.getAllRegisteredResolver().resolve("Logo.jpg");
        Assert.assertNotNull(is);
        is.close();

        // once again, resolver known, must work anyway
        is = ResourceLoader.getAllRegisteredResolver(res).resolve("Logo.jpg");
        Assert.assertNotNull(is);
        is.close();
        
        ResourceLoader.unregisterResourceResolver(res);
        
        // Here it works per class loader. This may fail in generated parts.
        is = ResourceLoader.MAVEN_RESOLVER.resolve("Logo.jpg");
        Assert.assertNotNull(is);
        is.close();
    }
    
    /**
     * Tests {@link ResourceLoader#prependSlash(String)}.
     */
    @Test
    public void testPrependSlash() {
        Assert.assertEquals("/a", ResourceLoader.prependSlash("a"));
        Assert.assertEquals("/a", ResourceLoader.prependSlash("/a"));
        Assert.assertEquals("/", ResourceLoader.prependSlash(""));
    }

}
