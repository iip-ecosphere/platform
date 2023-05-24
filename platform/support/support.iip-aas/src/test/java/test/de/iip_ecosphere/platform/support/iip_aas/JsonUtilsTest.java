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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import iip.datatypes.Abc;
import iip.datatypes.AbcImpl;

/**
 * Tests {@link JsonUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonUtilsTest {
    
    /**
     * A data object for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Data {
        
        private int intValue;
        private String stringValue;

        /**
         * Creates a data object (for JSON).
         */
        private Data() {
        }
        
        /**
         * Creates a data object.
         * 
         * @param intValue the int value
         * @param stringValue the string value
         */
        private Data(int intValue, String stringValue) {
            this.intValue = intValue;
            this.stringValue = stringValue;
        }

        /**
         * Returns the int value.
         * 
         * @return the int value
         */
        public int getIntValue() {
            return intValue;
        }

        /**
         * Defines the int value.
         * 
         * @param intValue the int value
         */
        void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        /**
         * Returns the String value.
         * 
         * @return the String value
         */
        public String getStringValue() {
            return stringValue;
        }
        
        /**
         * Defines the string value.
         * 
         * @param stringValue the string value
         */
        void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }
        
    }

    /**
     * Tests optional values.
     */
    @Test
    public void testOptionals() {
        String data = "{\"intValue\":\"1\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonUtils.defineOptionals(objectMapper, Data.class, "stringValue");
        try {
            Data obj = objectMapper.readValue(data, Data.class);
            Assert.assertEquals(1, obj.getIntValue());
            Assert.assertNull(obj.getStringValue());
        } catch (JsonProcessingException e) {
            Assert.fail("Shall not occur");
        }

        data = "{\"stringValue\":\"xyz\"}";
        objectMapper = new ObjectMapper();
        JsonUtils.defineOptionals(objectMapper, Data.class, "intValue");
        try {
            Data obj = objectMapper.readValue(data, Data.class);
            Assert.assertEquals(0, obj.getIntValue());
            Assert.assertEquals("xyz", obj.getStringValue());
        } catch (JsonProcessingException e) {
            Assert.fail("Shall not occur");
        }

        data = "{}";
        objectMapper = new ObjectMapper();
        JsonUtils.defineOptionals(objectMapper, Data.class, "stringValue", "intValue");
        try {
            Data obj = objectMapper.readValue(data, Data.class);
            Assert.assertEquals(0, obj.getIntValue());
            Assert.assertNull(obj.getStringValue());
        } catch (JsonProcessingException e) {
            Assert.fail("Shall not occur");
        }
    }
    
    /**
     * Tests the generic from/to JSON functions.
     */
    @Test
    public void testToFromJson() {
        String json = JsonUtils.toJson(null);
        Data data = JsonUtils.fromJson(json, Data.class);
        Assert.assertNull(data);
        
        data = new Data(25, "abba");
        json = JsonUtils.toJson(data);
        Data tmp = JsonUtils.fromJson(json, Data.class);
        Assert.assertNotNull(tmp);
        Assert.assertEquals(data.getStringValue(), tmp.getStringValue());
        Assert.assertEquals(data.getIntValue(), tmp.getIntValue());
    }
    
    /**
     * Tests the from/to JSON functions for {@link ServerAddress}.
     */
    @Test
    public void testServerAddress() {
        ServerAddress addr = null;
        String json = JsonUtils.toJson(addr);
        ServerAddress tmp = JsonUtils.serverAddressFromJson(json);
        Assert.assertNull(tmp);
        
        addr = new ServerAddress(Schema.TCP, "me.here", 10321);
        json = JsonUtils.toJson(addr);
        tmp = JsonUtils.serverAddressFromJson(json);
        Assert.assertNotNull(tmp);
        Assert.assertEquals(addr.getSchema(), tmp.getSchema());
        Assert.assertEquals(addr.getHost(), tmp.getHost());
        Assert.assertEquals(addr.getPort(), tmp.getPort());
    }
    
    /**
     * Tests {@link JsonUtils#escape(String)} and {@link JsonUtils#unescape(String)}.
     */
    @Test
    public void testEscapeUnescape() {
        // external code, just some basic tests
        assertEscapeUnescape("");
        assertEscapeUnescape("aaa");
        assertEscapeUnescape("{}");
        assertEscapeUnescape("{\"name\": \"abc\", \"value\": 1, \"enum\":20, \"enumName\":\"TEST1\"}");
        assertEscapeUnescape("{\"obj\": {\"name\": \"abc\", \"value\": 1}}");
    }
    
    /**
     * Asserts that escaping and unescaping leads back to the input {@code string}.
     * 
     * @param string the string to use
     */
    private static void assertEscapeUnescape(String string) {
        Assert.assertEquals(string, JsonUtils.unescape(JsonUtils.escape(string)));
    }

    /**
     * Tests {@link JsonUtils#handleIipDataClasses(ObjectMapper)}.
     * 
     * @throws JsonProcessingException shall not occur
     */
    @Test
    public void testIipTypes() throws JsonProcessingException {
        Abc abc = new AbcImpl();
        abc.setValue(42);
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(abc);

        JsonUtils.handleIipDataClasses(objectMapper);
        Abc test = objectMapper.readValue(str, Abc.class);
        Assert.assertNotNull(test);
        Assert.assertEquals(abc.getValue(), test.getValue());
    }
    
    // checkstyle: checkstyle: stop names check
    
    /**
     * Json class as it would be generated for 
     * 
     * @author Holger Eichelberger, SSE
     */
    static class PropertyData {
        private int INTVALUE = 5;
        private String stringVAlue;
        
        public int getINTVALUE() {
            return INTVALUE;
        }

        public String getStringVAlue() {
            return stringVAlue;
        }

        public void setINTVALUE(int INTVALUE) {
            this.INTVALUE = INTVALUE;
        }

        public void setStringVAlue(String stringVAlue) {
            this.stringVAlue = stringVAlue;
        }

    }

    // checkstyle: checkstyle: resume names check

    /**
     * Tests the mapping property naming strategy.
     * 
     * @throws JsonProcessingException shall not occur
     */
    @Test
    public void testPropertyNaming() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String str = "{\"INTVALUE\":1, \"stringVAlue\":\"abba\"}";
        JsonUtils.defineFields(objectMapper, "INTVALUE", "stringVAlue");
        PropertyData test = objectMapper.readValue(str, PropertyData.class);
        Assert.assertNotNull(test);
        Assert.assertEquals(1, test.getINTVALUE());
        Assert.assertEquals("abba", test.getStringVAlue());
    }
    
    /**
     * Tests strange names in {@link PropertyData} with {@link JsonUtils#toJson(Object)} 
     * and {@link JsonUtils#fromJson(Object, Class)}.
     */
    @Test
    public void testToJson() {
        PropertyData data = new PropertyData();
        String json = JsonUtils.toJson(data);
        Assert.assertTrue(json.contains("INTVALUE"));
        Assert.assertTrue(json.contains("stringVAlue"));
        PropertyData d1 = JsonUtils.fromJson(json, PropertyData.class);
        Assert.assertEquals(data.getStringVAlue(), d1.getStringVAlue());
        Assert.assertEquals(data.getINTVALUE(), d1.getINTVALUE());
    }

}
