/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.xml.Xml;
import test.de.iip_ecosphere.platform.support.xml.TestXml;

/**
 * "Tests" the {@link Xml} interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public class XmlTest {

    /**
     * A test data class.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Data {
        
        private int iValue;
        private String sValue;
        
        /**
         * Returns the string value. 
         * 
         * @return the iValue
         */
        public int getiValue() {
            return iValue;
        }
        
        /**
         * Changes the string value. [XML]
         * 
         * @param iValue the iValue to set
         */
        public void setiValue(int iValue) {
            this.iValue = iValue;
        }
        
        /**
         * Returns the string value. 
         * 
         * @return the sValue
         */
        public String getsValue() {
            return sValue;
        }
        
        /**
         * Changes the string value. [XML]
         * 
         * @param sValue the sValue to set
         */
        public void setsValue(String sValue) {
            this.sValue = sValue;
        } 
        
    }

    /**
     * Tests basic XML functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testXml() throws IOException {
        Xml xml = Xml.createInstance();
        Assert.assertTrue(xml instanceof TestXml);
        Xml.setPrototype(xml);
        
        Xml.createInstance(Object.class);
        Xml.createInstance(Object.class, Integer.class);
        Xml.createInstance4All();
        Xml.writeValueAsBytesDflt(null);
        byte[] b = Xml.writeValueAsBytesDflt(new Object());
        Object o = Xml.readValueDflt(b, Object.class);
        Assert.assertNotNull(o);

        Data d = new Data();
        b = Xml.writeValueAsBytesDflt(d);
        Data d1 = Xml.readValueDflt(b, Data.class);
        Assert.assertNotNull(d1);
        Assert.assertEquals(d.getsValue(), d1.getsValue());
        Assert.assertEquals(d.getiValue(), d1.getiValue());

        String s = Xml.writeValueAsStringDflt(d);
        d1 = Xml.readValueDflt(s, Data.class);
        Assert.assertNotNull(d1);
        Assert.assertEquals(d.getsValue(), d1.getsValue());
        Assert.assertEquals(d.getiValue(), d1.getiValue());
    }

}
