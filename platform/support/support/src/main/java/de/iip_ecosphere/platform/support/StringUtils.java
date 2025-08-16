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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.text.StringEscapeUtils;

/**
 * String utility functions, partially wrapping {@code org.apache.commons.text}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringUtils {
    
    /**
     * Short prefix style with limited string output.
     */
    static final ToStringStyle SHORT_STRING_STYLE = new ShortStringToStringStyle(); 
    
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
     * Checks if a CharSequence is not empty (""), not null and not whitespace only.
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *  not empty and not null and not whitespace only
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(cs);
    }
    
    /**
     * Replaces a String with another String inside a larger String, once.
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  <b>null</b> if null String input
     */
    public static String replaceOnce(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
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
    
    /**
     * Turns an object to an readable string, usually using reflection. Uses some default
     * style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public static String toString(Object obj) {
        return ReflectionToStringBuilder.toString(obj);
    }
    
    /**
     * Short prefix style with limited string output.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static final class ShortStringToStringStyle extends ToStringStyle {
        
        private static final long serialVersionUID = 1L;

        /**
         * <p>Constructor.</p>
         *
         * <p>Use the static constant rather than instantiating.</p>
         */
        ShortStringToStringStyle() {
            super();
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
        }
        
        @Override
        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            if (value instanceof String) { // in particular base64 images
                String sVal = (String) value;
                if (sVal.length() > 20) {
                    value = sVal.substring(0, 20) + "...";
                }
            }
            super.append(buffer, fieldName, value, fullDetail);
        }

        /**
         * <p>Ensure singleton after serialization.</p>
         * @return the singleton
         */
        private Object readResolve() {
            return SHORT_STRING_STYLE;
        }
        
    }

    /**
     * Turns an object to an readable string, usually using reflection. Uses oktoflow short style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public static String toStringShortStyle(Object obj) {
        return ReflectionToStringBuilder.toString(obj, SHORT_STRING_STYLE);
    }
    
    /**
     * Removes a substring only if it is at the beginning of a source string,
     * otherwise returns the source string.

     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <b>null</b> if <b>null</b> String input
     */
    public static String removeStart(String str, String remove) {
        return org.apache.commons.lang3.StringUtils.removeStart(str, remove);
    }
    
    /**
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <b>null</b> if <b>null</b> String input
     */
    public static String removeEnd(String str, String remove) {
        return org.apache.commons.lang3.StringUtils.removeEnd(str, remove);
    }

}
