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

package test.de.iip_ecosphere.platform.support;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.plugins.TestProviderDescriptor;

/**
 * Tests {@link TestProviderDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestProviderDescriptorTest {
    
    /**
     * A provider for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    @ExcludeFirst
    public static class Desc implements TestProviderDescriptor {

        @Override
        public Class<?>[] getTests(int index) {
            if (0 == index) {
                return new Class<?>[]{Object.class};
            } else {
                return null;
            }
        }
        
    }
    
    /**
     * Tests the provider.
     */
    @Test
    public void testProvider() {
        Assert.assertNotNull(TestProviderDescriptor.getTests(0, getClass().getClassLoader()));
        Assert.assertNull(TestProviderDescriptor.getTests(1, getClass().getClassLoader()));
    }

}
