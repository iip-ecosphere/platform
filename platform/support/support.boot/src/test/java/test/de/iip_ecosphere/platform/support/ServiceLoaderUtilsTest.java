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

package test.de.iip_ecosphere.platform.support;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.support.json.JsonProviderDescriptor;

/**
 * Tests {@link ServiceLoaderUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceLoaderUtilsTest {

    /**
     * Another descriptor interface without implementations shall lead to service configuration error.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ServiceLoaderUtilsFailDescriptor {
    }
    
    /**
     * An excluded descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    @ExcludeFirst
    public static class ExcludedDescriptor implements ServiceLoaderUtilsDescriptor {
    }

    /**
     * An included descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class IncludedDescriptor implements ServiceLoaderUtilsDescriptor {
    }

    /**
     * Tests the functions in {@link ServiceLoaderUtils}. 
     */
    @Test
    public void testServiceLoaderUtils() {
        Assert.assertFalse(ServiceLoaderUtils.hasExcludeFirst(new Object()));
        Assert.assertTrue(ServiceLoaderUtils.hasExcludeFirst(new ExcludedDescriptor()));
        
        Assert.assertEquals(2, CollectionUtils.toList(
            ServiceLoaderUtils.load(ServiceLoaderUtilsDescriptor.class).iterator()).size());
        
        Optional<ServiceLoaderUtilsDescriptor> first = ServiceLoaderUtils.filterExcluded(
            ServiceLoaderUtilsDescriptor.class);
        Assert.assertTrue(first.isPresent());
        Assert.assertTrue(first.get() instanceof IncludedDescriptor);
    }

    /**
     * Tests {@link ServiceLoaderUtils#findFirst(Class)}.
     */
    @Test
    public void testFindFirst() {
        Optional<Object> objOptional = ServiceLoaderUtils.findFirst(Object.class);
        Assert.assertFalse(objOptional.isPresent());

        Optional<JsonProviderDescriptor> jsonOptional = ServiceLoaderUtils.findFirst(JsonProviderDescriptor.class);
        Assert.assertTrue(jsonOptional.isPresent());

        Optional<ServiceLoaderUtilsDescriptor> testOptional 
            = ServiceLoaderUtils.findFirst(ServiceLoaderUtilsDescriptor.class);
        Assert.assertTrue(testOptional.isPresent());
        Assert.assertTrue(testOptional.get() instanceof ExcludedDescriptor); // mentioned first in JSL file, ...
    }
    
}
