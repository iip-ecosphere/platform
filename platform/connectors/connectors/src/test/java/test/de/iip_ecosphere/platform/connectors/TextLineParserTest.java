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

import org.junit.Test;

import org.junit.Assert;

import de.iip_ecosphere.platform.connectors.parser.InputParser;
import de.iip_ecosphere.platform.connectors.parser.ParserUtils;
import de.iip_ecosphere.platform.connectors.parser.TextLineParser;
import de.iip_ecosphere.platform.connectors.parser.TextLineParser.TextLineParseResult;
import de.iip_ecosphere.platform.connectors.parser.TextLineParser.TextLineParserConverter;

/**
 * Tests the {@link TextLineParser}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TextLineParserTest {
    
    /**
     * Tests basic successful text line parsing with different charsets and separators.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testTextLineParser() throws IOException {
        String charset = StandardCharsets.UTF_8.name();
        final String[] parts = new String[]{"123", "bbb", "true", "0.45", "0.56", "12345", "20", "TEST1"};
        testTextLineParser(parts, charset, "#");
        TextLineParseResult pr = testTextLineParser(parts, charset, "#-#");
        Assert.assertEquals("", pr.getFieldName()); // must hold always, no deeper indexes supported

        TextLineParserConverter conv = TextLineParser.CONVERTER; // unusual, ask parser, just for test
        Assert.assertEquals(parts[0], conv.toString(pr.getData("fieldX", 0)));
        Assert.assertEquals(parts[1], conv.toString(pr.getData("field2", 1)));
        Assert.assertEquals(parts[1], conv.toString(pr.getLocalData("field2", 1)));
        pr.getData(d -> Assert.assertEquals(parts[1], conv.toString(d)), "field2", 1);
        pr.getLocalData(d -> Assert.assertEquals(parts[1], conv.toString(d)), "field2", 1);
        Assert.assertEquals(parts[1], conv.toString(pr.getData("f", 1)));
        Assert.assertEquals(0.45, conv.toDouble(pr.getData("f", 1, 2)), 0.01);
        Assert.assertEquals(0.56, conv.toDouble(pr.getData("", 4)), 0.01);
        Assert.assertEquals(12345, conv.toInteger(pr.getData("", 5)));
        Assert.assertEquals(MyEnum.TEST2, conv.toEnum(pr.getData("", 6), MyEnum.class));
        Assert.assertEquals(MyEnum.TEST1, conv.toEnum(pr.getData("", 7), MyEnum.class));
        Assert.assertEquals(MyEnum.TEST1, conv.toEnum(pr.getLocalData("", 7), MyEnum.class));
        try {
            pr.getData("f", 10);
            Assert.fail("No Exception");
        } catch (IOException e) {
        }
        pr.getData(d -> Assert.fail(), "f", 10);
        pr.getLocalData(d -> Assert.fail(), "f", 10);

        charset = StandardCharsets.ISO_8859_1.name();
        testTextLineParser(parts, charset, "#");
        testTextLineParser(parts, charset, "#-#");
        
        pr = pr.stepInto("x", 1); // relocate 1 to 0
        Assert.assertEquals(parts[1], conv.toString(pr.getData("field2", 0)));
        Assert.assertEquals(0.45, conv.toDouble(pr.getData("f", 2)), 0.01);
        pr = pr.stepOut(); // reset relocation
        Assert.assertEquals(parts[0], conv.toString(pr.getData("fieldX", 0)));
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
    private TextLineParseResult testTextLineParser(String[] parts, String charset, String separator) 
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
        TextLineParseResult result = parser.parse(testString.getBytes(charset));
        Assert.assertNotNull(result);
        Assert.assertEquals(parts.length, result.getDataCount());
        return result;
    }
    
    /**
     * Tests cases where the converter shall fail.
     */
    @Test
    public void testConverterFail() {
        TextLineParserConverter conv = TextLineParser.CONVERTER;

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
     * Tests {@link ParserUtils#createInstance(ClassLoader, String, String)}.
     */
    @Test
    public void testCreateInstance() {
        ClassLoader loader = TextLineParserTest.class.getClassLoader();
        Assert.assertNull(ParserUtils.createInstance(loader, "me.here.Parser", "UTF-8"));
        
        Assert.assertNotNull(ParserUtils.createInstance(loader, CustomBaseParser.class.getName(), "UTF-8"));
        InputParser<?> p = ParserUtils.createInstance(loader, CustomExBaseParser.class.getName(), "UTF-8");
        Assert.assertTrue(p instanceof CustomExBaseParser);
        Assert.assertEquals("UTF-8", ((CustomExBaseParser) p).getEncoding());
    }

}
