/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.GenericJsonSerializer;
import org.junit.Assert;

/**
 * Tests {@link GenericJsonSerializer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GenericJsonSerializerTest {

    /**
     * Simple, platform-like data class.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DataClass {
        
        private int value;
        
        /**
         * Returns the value.
         * 
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * Changes the value.
         * 
         * @param value the value
         */
        public void setValue(int value) {
            this.value = value;
        }

    }

    /**
     * Simple, platform-like data class with copy constructor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DataClassWithCopyConstructor extends DataClass {

        /**
         * Creates an instance.
         */
        public DataClassWithCopyConstructor() {
        }

        /**
         * Creates an instance by copying {@code data}.
         * 
         * @param data the instance to copy from
         */
        @SuppressWarnings("unused") // used through serializer
        public DataClassWithCopyConstructor(DataClassWithCopyConstructor data) {
            setValue(data.getValue());
        }

    }

    /**
     * Tests {@link GenericJsonSerializer} with {@code DataClass}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDataClassSerializer() throws IOException {
        GenericJsonSerializer<DataClass> ser = new GenericJsonSerializer<>(DataClass.class);
        DataClass d = new DataClass();
        d.setValue(52);
        
        DataClass r = ser.from(ser.to(d));

        Assert.assertNotNull(r);
        Assert.assertEquals(d.getValue(), r.getValue());
        Assert.assertEquals(DataClass.class, ser.getType());
        
        r = ser.clone(r);
        Assert.assertNotNull(r);
        Assert.assertEquals(0, r.getValue()); // default value, no copy constructor
    }

    /**
     * Tests {@link GenericJsonSerializer} with {@code DataClassWithCopyConstructor}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDataClassWithCopyConstructorSerializer() throws IOException {
        GenericJsonSerializer<DataClassWithCopyConstructor> ser 
            = new GenericJsonSerializer<>(DataClassWithCopyConstructor.class);
        DataClassWithCopyConstructor d = new DataClassWithCopyConstructor();
        d.setValue(42);
        
        DataClassWithCopyConstructor r = ser.from(ser.to(d));

        Assert.assertNotNull(r);
        Assert.assertEquals(d.getValue(), r.getValue());
        Assert.assertEquals(DataClassWithCopyConstructor.class, ser.getType());
        
        r = ser.clone(r);
        Assert.assertNotNull(r);
        Assert.assertEquals(d.getValue(), r.getValue()); // default value, no copy constructor
    }

}
