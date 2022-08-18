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

import org.junit.Test;

import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult.Naming;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolverDescriptor;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult.DefaultNaming;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

/**
 * Tests the basic {@link SemanticIdResolver}. A test JLS file is in test resources ({@code META-INF/services})
 * 
 * @author Holger Eichelberger, SSE
 */
public class SemanticIdResolverTest {

    /**
     * Implements a test resolver.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static final class MyResolver extends SemanticIdResolver {

        @Override
        public SemanticIdResolutionResult resolveSemanticId(String semanticId) {
            DefaultSemanticIdResolutionResult result = null;
            if ("myId:ab1234".equals(semanticId)) {
                result = new DefaultSemanticIdResolutionResult();
                result.setSemanticId(semanticId); // we ignore the other stuff, shall be taken from map or so
                result.setKind("??");
                result.setPublisher("IIP");
                result.setRevision("1");
                result.setVersion("1");
                Map<String, Object> naming = new HashMap<String, Object>();
                DefaultNaming n = new DefaultNaming();
                n.setName("whatever");
                n.setStructuredName("whatever");
                n.setDescription("whatever");
                naming.put("de", n);
                result.setNaming(naming);
            }
            return result;
        }

        @Override
        public boolean isResponsible(String semanticId) {
            return semanticId.startsWith("myId:");
        }
        
    }
    
    /**
     * Implements a test resolver descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static final class MyResolverDescriptor implements SemanticIdResolverDescriptor {

        @Override
        public SemanticIdResolver createResolver() {
            return new MyResolver();
        }
        
    }
 
    /**
     * We just need an unknown type.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static final class MyResolver2 extends SemanticIdResolver {

        @Override
        public SemanticIdResolutionResult resolveSemanticId(String semanticId) {
            return null;
        }

        @Override
        public boolean isResponsible(String semanticId) {
            return false;
        }
        
    }
    
    /**
     * Tests the resolver.
     */
    @Test
    public void testResolver() {
        // shall be there via JSL
        Assert.assertTrue(SemanticIdResolver.hasResolver(MyResolver.class));
        // no JSL descriptor, shall not be there
        Assert.assertFalse(SemanticIdResolver.hasResolver(MyResolver2.class));
        
        // responsible, not found
        SemanticIdResolutionResult res = SemanticIdResolver.resolve("myId:abcs");
        Assert.assertNull(res); 

        // not responsible
        SemanticIdResolver.resolve("abcs");
        Assert.assertNull(res); 

        // responsible, found
        res = SemanticIdResolver.resolve("myId:ab1234");
        Assert.assertNotNull(res);
        Assert.assertNotNull(res.getSemanticId());
        Assert.assertNotNull(res.getPublisher());
        Assert.assertNotNull(res.getKind());
        Assert.assertNotNull(res.getVersion());
        Assert.assertNotNull(res.getNaming());
        Naming n = res.getNaming().get("de");
        Assert.assertNotNull(n);
        Assert.assertNotNull(n.getName());
        Assert.assertNotNull(n.getDescription());
        Assert.assertNotNull(n.getStructuredName());
    }

}
