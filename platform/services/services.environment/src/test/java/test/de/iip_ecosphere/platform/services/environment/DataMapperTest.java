/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.DataMapper;
import de.iip_ecosphere.platform.services.environment.DataMapper.MappingConsumer;

import org.junit.Assert;

/**
 * Tests {@link DataMapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DataMapperTest {

    private static int count = 0;

    /**
     * Some input data for a service. [testing]
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyInput1 {
        
        private int value;

        /**
         * Returns the integer value.
         * 
         * @return the intVal
         */
        public int getValue() {
            return value;
        }
        
        /**
         * Defines the integer value. [required by jackson]
         * 
         * @param value the value to set
         */
        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, 
                de.iip_ecosphere.platform.services.environment.IipStringStyle.SHORT_STRING_STYLE);
        }

    }
    
    /**
     * Some input data for a service. [testing]
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyInput2 {
        
        private int intVal;
        private String text;

        /**
         * Returns the integer value.
         * 
         * @return the intVal
         */
        public int getIntVal() {
            return intVal;
        }
        
        /**
         * Defines the integer value. [required by jackson]
         * 
         * @param intVal the intVal to set
         */
        public void setIntVal(int intVal) {
            this.intVal = intVal;
        }

        /**
         * Returns the text/string data.
         * 
         * @return the text
         */
        public String getText() {
            return text;
        }
        
        /**
         * Defines the text/string data. [required by jackson]
         * 
         * @param text the text to set
         */
        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, 
                de.iip_ecosphere.platform.services.environment.IipStringStyle.SHORT_STRING_STYLE);
        }

    }

    /**
     * Generated data unit, representing alternative intputs to a service.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DataUnit {
        
        // alternative inputs, may be null
        private MyInput1 myInput1;
        private MyInput2 myInput2;
        
        /**
         * Returns the first input.
         * 
         * @return the first input, may be <b>null</b>
         */
        public MyInput1 getMyInput1() {
            return myInput1;
        }
        
        /**
         * Defines the first input. [required by jackson]
         * 
         * @param myInput1 the input to set
         */
        public void setMyInput1(MyInput1 myInput1) {
            this.myInput1 = myInput1;
        }
        
        /**
         * Returns the second input.
         * 
         * @return the second input, may be <b>null</b>
         */
        public MyInput2 getMyInput2() {
            return myInput2;
        }
        
        /**
         * Defines the second input. [required by jackson]
         * 
         * @param myInput2 the input to set
         */
        public void setMyInput2(MyInput2 myInput2) {
            this.myInput2 = myInput2;
        }
        
        @Override
        public String toString() {
            return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, 
                de.iip_ecosphere.platform.services.environment.IipStringStyle.SHORT_STRING_STYLE);
        }
        
    }
    
    /**
     * Creates a test input stream with 2 elements.
     * 
     * @return the input stream
     */
    private static InputStream createTestInputStream() {
        String input = "{\"myInput1\":{\"value\":\"24\"}}\n" 
                + "{\"myInput2\":{\"intVal\":\"10\", \"text\":\"abc\"}}\n";
        return new ByteArrayInputStream(input.getBytes());
    }
    
    /**
     * Asserts an instance of {@link MyInput1} against {@link #createTestInputStream()}.
     * 
     * @param input the input instance
     */
    private static void assertMyInput1(MyInput1 input) {
        Assert.assertNotNull(input);
        System.out.println("Asserting(1): " + input);
        Assert.assertEquals(24, input.getValue());
        count++;
    }

    /**
     * Asserts an instance of {@link MyInput2} against {@link #createTestInputStream()}.
     * 
     * @param input the input instance
     */
    private static void assertMyInput2(MyInput2 input) {
        Assert.assertNotNull(input);
        System.out.println("Asserting(2): " + input);
        Assert.assertEquals(10, input.getIntVal());
        Assert.assertEquals("abc", input.getText());
        count++;
    }
    
    /**
     * Tests {@link DataMapper#mapJsonData(InputStream, Class, Consumer)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonDataMapper() throws IOException {
        count = 0;
        System.out.println("Specific:");        
        Consumer<DataUnit> cons = new Consumer<DataUnit>() {
            
            @Override
            public void accept(DataUnit unit) {
                System.out.println("Received: " + unit);
                Assert.assertNotNull(unit);
                if (count == 0) {
                    assertMyInput1(unit.getMyInput1());
                    Assert.assertNull(unit.getMyInput2());
                } else if (count == 1) {
                    Assert.assertNull(unit.getMyInput1());
                    assertMyInput2(unit.getMyInput2());
                } else {
                    Assert.fail("Unexpected instance " + unit);
                }
            }

        };
        DataMapper.mapJsonData(createTestInputStream(), DataUnit.class, cons);
        Assert.assertEquals(2, count);
        count = 0;
    }

    /**
     * Tests {@link DataMapper#mapJsonData(InputStream, Class, Consumer)} with {@link MappingConsumer}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testGenericJsonDataMapper() throws IOException {
        count = 0;
        System.out.println("Generic:");        
        MappingConsumer<DataUnit> mConsumer = new MappingConsumer<>(DataUnit.class);
        mConsumer.addHandler(MyInput1.class, u -> assertMyInput1(u));
        mConsumer.addHandler(MyInput2.class, u -> assertMyInput2(u));
        DataMapper.mapJsonData(createTestInputStream(), DataUnit.class, mConsumer);
        Assert.assertEquals(2, count);
        count = 0;
    }

}
