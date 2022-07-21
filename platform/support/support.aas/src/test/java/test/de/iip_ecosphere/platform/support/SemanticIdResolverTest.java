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
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolverDescriptor;

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
     * Tests the resolver.
     */
    @Test
    public void testResolver() {
        // responsible, not found
        SemanticIdResolutionResult res = SemanticIdResolver.resolve("myId:abcs");
        Assert.assertNull(res); 

        // not responsible
        SemanticIdResolver.resolve("abcs");
        Assert.assertNull(res); 

        // responsible, found
        res = SemanticIdResolver.resolve("myId:ab1234");
        Assert.assertNotNull(res);
    }

}
