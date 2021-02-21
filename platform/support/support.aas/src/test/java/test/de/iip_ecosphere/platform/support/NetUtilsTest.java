/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
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

import de.iip_ecosphere.platform.support.NetUtils;

/**
 * Tests {@link NetUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetUtilsTest {

    /**
     * Tests {@link NetUtils#getEphemeralPort()}.
     */
    @Test
    public void testEphemeralPort() {
        // if there is no free port, it probably makes no sense to run the tests at all
        Assert.assertTrue(NetUtils.getEphemeralPort() > 0);
    }
    
    /**
     * Tests {@link NetUtils#getOwnIP()}.
     */
    @Test
    public void testOwnIP() {
        Assert.assertTrue(NetUtils.getOwnIP().length() > 0);
    }
    
}
