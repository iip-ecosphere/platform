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

import de.iip_ecosphere.platform.support.ClassLoaderUtils;
import org.junit.Assert;

/**
 * Tests {@link ClassLoaderUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassLoaderUtilsTest {
    
    /**
     * Tests {@link ClassLoaderUtils#hierarchyToString(ClassLoader)}.
     */
    @Test
    public void testHierarchyToString() {
        ClassLoader loader = getClass().getClassLoader();
        Assert.assertTrue(ClassLoaderUtils.hierarchyToString(loader).indexOf(loader.getClass().getSimpleName()) >= 0);
    }

}
