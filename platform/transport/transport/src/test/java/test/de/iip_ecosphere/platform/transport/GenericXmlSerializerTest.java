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

import de.iip_ecosphere.platform.transport.serialization.GenericXmlSerializer;
import test.de.iip_ecosphere.platform.transport.GenericJsonSerializerTest.DataClass;
import test.de.iip_ecosphere.platform.transport.GenericJsonSerializerTest.DataClassWithCopyConstructor;

import org.junit.Assert;

/**
 * Tests {@link GenericXmlSerializer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GenericXmlSerializerTest {

    /**
     * Tests {@link GenericXmlSerializer} with {@code DataClass}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDataClassSerializer() throws IOException {
        GenericXmlSerializer<DataClass> ser = new GenericXmlSerializer<>(DataClass.class);
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
     * Tests {@link GenericXmlSerializer} with {@code DataClassWithCopyConstructor}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDataClassWithCopyConstructorSerializer() throws IOException {
        GenericXmlSerializer<DataClassWithCopyConstructor> ser 
            = new GenericXmlSerializer<>(DataClassWithCopyConstructor.class);
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
