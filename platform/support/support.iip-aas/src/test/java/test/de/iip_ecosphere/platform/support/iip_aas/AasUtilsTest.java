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

package test.de.iip_ecosphere.platform.support.iip_aas;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.AasUtils;

/**
 * Tests {@link AasUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasUtilsTest {
    
    /**
     * Tests {@link AasUtils}.
     */
    @Test
    public void testAasUtils() {
        Object[] noArgs = new Object[0];
        Object[] args = new Object[2];
        args[0] = "abba";
        args[1] = 5;
        
        Assert.assertEquals("", AasUtils.readString(noArgs, 0, ""));
        Assert.assertEquals("abba", AasUtils.readString(args, 0, null));

        Assert.assertEquals(-1, AasUtils.readInt(noArgs, 1, -1));
        Assert.assertEquals(5, AasUtils.readInt(args, 1, 0));
    }

}
