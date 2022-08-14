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

package test.de.iip_ecosphere.platform.support.iip_aas;

import de.iip_ecosphere.platform.support.iip_aas.IipVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link IipVersion}.
 * 
 * @author Holger Eichelberger, SSE
 *
 */
public class IipVersionTest {

    /**
     * Simple test for {@link IipVersion}.
     */
    @Test
    public void testIipVersion() {
        IipVersion ver = IipVersion.getInstance();
        Assert.assertNotNull(ver);
        
        // we may rely on iip-version.properties, but this may change in CI and then the value
        // we could read out the properties file a second time, ...
        Assert.assertTrue(ver.getVersion().length() > 0);
        Assert.assertTrue(ver.getBuildId().length() > 0);
        Assert.assertTrue(ver.getVersionInfo().length() > 0);
        ver.isRelease(); // true or false
    }

}
