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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;

/**
 * A simple text line formatter for given separators. Field names are ignored.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TextLineFormatter implements OutputFormatter<String> {

    public static final OutputConverter<String> CONVERTER = new ConverterToString();
    private String charset;
    private String separator;
    private StringBuilder tmp = new StringBuilder();

    /**
     * Creates a new text line formatter.
     * 
     * @param charset the charset of the text encoding (preliminary as string)
     * @param separator the separator to be used between data fields
     */
    public TextLineFormatter(String charset, String separator) {
        this.charset = charset;
        this.separator = separator;
    }
    
    @Override
    public void add(String name, String data) throws IOException {
        if (tmp.length() > 0) {
            tmp.append(separator);
        }
        tmp.append(data);
    }

    @Override
    public byte[] chunkCompleted() throws IOException {
        String result = tmp.toString();
        tmp = new StringBuilder();
        return result.getBytes(charset);
    }

    @Override
    public OutputConverter<String> getConverter() {
        return CONVERTER;
    }

}
