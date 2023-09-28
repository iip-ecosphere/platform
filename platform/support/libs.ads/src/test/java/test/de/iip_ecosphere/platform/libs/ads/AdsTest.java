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

package test.de.iip_ecosphere.platform.libs.ads;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.sun.jna.Platform;

import de.iip_ecosphere.platform.libs.ads.Ads;
import de.iip_ecosphere.platform.libs.ads.TcAds;

/**
 * Tests {@link Ads} and {@link TcAds}.
 * 
 * @author Alexander Weber, SSE
 * @author Holger Eichelberger, SSE
 */
public class AdsTest {
    
    /**
     * Tests loading the library.
     */
    @Test
    public void testAds() {
        File f = new File("./src/main/resources");
        org.junit.Assume.assumeTrue(Platform.isLinux() 
            || Platform.isWindows() && (new File(f, "win32-x86-32").exists() || new File(f, "win32-x86-64").exists()));
        
        TcAds ads = Ads.getInstance();
        Assert.assertNotNull("No ADS library found for loading", ads);
        if (Platform.isWindows()) {
            long ver = ads.AdsGetDllVersion();
            Assert.assertTrue(ver > 0);
        }
    }
    
}
