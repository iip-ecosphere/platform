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

import de.iip_ecosphere.platform.connectors.parser.InputParser.ParseResult;
import org.junit.Assert;
import de.iip_ecosphere.platform.connectors.parser.TextLineParser;

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
        final String[] parts = new String[]{"aaa", "bbb", "ccc", "ddd"};
        testTextLineParser(parts, charset, "#");
        testTextLineParser(parts, charset, "#-#");

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
    private ParseResult testTextLineParser(String[] parts, String charset, String separator) throws IOException {
        String testString = "";
        for (String p: parts) {
            if (testString.length() > 0) {
                testString += separator;
            }
            testString += p;
        }
        TextLineParser parser = new TextLineParser(charset, separator);
        ParseResult result = parser.parse(testString.getBytes(charset));
        Assert.assertNotNull(result);
        Assert.assertEquals(parts.length, result.getDataCount());
        for (int i = 0; i < parts.length; i++) {
            Assert.assertEquals(parts[i], result.getData(i));
        }
        return result;
    }

}
