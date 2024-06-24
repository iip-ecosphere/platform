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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.model.AbstractTypeMappingModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Tests {@link AbstractTypeMappingModelAccess}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractTypeMappingModelAccessTest {

    private int intValue;
    private double doubleValue;
    private String stringValue;
    private float floatValue;
    private boolean booleanValue;
    private long longValue;
    private short shortValue;
    private byte byteValue;
    
    /**
     * The instance to test.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyAccess extends AbstractTypeMappingModelAccess {

        /**
         * Creates an instance.
         */
        protected MyAccess() {
            super(null);
        }

        @Override
        public String topInstancesQName() {
            return null;
        }

        @Override
        public String getQSeparator() {
            return null;
        }

        @Override
        public Object call(String qName, Object... args) throws IOException {
            return null;
        }

        @Override
        public Object get(String qName) throws IOException {
            return null;
        }

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            return null;
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
        }

        @Override
        public void monitor(int notificationInterval, String... qNames) throws IOException {
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
        }

        @Override
        public ModelAccess stepInto(String name) throws IOException {
            return null;
        }

        @Override
        public ModelAccess stepOut() {
            return null;
        }

        @Override
        public void setInt(String qName, int value) throws IOException {
            intValue = value;            
        }

        @Override
        public void setLong(String qName, long value) throws IOException {
            longValue = value;            
        }

        @Override
        public void setByte(String qName, byte value) throws IOException {
            byteValue = value;            
        }

        @Override
        public void setShort(String qName, short value) throws IOException {
            shortValue = value;            
        }

        @Override
        public void setBoolean(String qName, boolean value) throws IOException {
            booleanValue = value;            
        }

        @Override
        public void setDouble(String qName, double value) throws IOException {
            doubleValue = value;            
        }

        @Override
        public void setFloat(String qName, float value) throws IOException {
            floatValue = value;            
        }

        @Override
        public void setString(String qName, String value) throws IOException {
            stringValue = value;            
        }

        @Override
        public ConnectorParameter getConnectorParameter() {
            return null;
        }
        
    }
    
    /**
     * Tests {@link AbstractTypeMappingModelAccess}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testAccess() throws IOException {
        MyAccess acc = new MyAccess();
        acc.set("a", 1);
        Assert.assertEquals(1, intValue);
        acc.set("a", 12959L);
        Assert.assertEquals(12959L, longValue);
        acc.set("a", 0.125);
        Assert.assertEquals(0.125, doubleValue, 0.001);
        acc.set("a", (float) 0.234);
        Assert.assertEquals(0.125, floatValue, 0.234);
        acc.set("a", (byte) 10);
        Assert.assertEquals(10, byteValue);
        acc.set("a", (short) 1234);
        Assert.assertEquals(1234, shortValue);
        acc.set("a", true);
        Assert.assertEquals(true, booleanValue);
        acc.set("a", "abbc");
        Assert.assertEquals("abbc", stringValue);
        stringValue = null;
        acc.set("a", new Object());
        Assert.assertNotNull(stringValue);
    }
    
}
