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

import org.apache.commons.text.StringEscapeUtils;

/**
 * String utility functions, partially wrapping {@code org.apache.commons.text}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringUtils {
    
    /**
     * Escapes the characters in a {@code String} using Java String rules.
     * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     *
     * @param input  String to escape values in, may be <b>null</b>
     * @return String with escaped values, <b>null</b> if <b>null</b> string input
     */
    public static final String escapeJava(final String input) {
        return StringEscapeUtils.escapeJava(input);
    }

    /**
     * Unescapes any Java literals found in the {@code String}.
     *
     * @param input  the {@code String} to unescape, may be <b>null</b>
     * @return a new unescaped {@code String}, <b>null</b> if <b>null</b> string input
     */
    public static final String unescapeJava(final String input) {
        return StringEscapeUtils.unescapeJava(input);
    }
    
    /**
     * Escapes the characters in a {@code String} using Json String rules.
     * Escapes any values it finds into their Json String form.
     * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     *
     * @param input  String to escape values in, may be <b>null</b>
     * @return String with escaped values, <b>null</b> if null string input
     */
    public static final String escapeJson(final String input) {
        return StringEscapeUtils.escapeJson(input);
    }

    /**
     * Unescapes any Json literals found in the {@code String}.
     *
     * @param input  the {@code String} to unescape, may be <b>null</b>
     * @return A new unescaped {@code String}, <b>null</b> if null string input
     */
    public static final String unescapeJson(final String input) {
        return StringEscapeUtils.unescapeJson(input);
    }
    
    /**
     * Returns either the passed in CharSequence, or if the CharSequence is
     * whitespace, empty ({@code ""}) or <b>null</b>, the value of {@code defaultStr}.
     *
     * @param <T> the specific kind of CharSequence
     * @param str the CharSequence to check, may be <b>null</b>
     * @param defaultStr  the default CharSequence to return
     *  if the input is whitespace, empty ({@code ""}) or <b>null</b>
     * @return the passed in CharSequence, or the default
     */
    public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return org.apache.commons.lang3.StringUtils.defaultIfBlank(str, defaultStr);
    }

    /**
     * Returns either the passed in CharSequence, or if the CharSequence is
     * empty or <b>null</b>, the value of {@code defaultStr}.
     *
     * @param <T> the specific kind of CharSequence
     * @param str  the CharSequence to check, may be null
     * @param defaultStr  the default CharSequence to return
     *  if the input is empty ({@code ""}) or <b>null</b>
     * @return the passed in CharSequence, or the default
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return org.apache.commons.lang3.StringUtils.defaultIfEmpty(str, defaultStr);
    }

    /**
     * Checks if a CharSequence is empty ({@code ""}), <b>null</b> or whitespace only.
     *
     * @param cs  the CharSequence to check, may be <b>null</b>
     * @return {@code true} if the CharSequence is <b>null</b>, empty or whitespace only
     */
    public static boolean isBlank(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
    }

    /**
     * Checks if a CharSequence is empty ({@code ""}) or <b>null</b>.
     *
     * @param cs  the CharSequence to check, may be <b>null</b>
     * @return {@code true} if the CharSequence is empty or <b>null</b>
     */
    public static boolean isEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

}
