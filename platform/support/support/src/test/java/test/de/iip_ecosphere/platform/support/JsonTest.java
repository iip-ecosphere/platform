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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonArray;
import de.iip_ecosphere.platform.support.json.JsonNumber;
import de.iip_ecosphere.platform.support.json.JsonObject;
import de.iip_ecosphere.platform.support.json.JsonString;

/**
 * Tests {@link Json}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonTest {

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
     * Tests basic YAML functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJson() throws IOException {
        Json json = Json.createInstance();

        Data data = new Data();
        data.setiValue(10);
        data.setsValue("abba");
        String s = Json.toJsonDflt(data);
        Data data1 = Json.fromJsonDflt(s, Data.class);
        assertData(data1, data);
        
        s = json.toJson(data);
        data1 = json.fromJson(s, Data.class);
        assertData(data1, data);
        
        byte[] b = json.writeValueAsBytes(data);
        data1 = json.readValue(b, Data.class);
        assertData(data1, data);
    }
    
    /**
     * Tests basic {@link JsonObject} functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonObject() throws IOException {
        JsonObject jobj = Json.createObjectBuilder()
            .add("intVal", 1)
            .add("strVal", "abc")
            .add("boolVal", true)
            .add("dblVal", 2.0)
            .add("arr", Json.createArrayBuilder()
                .add(1)
                .add("str")
                .add(true)
                .add(Json.createArrayBuilder()) // as array builder
                .build()) // as array value
            .build();
        String json = jobj.toString();
        
        JsonObject obj = Json.createObject(json);
        Assert.assertNotNull(obj);

        JsonNumber n1 = obj.getJsonNumber("intVal");
        Assert.assertNotNull(n1);
        Assert.assertEquals(1, n1.intValue());

        JsonNumber n2 = obj.getJsonNumber("dblVal");
        Assert.assertNotNull(n2);
        Assert.assertEquals(2.0, n2.intValue(), 0.01);

        JsonString s1 = obj.getJsonString("strVal");
        Assert.assertNotNull(s1);
        Assert.assertEquals("abc", s1.getString());

        Assert.assertEquals(1, obj.getInt("intVal"));
        Assert.assertEquals("abc", obj.getString("strVal"));
        Assert.assertEquals(true, obj.getBoolean("boolVal"));
        
        JsonArray a1 = obj.getJsonArray("arr");
        Assert.assertEquals(4, a1.size());
        Assert.assertNotNull(a1);
        Assert.assertEquals("str", a1.getString(1));
        Assert.assertEquals(1, a1.getInt(0));
        Assert.assertEquals(true, a1.getBoolean(2));
        JsonArray a2 = a1.getJsonArray(3);
        Assert.assertNotNull(a2);
        Assert.assertEquals(0, a2.size());
    }

}
