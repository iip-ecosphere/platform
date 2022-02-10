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
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a generic line parser, i.e., data instances are assumed to be given in a 
 * single line of text.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TextLineParser implements InputParser {

    private String charset;
    private String separator;
    private Map<String, Integer> nameIndexMapping;
    
    /**
     * Creates a new text line parser.
     * 
     * @param charset the charset of the text encoding (preliminary as string)
     * @param separator the separator to be used between data fields
     * @param nameIndexMapping optional mapping of field names to index numbers
     */
    public TextLineParser(String charset, String separator, Map<String, Integer> nameIndexMapping) {
        this.charset = charset;
        this.separator = separator;
        this.nameIndexMapping = nameIndexMapping;
    }

    /**
     * Creates a new text line parser with no name mapping.
     * 
     * @param charset the charset of the text encoding (preliminary as string)
     * @param separator the separator to be used between data fields
     */
    public TextLineParser(String charset, String separator) {
        this(charset, separator, new HashMap<>());
    }
    
    @Override
    public ParseResult parse(byte[] data) throws IOException {
        String s = new String(data, charset); // unsupportedencoding -> IOException
        return new ArrayParseResult(s.split(separator), nameIndexMapping); // we may pool this...
    }

}
