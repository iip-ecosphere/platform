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

package test.de.iip_ecosphere.platform.services.environment;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.IipStringStyle;
import org.junit.Assert;

/**
 * Tests {@link IipStringStyle}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IipStringStyleTest {

    /**
     * Just som data to be emitted.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestData {
        @SuppressWarnings("unused")
        private int iVal;
        private String sVal;
    }
    
    /**
     * Tests {@link IipStringStyle}.
     */
    @Test
    public void testIipStringStyle() {
        TestData t = new TestData();
        t.iVal = 10;
        t.sVal = "abc";
        String res = ReflectionToStringBuilder.toString(t, IipStringStyle.SHORT_STRING_STYLE);
        Assert.assertTrue(res.length() > t.sVal.length()); // chosen so that prefix fits in
       
        t.sVal = "aaaabbbbaaaabbbbaaaabbbbaaaabbbbkkskghwnajvkjejbajkbe5u ajdgkjekjbngkjnak";
        res = ReflectionToStringBuilder.toString(t, IipStringStyle.SHORT_STRING_STYLE);
        Assert.assertTrue(res.length() < t.sVal.length()); // chosen so that prefix fits in
    }

}
