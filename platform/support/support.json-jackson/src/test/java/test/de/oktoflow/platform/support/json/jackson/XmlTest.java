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

package test.de.oktoflow.platform.support.json.jackson;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Filter;
import de.iip_ecosphere.platform.support.IgnoreProperties;
import de.iip_ecosphere.platform.support.xml.Xml;
import de.oktoflow.platform.support.json.jackson.JacksonXml;
import iip.datatypes.DataImpl;

/**
 * Tests {@link Xml}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class XmlTest {
    
    public static final String FILTER_ID_DATA = "dataFilter";

    /**
     * A test data class.
     * 
     * @author Holger Eichelberger, SSE
     */
    @Filter(FILTER_ID_DATA)
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
         * Changes the string value. [JSON]
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
         * Changes the string value. [JSON]
         * 
         * @param sValue the sValue to set
         */
        public void setsValue(String sValue) {
            this.sValue = sValue;
        } 
        
    }
    
    /**
     * Asserts data instances.
     *
     * @param value the value to test
     * @param expected the expected value
     */
    private void assertData(Data value, Data expected) {
        Assert.assertNotNull(value);
        Assert.assertEquals(value.getiValue(), expected.getiValue());
        Assert.assertEquals(value.getsValue(), expected.getsValue());
    }

    /**
     * Tests basic XML functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testXml() throws IOException {
        Xml xml = Xml.createInstance();
        Assert.assertTrue(xml instanceof JacksonXml);
        
        Data data = new Data();
        data.setiValue(10);
        data.setsValue("abba");
        String s = xml.writeValueAsString(data);
        Data data1 = xml.readValue(s, Data.class);
        assertData(data1, data);
        
        byte[] b = xml.writeValueAsBytes(new Object());
        Object o = xml.readValue(b, Object.class);
        Assert.assertNotNull(o);
        
        b = xml.writeValueAsBytes(data);
        data1 = xml.readValue(b, Data.class);
        assertData(data1, data);
    }

    /**
     * Tests {@link Xml#handleIipDataClasses()}.
     */
    @Test
    public void testHandleIipDataClasses() {
        iip.datatypes.Data data = new DataImpl();
        data.setValue(20);
        
        try {
            String str = Xml.writeValueAsStringDflt(data);
            Assert.assertFalse(str.contains("iField"));
            Assert.assertTrue(str.contains("value"));
            Xml.readValueDflt(str, iip.datatypes.Data.class);
            Assert.fail("Shall not succeed as cannot instantiate interface");
        } catch (IOException e) {
            // ok
        }
        Xml xml = Xml.createInstance(iip.datatypes.Data.class, DataImpl.class).handleIipDataClasses();
        try {
            String str = xml.writeValueAsString(data);
            Assert.assertTrue(str.contains("iField"));
            Assert.assertFalse(str.contains("value"));
            iip.datatypes.Data d = xml.readValue(str, iip.datatypes.Data.class);
            Assert.assertNotNull(d);
            Assert.assertEquals(data.getValue(), d.getValue());
        } catch (IOException e) {
            Assert.fail("Shall not fail");
        }

        xml = Xml.createInstance4All().handleIipDataClasses();
        try {
            String str = xml.writeValueAsString(data);
            Assert.assertTrue(str.contains("iField"));
            Assert.assertFalse(str.contains("value"));
            iip.datatypes.Data d = xml.readValue(str, iip.datatypes.Data.class);
            Assert.assertNotNull(d);
            Assert.assertEquals(data.getValue(), d.getValue());
        } catch (IOException e) {
            Assert.fail("Shall not fail");
        }
    }
    
    /**
     * Tests {@link Xml#exceptFields(String...)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testExceptFields() throws IOException {
        DataImpl data = new DataImpl();
        // consider plain fields
        Xml xml = Xml.createInstance().exceptFields("value");
        String str = xml.writeValueAsString(data);
        Assert.assertFalse(str.contains("iField"));
        Assert.assertFalse(str.contains("value"));

        // consider annotations
        xml = Xml.createInstance4All().exceptFields("iField");
        str = xml.writeValueAsString(data);
        Assert.assertFalse(str.contains("iField"));
        Assert.assertFalse(str.contains("value"));
    }

    @IgnoreProperties(ignoreUnknown = true)
    static class TestIgnoreUnknown {
        
        private boolean flag;

        /**
         * Returns the flag.
         * 
         * @return the flag value
         */
        public boolean getFlag() {
            return flag;
        }

        /**
         * Changes the flag.
         * 
         * @param flag the new flag value
         */
        public void setFlag(boolean flag) {
            this.flag = flag;
        }
        
    }
    
    /**
     * Tests {@link IgnoreProperties} and {@link Xml#failOnUnknownProperties(boolean)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIgnoreUnkown() throws IOException {
        Xml xml = Xml.createInstance(TestIgnoreUnknown.class); // or 4All
        String data = "<TestIgnoreUnknown><field>123</field><flag>true</flag></TestIgnoreUnknown>";
        TestIgnoreUnknown obj = xml.readValue(data, TestIgnoreUnknown.class);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.getFlag());
        
        xml = Xml.createInstance().failOnUnknownProperties(false); // or 4All
        obj = xml.readValue(data, TestIgnoreUnknown.class);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.getFlag());
    }

}
