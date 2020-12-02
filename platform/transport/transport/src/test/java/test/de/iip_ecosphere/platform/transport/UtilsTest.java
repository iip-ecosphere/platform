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

package test.de.iip_ecosphere.platform.transport;

import de.iip_ecosphere.platform.transport.Utils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link Utils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class UtilsTest {

    /**
     * Tests the sleep method.
     */
    @Test
    public void testSleep() {
        long before = System.currentTimeMillis();
        Utils.sleep(100);
        long after = System.currentTimeMillis();
        long diff = after - before;
        Assert.assertTrue(80 < diff && diff < 200);
    }

}
