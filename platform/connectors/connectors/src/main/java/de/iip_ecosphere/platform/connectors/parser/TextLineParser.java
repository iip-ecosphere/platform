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

package de.iip_ecosphere.platform.connectors.parser;

import java.io.IOException;

/**
 * Implements a generic line parser, i.e., data instances are assumed to be given in a 
 * single line of text.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TextLineParser implements InputParser<String> {

    /**
     * Own parser converter type to hide implementing class for future modifications.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TextLineParserConverter extends ConverterFromString {
    }

    /**
     * Own parser result type to hide implementing class for future modifications.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TextLineParseResult extends ArrayParseResult {

        /**
         * Creates an array-based parse result.
         * 
         * @param data the parsed data
         */
        protected TextLineParseResult(String[] data) {
            super(data);
        }
        
        /**
         * Creates an array-based parse result.
         * 
         * @param data the parsed data
         * @param baseIndex the base index set as context root, {code 0} for top-level
         * @param parent the parent result representing the context where a {@code #stepInto(String, int)} happened, 
         *     <b>null</b> for the top context
         */
        protected TextLineParseResult(String[] data, int baseIndex, ArrayParseResult parent) {
            super(data, baseIndex, parent);
        }

        @Override
        public TextLineParseResult stepInto(String name, int index) {
            return new TextLineParseResult(getData(), index, this);
        }

        @Override
        public TextLineParseResult stepOut() {
            return (TextLineParseResult) super.stepOut();
        }

    }
    
    public static final TextLineParserConverter CONVERTER = new TextLineParserConverter();
    private String charset;
    private String separator;
    
    /**
     * Creates a new text line parser.
     * 
     * @param charset the charset of the text encoding (preliminary as string)
     * @param separator the separator to be used between data fields
     */
    public TextLineParser(String charset, String separator) {
        this.charset = charset;
        this.separator = separator;
    }
    
    @Override
    public TextLineParseResult parse(byte[] data) throws IOException {
        String s = new String(data, charset); // unsupportedencoding -> IOException
        return new TextLineParseResult(s.split(separator)); // we may pool this...
    }

    @Override
    public TextLineParserConverter getConverter() {
        return CONVERTER;
    }

}
