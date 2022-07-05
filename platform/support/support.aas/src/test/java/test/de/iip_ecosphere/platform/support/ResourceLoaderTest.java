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
     */
    @Test
    public void testResolver() {
        MyResolver2 res = new MyResolver2();
        ResourceLoader.registerResourceResolver(res);
        InputStream is = ResourceLoader.getResourceAsStream("Logo.jpg");
        Assert.assertNotNull(is);
        is = ResourceLoader.getResourceAsStream("/Logo.jpg");
        Assert.assertNotNull(is);
        // via classloader
        Assert.assertTrue(myResolverCalled == 0);
        Assert.assertTrue(myResolver2Called == 0);
        
        // does not exist
        is = ResourceLoader.getResourceAsStream("BadBadFile.xyz");
        Assert.assertNull(is);
        // also the other resolvers are taken into account
        Assert.assertTrue(myResolverCalled > 0);
        Assert.assertTrue(myResolver2Called > 0);
        ResourceLoader.unregisterResourceResolver(res);
    }

}
