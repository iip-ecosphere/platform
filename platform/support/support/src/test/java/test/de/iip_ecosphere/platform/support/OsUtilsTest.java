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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.OsUtils;

/**
 * Tests {@link OsUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OsUtilsTest {
    
    /**
     * Tests {@link OsUtils} methods.
     */
    @Test
    public void testValues() {
        String osName = OsUtils.getOsName();
        Assert.assertNotNull(osName);
        Assert.assertTrue(osName.length() > 0);
        System.out.println("OS: " + osName);

        String osArch = OsUtils.getOsArch();
        Assert.assertNotNull(osArch);
        Assert.assertTrue(osArch.length() > 0);
        System.out.println("Arch: " + osArch);

        int cpuNum = OsUtils.getNumCpuCores();
        Assert.assertTrue(cpuNum > 0);
        System.out.println("#CPU: " + cpuNum);
    }

    /**
     * Tests {@link SysUtils#getPropertyOrEnv(String, String)}, {@link SysUtils#getPropertyOrEnv(String)} and 
     * implicitly {@link {@link SysUtils#getEnv(String)}.
     */
    @Test
    public void testSystemPropertyOrEnv() {
        Assert.assertNull(OsUtils.getPropertyOrEnv("iip.nonsense"));
        Assert.assertEquals("abba", OsUtils.getPropertyOrEnv("iip.nonsense", "abba"));
        // SysUtils.getEnv implicit; but just for non-whitebox :P
        Assert.assertNull(OsUtils.getEnv("iip.nonsense1"));
    }
}
