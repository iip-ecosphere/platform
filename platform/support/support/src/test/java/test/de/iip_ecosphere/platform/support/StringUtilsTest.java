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

import de.iip_ecosphere.platform.support.StringUtils;

import org.junit.Assert;

/**
 * Tests {@link StringUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringUtilsTest {
    
    /**
     * Tests {@link StringUtils#escapeJava(String)} and {@link StringUtils#unescapeJava(String)}.
     */
    @Test
    public void testJava() {
        Assert.assertNull(StringUtils.escapeJava(null));
        Assert.assertNull(StringUtils.unescapeJava(null));

        Assert.assertEquals("\n", StringUtils.unescapeJava(StringUtils.escapeJava("\n")));
    }

    /**
     * Tests {@link StringUtils#escapeJson(String)} and {@link StringUtils#unescapeJson(String)}.
     */
    @Test
    public void testJson() {
        Assert.assertNull(StringUtils.escapeJson(null));
        Assert.assertNull(StringUtils.unescapeJson(null));

        Assert.assertEquals("\t", StringUtils.unescapeJava(StringUtils.escapeJava("\t")));
    }
    
    /**
     * Tests {@link StringUtils#defaultIfBlank(CharSequence, CharSequence)} and 
     * {@link StringUtils#defaultIfEmpty(CharSequence, CharSequence)}.
     */
    @Test
    public void testDefaults() {
        Assert.assertEquals("a", StringUtils.defaultIfBlank(null, "a"));
        Assert.assertEquals("a", StringUtils.defaultIfBlank("", "a"));
        Assert.assertEquals("a", StringUtils.defaultIfBlank("   ", "a"));

        Assert.assertEquals("a", StringUtils.defaultIfEmpty(null, "a"));
        Assert.assertEquals("a", StringUtils.defaultIfEmpty("", "a"));
        Assert.assertEquals("   ", StringUtils.defaultIfEmpty("   ", "a"));
    }

    /**
     * Tests {@link StringUtils#isBlank(CharSequence)} and 
     * {@link StringUtils#isEmpty(CharSequence)}.
     */
    @Test
    public void testIsEmptyBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank("   "));

        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty("   "));
    }

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
     * Tests {@link StringUtils#toString(Object)} and 
     * {@link StringUtils#toStringShortStyle(Object)}.
     */
    public void testToString() {
        TestData t = new TestData();
        t.iVal = 10;
        t.sVal = "abc";
        String res = StringUtils.toStringShortStyle(t);
        Assert.assertTrue(res.length() > t.sVal.length()); // chosen so that prefix fits in
        res = StringUtils.toString(t);
        Assert.assertTrue(res.length() > 0);
       
        t.sVal = "aaaabbbbaaaabbbbaaaabbbbaaaabbbbkkskghwnajvkjejbajkbe5u ajdgkjekjbngkjnak";
        res = StringUtils.toStringShortStyle(t);
        Assert.assertTrue(res.length() < t.sVal.length()); // chosen so that prefix fits in
    }
    
    /**
     * Tests {@link StringUtils#removeStart(String, String)} and 
     * {@link StringUtils#removeEnd(String, String)}.
     */
    public void testRemove() {
        Assert.assertEquals("This", StringUtils.removeEnd("ThisEnd", "End"));
        Assert.assertEquals("End", StringUtils.removeStart("ThisEnd", "This"));
    }

}
