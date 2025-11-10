/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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

import de.iip_ecosphere.platform.support.JavaUtils;
import de.iip_ecosphere.platform.support.OsUtils;

/**
 * Tests {@link JavaUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JavaUtilsTest {

    /**
     * Tests {@link JavaUtils#getJavaPath()}, {@link JavaUtils#getJavaBinaryPath()} and 
     * {@link JavaUtils#getJavaBinaryPath(String)}.
     */
    @Test
    public void testJavaPaths() {
        Assert.assertNotNull(JavaUtils.getJavaPath());
        Assert.assertTrue(JavaUtils.getJavaPath().length() > 0);
        if (OsUtils.getJavaSpecificationVersion().startsWith("1.")) {
            Assert.assertNull(JavaUtils.getJavaBinaryPath());
        } else {
            Assert.assertNotNull(JavaUtils.getJavaBinaryPath());
            Assert.assertTrue(JavaUtils.getJavaBinaryPath().length() > 0);
        }
        Assert.assertNotNull(JavaUtils.getJavaBinaryPath("java"));
        Assert.assertTrue(JavaUtils.getJavaBinaryPath("java").length() > 0);
    }

}
