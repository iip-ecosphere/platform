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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.iip_ecosphere.platform.connectors.formatter.DummyFormatter;
import de.iip_ecosphere.platform.connectors.formatter.FormatterUtils;
import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter;
import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter.OutputConverter;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.formatter.TextLineFormatter;

/**
 * Tests {@link OutputFormatter} functionality.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OutputFormatterTest {
    
    /**
     * Tests the formatter.
     */
    @Test
    public void testFormatter() throws IOException {
        String charset = StandardCharsets.UTF_8.name();
        TextLineFormatter formatter = new TextLineFormatter(charset, "#");
        OutputConverter<String> conv = formatter.getConverter();
        Assert.assertNotNull(conv);
        formatter.add("field1", conv.fromString("abba"));
        formatter.add("field2", conv.fromInteger(21));
        formatter.add("field2", conv.fromDouble(1.234));
        formatter.add("field2", conv.fromFloat(1.235f));
        formatter.add("field2", conv.fromLong(3456L));
        formatter.add("field2", conv.fromBoolean(true));
        byte[] data = formatter.chunkCompleted();

        String text = new String(data, charset);
        Assert.assertEquals("abba#21#1.234#1.235#3456#true", text);
    }

    
    /**
     * A test formatter without encoding.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomBaseFormatter implements OutputFormatter<String> {
        
        @Override
        public void add(String name, String data) throws IOException {
        }

        @Override
        public byte[] chunkCompleted() throws IOException {
            return null;
        }

        @Override
        public OutputConverter<String> getConverter() {
            return null;
        }
        
    }

    /**
     * A test formatter with encoding.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomExBaseFormatter extends CustomBaseFormatter {
        
        private String encoding;
        
        /**
         * Creates an instance and sets the {@code encoding}.
         * 
         * @param encoding the character encoding
         */
        public CustomExBaseFormatter(String encoding) {
            this.encoding = encoding;
        }
        
        /**
         * Returns the encoding.
         * 
         * @return the encoding
         */
        public String getEncoding() {
            return encoding;
        }
        
    }

    /**
     * Tests {@link FormatterUtils#createInstance(ClassLoader, String, Class, String)}.
     */
    @Test
    public void testCreateInstance() {
        ClassLoader loader = OutputFormatterTest.class.getClassLoader();
        Assert.assertTrue(FormatterUtils.createInstance(loader, "me.here.Parser", "UTF-8") instanceof DummyFormatter);
        
        Assert.assertNotNull(FormatterUtils.createInstance(loader, CustomBaseFormatter.class.getName(), "UTF-8"));
        OutputFormatter<?> f = FormatterUtils.createInstance(loader, CustomExBaseFormatter.class.getName(), "UTF-8");
        Assert.assertTrue(f instanceof CustomExBaseFormatter);
        Assert.assertEquals("UTF-8", ((CustomExBaseFormatter) f).getEncoding());
    }

}
