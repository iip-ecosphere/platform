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

package test.de.oktoflow.platform.support.json.jackson;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.json.Json;
import de.oktoflow.platform.support.json.jackson.JacksonJson;

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
        Assert.assertTrue(json instanceof JacksonJson);

        Data data = new Data();
        data.setiValue(10);
        data.setsValue("abba");
        String s = Json.toJsonDflt(data);
        Data data1 = Json.fromJsonDflt(s, Data.class);
        assertData(data1, data);
        
        s = json.toJson(data);
        data1 = json.fromJson(s, Data.class);
        assertData(data1, data);
    }

}
