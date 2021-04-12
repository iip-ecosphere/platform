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

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Tests {@link Version}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class VersionTest {

    /**
     * Tests the creation of a version causing an illegal argument exception.
     */
    @Test
    public void versionFormatExc() {
        try {
            @SuppressWarnings("unused")
            Version ver = new Version("a");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("a" + "is not valid", e.getMessage());
        }
    }
    
    /**
     * Tests the functions of valid versions.
     */
    @Test
    public void versionTests() {
        new Version((String) null);
        new Version();
        new Version((int[]) null);
        Version v0 = new Version(0);
        Version v101 = new Version("1.0.1");
        Version v1011 = new Version("1.0.1.1");
        Version v102 = new Version(1, 0, 2);
        Version a0 = new Version(0);
        
        Assert.assertEquals(0, v0.compareTo(v0));
        Assert.assertEquals(-1, v0.compareTo(v101));
        Assert.assertEquals(1, v102.compareTo(v101));
        
        Assert.assertEquals(0, Version.compare(v0, v0));
        Assert.assertEquals(-1, Version.compare(v0, v101));
        Assert.assertEquals(1, Version.compare(v102, v101));
        Assert.assertEquals(-1, Version.compare(v101, v1011));
        Assert.assertEquals(1, Version.compare(v1011, v101));
        Assert.assertEquals(0, Version.compare(null, null));
        Assert.assertEquals(-1, Version.compare(null, v0));
        Assert.assertEquals(1, Version.compare(v0, null));
        
        Assert.assertNotEquals(v0.hashCode(), v102.hashCode());
        Assert.assertNotEquals(v0, v102);
        Assert.assertEquals(v0, v0);
        Assert.assertEquals(v0, a0);

        Assert.assertFalse(Version.equals(v0, v102));
        Assert.assertFalse(Version.equals(v0, null));
        Assert.assertFalse(Version.equals(null, v0));
        Assert.assertTrue(Version.equals(null, null));
        Assert.assertTrue(Version.equals(v0, v0));
        Assert.assertTrue(Version.equals(v0, a0));

        Assert.assertFalse(Version.isVersion(null));
        Assert.assertFalse(Version.isVersion("0.-1"));
        Assert.assertFalse(Version.isVersion("a"));
        Assert.assertTrue(Version.isVersion("0"));
        Assert.assertTrue(Version.isVersion("0.1"));

        Assert.assertEquals("0", v0.toString());
        Assert.assertEquals("1.0.1", v101.toString());
        Assert.assertEquals("1.0.2", v102.toString());
    }

}
