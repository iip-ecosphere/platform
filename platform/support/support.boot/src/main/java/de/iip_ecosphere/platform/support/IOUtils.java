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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.support.commons.Commons;

/**
 * I/O utilities.
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
        // needed by plugin management
        InputStreamReader inputStreamReader = new InputStreamReader(in, charset);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return bufferedReader.lines().collect(Collectors.toList());
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
        return Commons.getInstance().toString(in, charset);
    }
    
    /**
     * Gets the contents of an {@link InputStream} as a {@code byte[]}.
     *
     * @param inputStream the {@link InputStream} to read.
     * @return the requested byte array.
     * @throws NullPointerException if the InputStream is {@code null}.
     * @throws IOException if an I/O error occurs or reading more than {@link Integer#MAX_VALUE} occurs.
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        return Commons.getInstance().toByteArray(inputStream);
    }
    
    /**
     * Writes bytes from a {@code byte[]} to an {@link OutputStream}.
     *
     * @param data the byte array to write, do not modify during output,
     *     <b>null</b> ignored
     * @param outputStream the {@link OutputStream} to write to
     * @throws IOException if an I/O error occurs
     */
    public static void write(byte[] data, OutputStream outputStream) throws IOException {
        Commons.getInstance().write(data, outputStream);
    }

}
