/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
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

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.StringUtils;

import java.util.StringTokenizer;

import org.junit.Assert;

/**
 * Tests {@link StringUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringUtilsTest {

    /**
     * Tests {@link StringUtils#isBlank(CharSequence)} and 
     * {@link StringUtils#isEmpty(CharSequence)}.
     */
    @Test
    public void testIsEmptyBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank("   "));
        Assert.assertFalse(StringUtils.isBlank("a"));

        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty("   "));
        Assert.assertFalse(StringUtils.isEmpty("a"));
    }

    /**
     * Just som data to be emitted.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestData {
        @SuppressWarnings("unused")
        private int iVal;
        @SuppressWarnings("unused")
        private String sVal;
    }

    /**
     * Tests {@link StringUtils#toString(Object)} and 
     * {@link StringUtils#toStringShortStyle(Object)}.
     */
    @Test
    public void testToString() {
        TestData t = new TestData();
        t.iVal = 10;
        t.sVal = "abc";
        StringUtils.toStringShortStyle(t);
    }

    /**
     * Tests {@link StringUtils#toTokenArray(StringTokenizer)} and {@link StringUtils#toTokenList(StringTokenizer)}.
     */
    @Test
    public void testTokens() {
        Assert.assertEquals(CollectionUtils.toList("a", "b"), 
            StringUtils.toTokenList(new StringTokenizer("a, b", ", ")));
        Assert.assertArrayEquals(new String[] {"a", "b"}, 
            StringUtils.toTokenArray(new StringTokenizer("a, b", ", ")));
        Assert.assertNull(StringUtils.toArray(null));
    }
    
    /**
     * Tests remaining functions, on this level without functionality.
     */
    @Test
    public void testRest() {
        StringUtils.unescapeJava(StringUtils.escapeJava("\n"));
        StringUtils.unescapeJson(StringUtils.escapeJson("\t"));
        StringUtils.removeEnd("ThisEnd", "End");
        StringUtils.removeStart("ThisEnd", "This");
        StringUtils.defaultIfBlank(null, "a");
        StringUtils.defaultIfBlank("", "a");
        StringUtils.defaultIfBlank("   ", "a");

        StringUtils.defaultIfEmpty(null, "a");
        StringUtils.defaultIfEmpty("", "a");
        StringUtils.defaultIfEmpty("   ", "a");

        StringUtils.isNotBlank("   ");
        StringUtils.replaceOnce("", "", "");
        StringUtils.toString(new Object());
    }

}
