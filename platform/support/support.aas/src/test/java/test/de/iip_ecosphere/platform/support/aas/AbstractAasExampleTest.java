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

import de.iip_ecosphere.platform.support.aas.LangString;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    
    /**
     * A simple testing enumeration.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum E1 {
        VAL1,
        VAL2;
    }

    /**
     * A testing enumeration.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum E2 {
        VAL1(0, "iri:semId-v01", "Value One"),
        VAL2(0, "iri:semId-v02", "Value Two");
        
        private int valueId;
        private String semanticId;
        private String value;
    
        /**
         * Creates a constant.
         * 
         * @param valueId the value id/given ordinal
         * @param semanticId the semantic id
         * @param value the value
         */
        private E2(int valueId, String semanticId, String value) {
            this.valueId = valueId;
            this.semanticId = semanticId;
            this.value = value;
        }
    
        /**
         * Returns the value id/given ordinal.
         * 
         * @return the value id/given ordinal
         */
        @SuppressWarnings("unused")
        public int getValueId() {
            return valueId;
        }
        
        /**
         * Returns the semantic id.
         * 
         * @return the semantic id
         */
        @SuppressWarnings("unused")
        public String getSemanticId() {
            return semanticId;
        }
        
        /**
         * Returns the value to be used in the AAS.
         * 
         * @return the value to be used in the AAS
         */
        @SuppressWarnings("unused")
        public String getValue() {
            return value;
        }
        
    }

    /**
     * Tests {@link AbstractAasExample#toTestEnum(Class, String, Enum)}.
     */
    @Test
    public void testToTestEnum() {
        Assert.assertEquals(E1.VAL2, AbstractAasExample.toTestEnum(E1.class, "VAL", E1.VAL2));
        Assert.assertEquals(E1.VAL1, AbstractAasExample.toTestEnum(E1.class, "VAL")); // first declared as default

        Assert.assertEquals(E1.VAL1, AbstractAasExample.toTestEnum(E1.class, "VAL1", E1.VAL2));
        Assert.assertEquals(E2.VAL2, AbstractAasExample.toTestEnum(E2.class, "VAL2", E2.VAL2));
        Assert.assertEquals(E2.VAL2, AbstractAasExample.toTestEnum(E2.class, "Value Two", E2.VAL1));
        Assert.assertEquals(E2.VAL2, AbstractAasExample.toTestEnum(E2.class, "iri:semId-v02", E2.VAL1));
    }

    /**
     * Tests {@link AbstractAasExample#toTestLangString(String, String)}.
     */
    @Test
    public void testToLangString() {
        AbstractAasExample.assertLangStringsEquals(new LangString[0], AbstractAasExample.toTestLangString(null, null));
        AbstractAasExample.assertLangStringsEquals(new LangString[] {new LangString("de", "test")}, 
            AbstractAasExample.toTestLangString(null, "test@de"));
        AbstractAasExample.assertLangStringsEquals(new LangString[] {new LangString("en", "test1")}, 
            AbstractAasExample.toTestLangString("test1@en", "test@de"));
        AbstractAasExample.assertLangStringsEquals(new LangString[] {new LangString("en", "test1"), 
            new LangString("de", "test")}, AbstractAasExample.toTestLangString("test1@en test@de", "t@de"));
    }
    
    /**
     * Tests {@link AbstractAasExample#toTestResourceFile(String, String)} and 
     * {@link AbstractAasExample#toTestResourceMimeType(String, String)}. 
     */
    @Test
    public void testFileResource() {
        Assert.assertEquals("ab", AbstractAasExample.toTestResourceFile(null, "ab"));
        Assert.assertEquals("ab", AbstractAasExample.toTestResourceFile("", "ab"));

        Assert.assertEquals("ab", AbstractAasExample.toTestResourceMimeType(null, "ab"));
        Assert.assertEquals("ab", AbstractAasExample.toTestResourceMimeType("", "ab"));

        String mimeValue = "image/png";
        String mimeTest = "MimeType = " + mimeValue;
        String valueValue = "/aasx/TechnicalData/logo.png";
        String valueTest = "Value = " + valueValue;
        String test = mimeTest + " " + valueTest;
        Assert.assertEquals(mimeValue, AbstractAasExample.toTestResourceMimeType(test, "ab"));
        Assert.assertEquals(valueValue, AbstractAasExample.toTestResourceFile(test, "ab"));

        test = valueTest + " " + mimeTest; 
        Assert.assertEquals(mimeValue, AbstractAasExample.toTestResourceMimeType(test, "ab"));
        Assert.assertEquals(valueValue, AbstractAasExample.toTestResourceFile(test, "ab"));
    }

    /**
     * Tests {@link AbstractAasExample#toTestDate(String, java.util.Date)}. 
     */
    @Test
    public void testDate() {
        Date dflt = new Date();
        Assert.assertEquals(dflt, AbstractAasExample.toTestDate(null, dflt));
        Assert.assertEquals(dflt, AbstractAasExample.toTestDate("", dflt));
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2024);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date test = cal.getTime();
        
        Assert.assertEquals(test, AbstractAasExample.toTestDate("1.1.2024", dflt));
        Assert.assertEquals(test, AbstractAasExample.toTestDate("2024/1/1", dflt));
    }
    
}
