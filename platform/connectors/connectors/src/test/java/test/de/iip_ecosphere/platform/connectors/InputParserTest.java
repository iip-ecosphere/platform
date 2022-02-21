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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.parser.InputParser.InputConverter;
import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;
import org.junit.Assert;

import de.iip_ecosphere.platform.connectors.parser.InputParser;
import de.iip_ecosphere.platform.connectors.parser.ParserUtils;
import de.iip_ecosphere.platform.connectors.parser.TextLineParser;

/**
 * Tests the {@link InputParser}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class InputParserTest {
    
    /**
     * Tests basic successful text line parsing with different charsets and separators.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testTextLineParser() throws IOException {
        String charset = StandardCharsets.UTF_8.name();
        final String[] parts = new String[]{"123", "bbb", "true", "0.45", "0.56", "12345"};
        testTextLineParser(parts, charset, "#");
        ParseResult<String> pr = testTextLineParser(parts, charset, "#-#");

        InputConverter<String> conv = TextLineParser.CONVERTER;
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("field1", 0);
        mapping.put("field2", 1);
        mapping.put("field3", 2);
        mapping.put("fieldX", 3);
        Assert.assertEquals(parts[3], conv.toString(pr.getData("fieldX", 0, mapping)));
        Assert.assertEquals(parts[1], conv.toString(pr.getData("field2", 0, mapping)));
        Assert.assertEquals(parts[1], conv.toString(pr.getData("f", 1, mapping)));
        Assert.assertEquals(0.45, conv.toDouble(pr.getData("f", 3, mapping)), 0.01);
        Assert.assertEquals(0.56, conv.toDouble(pr.getData("", 4, null)), 0.01);
        Assert.assertEquals(12345, conv.toInteger(pr.getData("", 5, null)));
        try {
            pr.getData("f", 10, mapping);
            Assert.fail("No Exception");
        } catch (IndexOutOfBoundsException e) {
        }

        charset = StandardCharsets.ISO_8859_1.name();
        testTextLineParser(parts, charset, "#");
        testTextLineParser(parts, charset, "#-#");
    }
    
    /**
     * Tests text line parsing with varying parts, charset and separator.
     * 
     * @param parts the parts to be tested in positional mode
     * @param charset the charset
     * @param separator the separator
     * @return the parse result for further tests
     * @throws IOException in case that parsing fails
     */
    private ParseResult<String> testTextLineParser(String[] parts, String charset, String separator) 
        throws IOException {
        String testString = "";
        for (String p: parts) {
            if (testString.length() > 0) {
                testString += separator;
            }
            testString += p;
        }
        TextLineParser parser = new TextLineParser(charset, separator);
        Assert.assertNotNull(parser.getConverter());
        ParseResult<String> result = parser.parse(testString.getBytes(charset));
        Assert.assertNotNull(result);
        Assert.assertEquals(parts.length, result.getDataCount());
        return result;
    }
    
    /**
     * Tests cases where the converter shall fail.
     */
    @Test
    public void testConverterFail() {
        InputConverter<String> conv = TextLineParser.CONVERTER;

        try {
            conv.toInteger("abba");
            Assert.fail("No exception thrown");
        } catch (IOException e) {
        }

        try {
            conv.toFloat("abba");
            Assert.fail("No exception thrown");
        } catch (IOException e) {
        }

        try {
            conv.toLong("abba");
            Assert.fail("No exception thrown");
        } catch (IOException e) {
        }

        try {
            conv.toDouble("abba");
            Assert.fail("No exception thrown");
        } catch (IOException e) {
        }
    }

    /**
     * A test parser without encoding.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomBaseParser implements InputParser<String> {
        
        @Override
        public ParseResult<String> parse(byte[] data) throws IOException {
            return null;
        }

        @Override
        public InputConverter<String> getConverter() {
            return null;
        }
        
    }

    /**
     * A test parser with encoding.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class CustomExBaseParser extends CustomBaseParser {
        
        private String encoding;
        
        /**
         * Creates an instance and sets the {@code encoding}.
         * 
         * @param encoding the character encoding
         */
        public CustomExBaseParser(String encoding) {
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
     * Tests {@link ParserUtils#createInstance(ClassLoader, String, Class, String)}.
     */
    @Test
    public void testCreateInstance() {
        ClassLoader loader = InputParserTest.class.getClassLoader();
        Assert.assertNull(ParserUtils.createInstance(loader, "me.here.Parser", "UTF-8"));
        
        Assert.assertNotNull(ParserUtils.createInstance(loader, CustomBaseParser.class.getName(), "UTF-8"));
        InputParser<?> p = ParserUtils.createInstance(loader, CustomExBaseParser.class.getName(), "UTF-8");
        Assert.assertTrue(p instanceof CustomExBaseParser);
        Assert.assertEquals("UTF-8", ((CustomExBaseParser) p).getEncoding());
    }

}
