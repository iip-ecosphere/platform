/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Test;

import org.junit.Assert;

/**
 * Tests {@link AbstractAasExample}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractAasExampleTest {
    
    /**
     * Tests {@link AbstractAasExample#toTestString(String, String)}.
     */
    @Test
    public void testToTestString() {
        Assert.assertEquals("a", AbstractAasExample.toTestString(null, "a"));
        Assert.assertEquals("a", AbstractAasExample.toTestString("", "a"));
    }

    /**
     * Tests {@link AbstractAasExample#toTestMLString(String, String, String)}.
     */
    @Test
    public void testToTestMLString() {
        Assert.assertEquals("a@de", AbstractAasExample.toTestMLString(null, "en", "a@de"));
        Assert.assertEquals("a@en", AbstractAasExample.toTestMLString("", "de", "a@en"));
        Assert.assertEquals("a@en", AbstractAasExample.toTestMLString("", "@en", "a"));
        Assert.assertEquals("testing@en", AbstractAasExample.toTestMLString("@de: test @en: testing", "en", "a"));
        Assert.assertEquals("testing@en", AbstractAasExample.toTestMLString("@de test @en testing", "en", "a"));
        Assert.assertEquals("testing@en", AbstractAasExample.toTestMLString("test@de testing@en", "en", "a"));
    }

    /**
     * Tests {@link AbstractAasExample#toTestInt(String, int)}.
     */
    @Test
    public void testToTestInt() {
        Assert.assertEquals(12, AbstractAasExample.toTestInt(null, 12));
        Assert.assertEquals(13, AbstractAasExample.toTestInt("", 13));
        Assert.assertEquals(13, AbstractAasExample.toTestInt("a", 13));
        Assert.assertEquals(155, AbstractAasExample.toTestInt("155", 155));
        Assert.assertEquals(14, AbstractAasExample.toTestInt("14 [kg]", 14));
        Assert.assertEquals(14, AbstractAasExample.toTestInt("[kg] 14", 14));
        Assert.assertEquals(14, AbstractAasExample.toTestInt("14 kg", 14));
        Assert.assertEquals(14, AbstractAasExample.toTestInt("14.65 kg", 14));
    }

    /**
     * Tests {@link AbstractAasExample#toTestDouble(String, double)}.
     */
    @Test
    public void testToTestDouble() {
        Assert.assertEquals(12.1, AbstractAasExample.toTestDouble(null, 12.1), 0.01);
        Assert.assertEquals(13.2, AbstractAasExample.toTestDouble("", 13.2), 0.01);
        Assert.assertEquals(13.3, AbstractAasExample.toTestDouble("a", 13.3), 0.01);
        Assert.assertEquals(13.4, AbstractAasExample.toTestDouble("13.4", 13.3), 0.01);
        Assert.assertEquals(13, AbstractAasExample.toTestDouble("13", 13.3), 0.01);
        Assert.assertEquals(14, AbstractAasExample.toTestDouble("14 [kg]", 14), 0.01);
        Assert.assertEquals(14, AbstractAasExample.toTestDouble("[kg] 14", 14), 0.01);
        Assert.assertEquals(14, AbstractAasExample.toTestDouble("14 kg", 14), 0.01);
    }

    /**
     * Tests {@link AbstractAasExample#toTestBoolean(String, boolean)}.
     */
    @Test
    public void testToTestBoolean() {
        Assert.assertEquals(true, AbstractAasExample.toTestBoolean(null, true));
        Assert.assertEquals(true, AbstractAasExample.toTestBoolean("", true));
        Assert.assertEquals(true, AbstractAasExample.toTestBoolean("a", true));
        Assert.assertEquals(true, AbstractAasExample.toTestBoolean("true", false));
        Assert.assertEquals(false, AbstractAasExample.toTestBoolean("false", true));
        Assert.assertEquals(true, AbstractAasExample.toTestBoolean("bla true bli", false));
    }

}
