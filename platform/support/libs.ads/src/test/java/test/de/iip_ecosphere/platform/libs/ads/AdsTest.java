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
     * Template test.
     */
    @Test
    public void testAds() {
        org.junit.Assume.assumeTrue(Platform.isLinux());
        
        TcAds ads = Ads.getInstance();
        Assert.assertNotNull("No ADS library found for loading", ads);
        long ver = ads.AdsGetDllVersion();
        Assert.assertTrue(ver > 0);
    }
    
}
