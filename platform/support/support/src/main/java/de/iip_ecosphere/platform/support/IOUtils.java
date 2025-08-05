/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * I/O utilities in the style of {@code org.apache.commons.io.IOUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IOUtils {

    /**
     * Gets the contents of an {@link InputStream} as a list of Strings,
     * one entry per line, using the Java platform default character encoding.
     *
     * @param in the {@link InputStream} to read
     * @return the list of Strings
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLines(InputStream in) throws IOException {
        return readLines(in, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@link InputStream} as a list of Strings,
     * one entry per line, using the specified character encoding.
     *
     * @param in the {@link InputStream} to read
     * @param charset the charset to use, <b>null</b> means platform default
     * @return the list of Strings
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLines(InputStream in, Charset charset) throws IOException {
        try {
            return org.apache.commons.io.IOUtils.readLines(in, charset);
        } catch (UncheckedIOException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Gets the contents of an {@link InputStream} as a String
     * using the specified Java platform default character encoding.
     *
     * @param in the {@link InputStream} to read
     * @return the requested String
     * @throws IOException if an I/O error occurs
     */
    public static String toString(InputStream in) throws IOException {
        return toString(in, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an {@link InputStream} as a String
     * using the specified character encoding.
     *
     * @param in the {@link InputStream} to read
     * @param charset the charset to use, <b>null</b> means platform default
     * @return the requested String
     * @throws IOException if an I/O error occurs
     */
    public static String toString(InputStream in, Charset charset) throws IOException {
        return org.apache.commons.io.IOUtils.toString(in, charset);
    }

}
