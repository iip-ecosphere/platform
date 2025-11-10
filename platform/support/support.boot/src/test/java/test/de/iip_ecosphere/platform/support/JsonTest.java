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
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.json.Json;
import de.iip_ecosphere.platform.support.json.JsonIterator;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.logging.LogLevel;
import test.de.iip_ecosphere.platform.support.json.TestJson;

/**
 * "Tests" the {@link Json} interface.
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
     * Tests basic YAML functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJson() throws IOException {
        Json json = Json.createInstance();
        Assert.assertTrue(json instanceof TestJson);
        Json.setPrototype(json);
        
        Json.createInstance(Object.class);
        Json.createInstance(Object.class, Integer.class);
        Json.createInstance4All();
        Json.createArrayBuilder();
        Json.createObjectBuilder();
        Json.parse("");
        Json.parse("".getBytes());
        Json.writeValueAsStringDflt(null);
        Json.writeValueAsBytesDflt(null);
        Json.toJsonDflt(null);
        Json.readValueDflt("{}", Object.class);
        Json.readValueDflt("{}".getBytes(), Object.class);
        Json.fromJsonDflt(null, Object.class);
        Json.listFromJsonDflt(null, Object.class);
        Json.mapFromJsonDflt(null, String.class, Object.class);
        Json.createObject("");
        Json.createObject("".getBytes());
        Json.createObject((Reader) null);
        Json.createGenerator((Writer) null);
        Json.createEnumValueMap(JsonIterator.ValueType.class);

        json.toJsonQuiet(null);
        json.createEnumValueMapping(LogLevel.class);
        json.createEnumMapping(LogLevel.class);
    }
    
    /**
     * Tests {@link JsonIterator}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonIter() throws IOException {
        // not implemented here
        Json.parse(new byte[0]);
        Json.parse("");
    }
    
    /**
     * Tests {@link JsonUtils}.
     */
    @Test
    public void testJsonUtils() {
        Data data = new Data();
        data.setiValue(120);
        data.setsValue("xyz");
        Data data2 = JsonUtils.fromJson(JsonUtils.toJson(data), Data.class);
        Assert.assertNotNull(data2);
        Assert.assertEquals(data.getiValue(), data2.getiValue());
        Assert.assertEquals(data.getsValue(), data2.getsValue());
        Assert.assertNull(JsonUtils.fromJson(null, Data.class));
        Assert.assertEquals("", JsonUtils.toJson(null));

        JsonUtils.listFromJson(null, Object.class); // no implementation
        JsonUtils.mapFromJson(null, String.class, Object.class); // no implementation
        
        String txt = "\"\n\t\r\\\b\f" + (char) 129;
        Assert.assertEquals(txt, JsonUtils.unescape(JsonUtils.escape(txt)));
    }


}
