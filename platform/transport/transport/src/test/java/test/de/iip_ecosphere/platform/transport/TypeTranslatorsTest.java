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

package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.StringSerializer;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * Tests the default type translators in {@link TypeTranslators}. We focus in particular on types that could be useful
 * for the code generation, e.g., service parameter values.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TypeTranslatorsTest {

    /**
     * Tests {@link TypeTranslators#STRING}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testString() throws IOException {
        Assert.assertEquals("", TypeTranslators.STRING.to(TypeTranslators.STRING.from("")));
        Assert.assertEquals("abc", TypeTranslators.STRING.to(TypeTranslators.STRING.from("abc")));
    }

    /**
     * Tests {@link TypeTranslators#INTEGER}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testInteger() throws IOException {
        final int test = 1234;
        Assert.assertEquals(test, TypeTranslators.INTEGER.to(TypeTranslators.INTEGER.from(test)).intValue());
        try {
            TypeTranslators.INTEGER.to("abba");
            Assert.fail("No IOException thrown");
        } catch (IOException e) {
            // this is ok
        }
    }

    /**
     * Tests {@link TypeTranslators#JSON_STRING}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testJsonString() throws IOException {
        final String test = "a\"\b";
        Assert.assertEquals(test, TypeTranslators.JSON_STRING.to(TypeTranslators.JSON_STRING.from(test)));
    }

    /**
     * Tests {@link TypeTranslators#LONG}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testLong() throws IOException {
        final long test = 12354058L;
        Assert.assertEquals(test, TypeTranslators.LONG.to(TypeTranslators.LONG.from(test)).longValue());
    }

    /**
     * Tests {@link TypeTranslators#BOOLEAN}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testBoolean() throws IOException {
        Assert.assertTrue(TypeTranslators.BOOLEAN.to(TypeTranslators.BOOLEAN.from(true)));
        Assert.assertFalse(TypeTranslators.BOOLEAN.to(TypeTranslators.BOOLEAN.from(false)));
    }

    /**
     * Tests {@link TypeTranslators#LONG}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDouble() throws IOException {
        final double test = 12.353058;
        Assert.assertEquals(test, TypeTranslators.DOUBLE.to(TypeTranslators.DOUBLE.from(test)).doubleValue(), 0.001);
    }
    
    public static class TestTypeTranslator implements TypeTranslator<String, String> {

        @Override
        public String from(String data) throws IOException {
            return null;
        }

        @Override
        public String to(String source) throws IOException {
            return null;
        }
        
    }

    /**
     * Tests {@link TypeTranslators#createTypeTranslator(ClassLoader, String)}.
     */
    @Test
    public void testCreateTypeTranslator() {
        Assert.assertNotNull(TypeTranslators.createTypeTranslator(getClass().getClassLoader(), 
            TestTypeTranslator.class.getName()));
        Assert.assertNull(TypeTranslators.createTypeTranslator(getClass().getClassLoader(), 
            "abc.defg"));
    }

    /**
     * Tests {@link TypeTranslators#createSerializer(ClassLoader, String)}.
     */
    @Test
    public void testCreateSerializer() {
        Assert.assertNotNull(TypeTranslators.createSerializer(getClass().getClassLoader(), 
            StringSerializer.class.getName()));
        Assert.assertNull(TypeTranslators.createSerializer(getClass().getClassLoader(), 
            "abc.def"));
    }

}
