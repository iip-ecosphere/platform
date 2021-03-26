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

package test.de.iip_ecosphere.platform.services.spring;

import de.iip_ecosphere.platform.services.spring.Utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link Utils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class UtilsTest {
    
    /**
     * Tests {@link Utils#formatToMeBi(long)}.
     */
    @Test
    public void testFormatToMeBi() {
        Assert.assertEquals("0", Utils.formatToMeBi(0));
        Assert.assertEquals("1", Utils.formatToMeBi(1));
        Assert.assertEquals("1025", Utils.formatToMeBi(1025));
        Assert.assertEquals("1m", Utils.formatToMeBi(1024 * 1024));
        Assert.assertEquals("1m", Utils.formatToMeBi(1024 * 1024 + 1));
        Assert.assertEquals("4m", Utils.formatToMeBi(1024 * 1024 * 4));
        Assert.assertEquals("1g", Utils.formatToMeBi(1024 * 1024 * 1024));
        Assert.assertEquals("1g", Utils.formatToMeBi(1024 * 1024 * 1024 + 1));
        // overflow
    }
    
    /**
     * Tests {@link Utils#formatToMeBi(long)}.
     */
    @Test
    public void testFormatToMeBiBase() {
        Assert.assertEquals("0", Utils.formatToMeBi(0));
        Assert.assertEquals("1", Utils.formatToMeBi(1));
        Assert.assertEquals("1025", Utils.formatToMeBi(1025));
        Assert.assertEquals("1m", Utils.formatToMeBi(1024, 1));
        Assert.assertEquals("1m", Utils.formatToMeBi(1, 2));
        Assert.assertEquals("1m", Utils.formatToMeBi(1024 + 1, 1));
        Assert.assertEquals("4m", Utils.formatToMeBi(1024 * 4, 1));
        Assert.assertEquals("4m", Utils.formatToMeBi(4, 2));
        Assert.assertEquals("1g", Utils.formatToMeBi(1024 * 1024, 1));
        Assert.assertEquals("1g", Utils.formatToMeBi(1024 * 1024 + 1, 1));
        Assert.assertEquals("15g", Utils.formatToMeBi(1024 * 1024 * 15, 1));
        Assert.assertEquals("1024g", Utils.formatToMeBi(1024 * 1024 * 1024, 1));
    }
    
    /**
     * Tests {@link Utils#addPropertyIfPositiveToInt(Map, String, Number, String)}.
     */
    @Test
    public void testAddPropertiesInt() {
        Map<String, String> prop = new HashMap<String, String>();
        Utils.addPropertyIfPositiveToInt(prop, "a", -1, null);
        Assert.assertFalse(prop.containsKey("a"));
        Utils.addPropertyIfPositiveToInt(prop, "a", -1, "0");
        Assert.assertEquals("0", prop.get("a"));
        Utils.addPropertyIfPositiveToInt(prop, "b", 5, "0");
        Assert.assertEquals("5", prop.get("b"));
    }

    /**
     * Tests {@link Utils#addPropertyIfPositiveToMeBi(Map, String, Number, String)}.
     */
    @Test
    public void testAddPropertiesMeBi() {
        Map<String, String> prop = new HashMap<String, String>();

        Utils.addPropertyIfPositiveToMeBi(prop, "c", -1, null);
        Assert.assertFalse(prop.containsKey("c"));
        Utils.addPropertyIfPositiveToMeBi(prop, "c", -1, "1m");
        Assert.assertEquals("1m", prop.get("c"));
        Utils.addPropertyIfPositiveToMeBi(prop, "d", 1, null);
        Assert.assertEquals("1m", prop.get("d"));
        Utils.addPropertyIfPositiveToMeBi(prop, "e", 1024, null);
        Assert.assertEquals("1g", prop.get("e"));
    }

}
