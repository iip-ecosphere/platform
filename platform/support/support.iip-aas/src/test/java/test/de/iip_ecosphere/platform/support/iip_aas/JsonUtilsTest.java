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

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

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

}
